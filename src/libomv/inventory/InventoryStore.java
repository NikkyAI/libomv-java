/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.inventory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import libomv.GridClient;
import libomv.assets.AssetItem.AssetType;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.MultiMap;

/**
 * Responsible for maintaining the inventory structure. InventoryStore constructs nodes
 * and manages node children as is necessary to maintain a coherent hierarchy.
 * Other classes should not manipulate or create InventoryNodes explicitly. When
 * a node's parent changes (when a folder is moved, for example) simply pass
 * InventoryStore.add(parentID, node) the updated InventoryNode without changing the
 * parentID yet and it will make the appropriate changes to its internal representation.
 * 
 * We use a lazy tree linking approach in order to handle nodes that arrive before their
 * parent has arrived. Each node also has a storage for the parentID besides the direct
 * reference to its parent. When inserting a new node into the tree we attempt to lookup
 * the parent folder and store its reference in our node, but don't throw an exception if
 * that fails. So in order to make this work an external client should never directly
 * attempt to access the parent folder in a node but instead use the getParent() method
 * in this class. In order to enforce that, the parent node member of the InventoryNode
 * is set to protected. 
 */
public class InventoryStore extends InventoryFolder
{
	private static final long serialVersionUID = 1L;

	// #region CallbackArgs classes
	public class InventoryObjectUpdatedCallbackArgs implements CallbackArgs
	{
		private final InventoryNode m_OldObject;
		private final InventoryNode m_NewObject;

		public final InventoryNode getOldObject()
		{
			return m_OldObject;
		}

		public final InventoryNode getNewObject()
		{
			return m_NewObject;
		}

		public InventoryObjectUpdatedCallbackArgs(InventoryNode oldObject, InventoryNode newObject)
		{
			this.m_OldObject = oldObject;
			this.m_NewObject = newObject;
		}
	}

	public class InventoryObjectRemovedCallbackArgs implements CallbackArgs
	{
		private final InventoryNode m_Obj;
		public final InventoryNode getObj()
		{
			return m_Obj;
		}

		public InventoryObjectRemovedCallbackArgs(InventoryNode obj)
		{
			this.m_Obj = obj;
		}
	}

	public class InventoryObjectAddedCallbackArgs implements CallbackArgs
	{
		private final InventoryNode m_Obj;

		public final InventoryNode getObj()
		{
			return m_Obj;
		}

		public InventoryObjectAddedCallbackArgs(InventoryNode obj)
		{
			this.m_Obj = obj;
		}
	}

	// #endregion CallbackArgs classes

	public CallbackHandler<InventoryObjectUpdatedCallbackArgs> OnInventoryObjectUpdated = new CallbackHandler<InventoryObjectUpdatedCallbackArgs>();

	public CallbackHandler<InventoryObjectRemovedCallbackArgs> OnInventoryObjectRemoved = new CallbackHandler<InventoryObjectRemovedCallbackArgs>();

	public CallbackHandler<InventoryObjectAddedCallbackArgs> OnInventoryObjectAdded = new CallbackHandler<InventoryObjectAddedCallbackArgs>();

	private GridClient _Client;

	private HashMap<UUID, InventoryItem> _Items;
	private HashMap<UUID, InventoryFolder> _Folders;
	private MultiMap<UUID, InventoryNode> _Unresolved;

	public InventoryStore(GridClient client)
	{
		this(client, client.Self.getAgentID());
	}

	public InventoryStore(GridClient client, UUID owner)
	{
		_Client = client;

		if (owner == null || owner.equals(UUID.Zero))
		{
			Logger.Log("Inventory owned by nobody!", LogLevel.Warning, _Client);
		}
		_Items = new HashMap<UUID, InventoryItem>();
		_Folders = new HashMap<UUID, InventoryFolder>();
		_Unresolved = new MultiMap<UUID, InventoryNode>();

		name = "Root";
		ownerID = owner;
		preferredType = AssetType.RootFolder;
		children = new ArrayList<InventoryNode>(2);
		children.add(new InventoryFolder());
		children.add(new InventoryFolder());	
	}
	
	// The root folder of the avatars inventory
	public final InventoryFolder getInventoryFolder()
	{
		return (InventoryFolder)children.get(0);
	}
	public final void setInventoryFolder(UUID folderID)
	{
		InventoryFolder folder = new InventoryFolder(folderID, this.itemID, this.ownerID);
		folder.parent = this;
		folder.name = "Inventory";
		folder.preferredType = AssetType.RootFolder;
		children.set(0, folder);
		_Folders.put(folderID, folder);
	}
	
	// The root folder of the default shared library
	public final InventoryFolder getLibraryFolder()
	{
		return (InventoryFolder)children.get(1);
	}
	public final void setLibraryFolder(UUID folderID, UUID ownerID)
	{
		InventoryFolder folder = new InventoryFolder(folderID, this.itemID, ownerID);
		folder.parent = this;
		folder.name = "Library";
		folder.preferredType = AssetType.RootFolder;
		children.set(1, folder);
		_Folders.put(folderID, folder);
	}

	/**
	 * Used to find out if Inventory contains the InventoryItem specified by
	 * <code>uuid</code>.
	 * 
	 * @param uuid
	 *            The UUID to check.
	 * @return true if inventory contains an item with uuid, false otherwise
	 */
	public final boolean containsItem(UUID uuid)
	{
		return _Items.containsKey(uuid);
	}

	/**
	 * Used to find out if Inventory contains the InventoryFolder specified by
	 * <code>uuid</code>.
	 * 
	 * @param uuid
	 *            The UUID to check.
	 * @return true if inventory contains an item with uuid, false otherwise
	 */
	public final boolean containsFolder(UUID uuid)
	{
		return _Folders.containsKey(uuid);
	}

	@Override
	public final boolean contains(InventoryNode node)
	{
		if (node.getType() == InventoryType.Folder)
		{
			return _Folders.containsKey(node.itemID);
		}
		return _Items.containsKey(node.itemID);
	}

	/**
	 * Returns the item with the specified id
	 * 
	 * @param uuid
	 *            The UUID of the InventoryItem to get
	 * @return The InventoryItem corresponding to <code>uuid</code>.
	 */
	public InventoryItem getItem(UUID uuid)
	{
		return _Items.get(uuid);
	}

	/**
	 * Returns the folder with the specified id
	 * 
	 * @param uuid
	 *            The UUID of the InventoryFolder to get
	 * @return The InventoryFolder corresponding to <code>uuid</code>.
	 */
	public InventoryFolder getFolder(UUID uuid)
	{
		return _Folders.get(uuid);
	}

	/**
	 * Returns either a folder or item with the specified id
	 * 
	 * @param uuid
	 *            The UUID of the InventoryNode to get
	 * @return The InventoryNode corresponding to <code>uuid</code>.
	 */
	public final InventoryNode getNode(UUID uuid)
	{
		if (_Folders.containsKey(uuid))
			return _Folders.get(uuid);
		return _Items.get(uuid);	
	}
	
	/**
	 * Adds a node to a parent if it can be resolved and stores the node in the according
	 * HashMap for later reference and fast lookup by its ID, first removing it from its
	 * previous parent if present and not equal to the new parent.
	 * 
	 * @param node The node whose parentID to return
	 */
	@Override
	public final void add(InventoryNode node)
	{
		synchronized (_Folders)
		{
			// Check if there are any unresolved nodes referring to us
			if (node.getType() == InventoryType.Folder && _Unresolved.containsKey(node.itemID))
			{
				InventoryFolder parent = (InventoryFolder)node;
				Iterator<InventoryNode> iter = _Unresolved.iterator(node.itemID);
				while (iter.hasNext())
				{
					InventoryNode n = iter.next();
					n.parent = parent;
					parent.children.add(n);
					iter.remove();
				}
			}

			// Check if there was already a parent and if it matches with the new parent
			if (node.parent != null)
			{
				if (!node.parent.itemID.equals(node.parentID))
				{
					// This is a reassignment of the parent so remove us from the previous parent
					node.parent.children.remove(node);
				}
			}
			
			// Link this node to its parent if it already exists, otherwise put it in the unresolved list
			if (_Folders.containsKey(node.parentID))
			{
				node.parent = _Folders.get(node.parentID);
				if (!node.parent.contains(node))
				{
					node.parent.add(node);
				}

				if (node.getType() == InventoryType.Folder)
				{
					_Folders.put(node.itemID, (InventoryFolder)node);
				}
				else
				{
					_Items.put(node.itemID, (InventoryItem)node);			
				}
			}
			else
			{
				_Unresolved.put(node.parentID, node);
			}
		}
	}

	public final void add(UUID parentID, InventoryNode node)
	{
		node.parentID = parentID;
		add(node);
	}
	
	/**
	 * Removes the InventoryNode and all related node data from the Inventory
	 * 
	 * @param item
	 *            The InventoryNode to remove.
	 */
	@Override
	public final void remove(InventoryNode node)
	{
		synchronized (_Folders)
		{
			if (node.getType() == InventoryType.Folder)
			{
				InventoryFolder folder = (InventoryFolder)node;
				if (folder.children != null)
				{
					Iterator<InventoryNode> iter = folder.children.iterator();
					while (iter.hasNext())
						remove(iter.next());
				}
				_Folders.remove(node.itemID);
			}
			else
			{
				_Items.remove(node.itemID);
			}
			
			if (node.parent != null)
				node.parent.remove(node);
		}
	}	

	/**
	 * Returns the contents of the specified folder
	 * 
	 * @param folder
	 *            A folder's UUID
	 * @return The contents of the folder corresponding to <code>folder</code>
	 * @exception InventoryException
	 *                When <code>folder</code> does not exist in the inventory
	 */
	public final ArrayList<InventoryNode> getContents(UUID folder) throws InventoryException
	{
		synchronized (_Folders)
		{
			if (!_Folders.containsKey(folder))
			{
				throw new InventoryException("Unknown folder: " + folder);
			}
			return _Folders.get(folder).getContents();
		}
	}

	public final void resolveList()
	{
		synchronized (_Folders)
		{
			Set<Entry<UUID, Collection<InventoryNode>>> set = _Unresolved.entrySet();
			while (set.iterator().hasNext())
			{
				Entry<UUID, Collection<InventoryNode>> e = set.iterator().next();
				if (_Folders.containsKey(e.getKey()))
				{
					InventoryFolder parent = _Folders.get(e.getKey());
					Iterator<InventoryNode> iter = _Unresolved.get(e.getKey()).iterator();
					while (iter.hasNext())
					{
						InventoryNode n = iter.next();
						n.parent = parent;
						parent.children.add(n);
						iter.remove();
					}
				}
			}
		}
	}
	
	/**
	 * Saves the current inventory structure to a cache file
	 * 
	 * @param filename Name of the cache file to save to
	 */
	public final void saveToDisk(String filename) throws IOException
	{
		resolveList();
		FileOutputStream fos = new FileOutputStream(filename);
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(fos);
			try
			{
				Logger.Log("Caching inventory to " + filename, LogLevel.Info);
				out.writeLong(serialVersionUID);
				out.writeObject(ownerID);				
				super.writeObject(out);
			}
			finally
			{
				out.close();
			}
		}
		catch (Throwable ex)
		{
			Logger.Log("Error saving inventory cache to disk :" + ex.getMessage(), LogLevel.Error, _Client, ex);
		}
		finally
		{
			fos.close();
		}
	}

	/**
	 * Loads in inventory cache file into the inventory structure. Note only
	 * valid to call after login has been successful.
	 * 
	 * @param filename Name of the cache file to load
	 * @return The number of inventory items sucessfully reconstructed into the
	 * inventory node tree
	 */
	public final int restoreFromDisk(String filename) throws IOException
	{
		_Items.clear();
		_Folders.clear();
		_Unresolved.clear();
		
		FileInputStream fis = new FileInputStream(filename);
		try
		{
			ObjectInputStream in = new ObjectInputStream(fis);
			try
			{
				if (serialVersionUID != in.readLong())
					throw new InvalidObjectException("InventoryRoot serial version mismatch");
				ownerID = (UUID)in.readObject();
				super.readObject(in);
			}
			finally
			{
				in.close();
			}
		}
		catch (Throwable ex)
		{
			Logger.Log("Error accessing inventory cache file :" + ex.getMessage(), LogLevel.Error, _Client, ex);
		}
		finally
		{
			fis.close();
		}
		
		Stack<InventoryFolder> stack = new Stack<InventoryFolder>();
		stack.push(this);
		
		int folder_count = 0, item_count = 0;
		while (stack.size() > 0)
		{
			InventoryFolder folder = stack.pop();
			Iterator<InventoryNode> iter = folder.children.iterator();
			while (iter.hasNext())
			{
				InventoryNode node = iter.next();
				if (node.getType() == InventoryType.Folder)
				{
					stack.push((InventoryFolder)node);
					_Folders.put(node.itemID, (InventoryFolder)node);
					folder_count++;
				}
				else
				{
					_Items.put(node.itemID, (InventoryItem)node);
					item_count++;
				}
			}
		}
		Logger.Log("Read " + folder_count + " folders and " + item_count + " items from inventory cache file", LogLevel.Info, _Client);
		return item_count;
	}
}
