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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import libomv.GridClient;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/**
 * Responsible for maintaining inventory structure. Inventory constructs nodes
 * and manages node children as is necessary to maintain a coherant hirarchy.
 * Other classes should not manipulate or create InventoryNodes explicitly. When
 * A node's parent changes (when a folder is moved, for example) simply pass
 * Inventory the updated InventoryFolder and it will make the appropriate
 * changes to its internal representation.
 */
public class InventoryStore
{
	// #region CallbackArgs classes
	public class InventoryObjectUpdatedCallbackArgs implements CallbackArgs
	{
		private final InventoryBase m_OldObject;
		private final InventoryBase m_NewObject;

		public final InventoryBase getOldObject()
		{
			return m_OldObject;
		}

		public final InventoryBase getNewObject()
		{
			return m_NewObject;
		}

		public InventoryObjectUpdatedCallbackArgs(InventoryBase oldObject, InventoryBase newObject)
		{
			this.m_OldObject = oldObject;
			this.m_NewObject = newObject;
		}
	}

	public class InventoryObjectRemovedCallbackArgs implements CallbackArgs
	{
		private final InventoryBase m_Obj;

		public final InventoryBase getObj()
		{
			return m_Obj;
		}

		public InventoryObjectRemovedCallbackArgs(InventoryBase obj)
		{
			this.m_Obj = obj;
		}
	}

	public class InventoryObjectAddedCallbackArgs implements CallbackArgs
	{
		private final InventoryBase m_Obj;

		public final InventoryBase getObj()
		{
			return m_Obj;
		}

		public InventoryObjectAddedCallbackArgs(InventoryBase obj)
		{
			this.m_Obj = obj;
		}
	}

	// #endregion CallbackArgs classes

	public CallbackHandler<InventoryObjectUpdatedCallbackArgs> OnInventoryObjectUpdated = new CallbackHandler<InventoryObjectUpdatedCallbackArgs>();

	public CallbackHandler<InventoryObjectRemovedCallbackArgs> OnInventoryObjectRemoved = new CallbackHandler<InventoryObjectRemovedCallbackArgs>();

	public CallbackHandler<InventoryObjectAddedCallbackArgs> OnInventoryObjectAdded = new CallbackHandler<InventoryObjectAddedCallbackArgs>();

	private InventoryNode _RootNode;
	private InventoryNode _LibraryRootNode;

	// The root node of the avatars inventory
	public final InventoryNode getRootNode()
	{
		return _RootNode;
	}

	// The root node of the default shared library
	public final InventoryNode getLibraryRootNode()
	{
		return _LibraryRootNode;
	}

	// The root folder of this avatars inventory
	public final InventoryFolder getRootFolder()
	{
		InventoryBase root = getRootNode().getData();
		return (InventoryFolder) ((root instanceof InventoryFolder) ? root : null);
	}

	public final void setRootFolder(InventoryFolder value)
	{
		value.Name = Helpers.EmptyString;
		value.ParentUUID = UUID.Zero;
		updateNodeFor(value);
		_RootNode = Items.get(value.UUID);
	}

	// The default shared library folder
	public final InventoryFolder getLibraryFolder()
	{
		InventoryBase root = getLibraryRootNode().getData();
		return (InventoryFolder) ((root instanceof InventoryFolder) ? root : null);
	}

	public final void setLibraryFolder(InventoryFolder value)
	{
		value.Name = Helpers.EmptyString;
		value.ParentUUID = UUID.Zero;
		updateNodeFor(value);
		_LibraryRootNode = Items.get(value.UUID);
	}

	public final UUID getOwner()
	{
		return _Owner;
	}

	private UUID _Owner;

	private GridClient Client;

	private HashMap<UUID, InventoryNode> Items = new HashMap<UUID, InventoryNode>();

	public InventoryStore(GridClient client)
	{
		this(client, client.Self.getAgentID());
	}

	public InventoryStore(GridClient client, UUID owner)
	{
		Client = client;
		// Manager = manager;
		_Owner = owner;
		if (owner == null || owner.equals(UUID.Zero))
		{
			Logger.Log("Inventory owned by nobody!", LogLevel.Warning, Client);
		}
		Items = new HashMap<UUID, InventoryNode>();
	}

	public final ArrayList<InventoryBase> getContents(InventoryFolder folder) throws InventoryException
	{
		return getContents(folder.UUID);
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
	public final ArrayList<InventoryBase> getContents(UUID folder) throws InventoryException
	{
		InventoryNode folderNode;
		if (!Items.containsKey(folder))
		{
			throw new InventoryException("Unknown folder: " + folder);
		}
		folderNode = Items.get(folder);
		synchronized (folderNode.getNodes())
		{
			ArrayList<InventoryBase> contents = new ArrayList<InventoryBase>(folderNode.getNodes().size());
			for (InventoryNode node : folderNode.getNodes().values())
			{
				contents.add(node.getData());
			}
			return contents;
		}
	}

	/**
	 * Updates the state of the InventoryNode and inventory data structure that
	 * is responsible for the InventoryObject. If the item was previously not
	 * added to inventory, it adds the item, and updates structure accordingly.
	 * If it was, it updates the InventoryNode, changing the parent node if
	 * <code>item.parentUUID</code> does not match
	 * <code>node.Parent.Data.UUID</code>.
	 * 
	 * You can not set the inventory root folder using this method
	 * 
	 * @param item
	 *            The InventoryObject to store
	 */
	public final void updateNodeFor(InventoryBase item)
	{
		InventoryNode itemParent = null;
		if (!item.ParentUUID.equals(UUID.Zero))
		{
			if (Items.containsKey(item.ParentUUID))
			{
				itemParent = Items.get(item.ParentUUID);
			}
			else
			{
				// OK, we have no data on the parent, let's create a fake one.
				InventoryFolder fakeParent = new InventoryFolder(item.ParentUUID);
				fakeParent.descendentCount = 1; // Dear god, please forgive me.
				itemParent = new InventoryNode(fakeParent);
				Items.put(item.ParentUUID, itemParent);
				// Unfortunately, this breaks the nice unified tree while we're
				// waiting for the parent's data to come in.
				// As soon as we get the parent, the tree repairs itself.
				Logger.DebugLog("Attempting to update inventory child of " + item.ParentUUID.toString()
						+ " when we have no local reference to that folder", Client);

				if (Client.Settings.FETCH_MISSING_INVENTORY)
				{
					// Fetch the parent
					ArrayList<UUID> fetchreq = new ArrayList<UUID>(1);
					fetchreq.add(item.ParentUUID);

					// TODO: Do something with these requests
				}
			}
		}

		if (Items.containsKey(item.UUID)) // We're updating.
		{
			InventoryNode itemNode = Items.get(item.UUID);
			InventoryNode oldParent = itemNode.getParent();
			// Handle parent change
			if (oldParent == null || itemParent == null || itemParent.getData().UUID != oldParent.getData().UUID)
			{
				if (oldParent != null)
				{
					oldParent.getNodes().remove(item.UUID);
				}
				if (itemParent != null)
				{
					itemParent.getNodes().put(item.UUID, itemNode);
				}
			}

			itemNode.setParent(itemParent);
			OnInventoryObjectUpdated.dispatch(new InventoryObjectUpdatedCallbackArgs(itemNode.getData(), item));
			itemNode.setData(item);
		}
		else
		// We're adding.
		{
			InventoryNode itemNode = new InventoryNode(item, itemParent);
			Items.put(item.UUID, itemNode);
			OnInventoryObjectAdded.dispatch(new InventoryObjectAddedCallbackArgs(item));
		}
	}

	public final InventoryNode getNodeFor(UUID uuid)
	{
		return Items.get(uuid);
	}

	/**
	 * Removes the InventoryObject and all related node data from Inventory.
	 * 
	 * @param item
	 *            The InventoryObject to remove.
	 */
	public final void removeNodeFor(InventoryBase item)
	{
		if (Items.containsKey(item.UUID))
		{
			InventoryNode node = Items.get(item.UUID);
			if (node.getParent() != null)
			{
				node.getParent().getNodes().remove(item.UUID);
			}
			Items.remove(item.UUID);
			OnInventoryObjectRemoved.dispatch(new InventoryObjectRemovedCallbackArgs(item));
		}

		// In case there's a new parent:

		if (Items.containsKey(item.ParentUUID))
		{
			InventoryNode newParent = Items.get(item.ParentUUID);
			newParent.getNodes().remove(item.UUID);
		}
	}

	/**
	 * Used to find out if Inventory contains the InventoryObject specified by
	 * <code>uuid</code>.
	 * 
	 * @param uuid
	 *            The UUID to check.
	 * @return true if inventory contains uuid, false otherwise
	 */
	public final boolean containsKey(UUID uuid)
	{
		return Items.containsKey(uuid);
	}

	public final boolean contains(InventoryBase obj)
	{
		return containsKey(obj.UUID);
	}

	/*
	 * Saves the current inventory structure to a cache file
	 * 
	 * @param filename Name of the cache file to save to
	 */
	public final void saveToDisk(String filename) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(filename);
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(fos);
			;
			try
			{
				Logger.Log("Caching " + Items.size() + " inventory items to " + filename, LogLevel.Info);
				for (Entry<UUID, InventoryNode> kvp : Items.entrySet())
				{
					out.writeObject(kvp.getValue());
				}
			}
			finally
			{
				out.close();
			}
		}
		catch (Throwable e)
		{
			Logger.Log("Error saving inventory cache to disk :" + e.getMessage(), LogLevel.Error);
		}
		finally
		{
			fos.close();
		}
	}

	/*
	 * Loads in inventory cache file into the inventory structure. Note only
	 * valid to call after login has been successful.
	 * 
	 * @param filename Name of the cache file to load
	 * 
	 * @return The number of inventory items sucessfully reconstructed into the
	 * inventory node tree
	 */
	public final int restoreFromDisk(String filename) throws IOException
	{
		ArrayList<InventoryNode> nodes = new ArrayList<InventoryNode>();
		int item_count = 0;

		FileInputStream fis = new FileInputStream(filename);
		try
		{
			ObjectInputStream in = new ObjectInputStream(fis);
			try
			{
				while (in.available() > 0)
				{
					nodes.add((InventoryNode) in.readObject());
					item_count++;
				}
			}
			finally
			{
				in.close();
			}
		}
		catch (Throwable e)
		{
			Logger.Log("Error accessing inventory cache file :" + e.getMessage(), LogLevel.Error);
		}
		fis.close();

		Logger.Log("Read " + item_count + " items from inventory cache file", LogLevel.Info);

		item_count = 0;
		ArrayList<InventoryNode> del_nodes = new ArrayList<InventoryNode>(); // nodes
																				// that
																				// we
																				// have
																				// processed
																				// and
																				// will
																				// delete
		ArrayList<UUID> dirty_folders = new ArrayList<UUID>(); // Tainted
																// folders that
																// we will not
																// restore items
																// into

		// Because we could get child nodes before parents we must iterate
		// around and only add nodes who have
		// a parent already in the list because we must update both child and
		// parent to link together
		// But sometimes we have seen orphan nodes due to bad/incomplete data
		// when caching so we have an
		// emergency abort route
		int stuck = 0;

		while (nodes.size() > 0 && stuck < 5)
		{
			for (InventoryNode node : nodes)
			{
				if (node.getData().ParentUUID.equals(UUID.Zero))
				{
					// We don't need the root nodes "My Inventory" etc as they
					// will already exist for the correct
					// user of this cache.
					del_nodes.add(node);
					item_count--;
				}
				else if (Items.containsKey(node.getData().UUID))
				{
					InventoryNode pnode = Items.get(node.getData().UUID);
					// We already have this it must be a folder
					if (node.getData() instanceof InventoryFolder)
					{
						InventoryFolder cache_folder = (InventoryFolder) node.getData();
						InventoryFolder server_folder = (InventoryFolder) pnode.getData();

						if (cache_folder.version != server_folder.version)
						{
							Logger.DebugLog("Inventory Cache/Server version mismatch on " + node.getData().Name + " "
									+ cache_folder.version + " vs " + server_folder.version);
							pnode.setNeedsUpdate(true);
							dirty_folders.add(node.getData().UUID);
						}
						del_nodes.add(node);
					}
				}
				else if (Items.containsKey(node.getData().ParentUUID))
				{
					InventoryNode pnode = Items.get(node.getData().ParentUUID);
					if (node.getData() != null)
					{
						// If node is folder, and it does not exist in skeleton,
						// mark it as
						// dirty and don't process nodes that belong to it
						if (node.getData() instanceof InventoryFolder && !(Items.containsKey(node.getData().UUID)))
						{
							dirty_folders.add(node.getData().UUID);
						}

						// Only add new items, this is most likely to be run at
						// login time before any inventory
						// nodes other than the root are populated. Don't add
						// non existing folders.
						if (!Items.containsKey(node.getData().UUID) && !dirty_folders.contains(pnode.getData().UUID)
								&& !(node.getData() instanceof InventoryFolder))
						{
							Items.put(node.getData().UUID, node);
							node.setParent(pnode); // Update this node with its
													// parent
							pnode.getNodes().put(node.getData().UUID, node); // Add
																				// to
																				// the
																				// parents
																				// child
																				// list
							item_count++;
						}
					}
					del_nodes.add(node);
				}
			}

			if (del_nodes.isEmpty())
			{
				stuck++;
			}
			else
			{
				stuck = 0;
			}

			// Clean up processed nodes this loop around.
			for (InventoryNode node : del_nodes)
			{
				nodes.remove(node);
			}
			del_nodes.clear();
		}
		Logger.Log("Reassembled " + item_count + " items from inventory cache file", LogLevel.Info);
		return item_count;
	}

	// #region Operators

	/**
	 * By using the bracket operator on this class, the program can get the
	 * InventoryObject designated by the specified uuid. If the value for the
	 * corresponding UUID is null, the call is equivalent to a call to
	 * <code>RemoveNodeFor(this[uuid])</code>. If the value is non-null, it is
	 * equivalent to a call to <code>UpdateNodeFor(value)</code>, the uuid
	 * parameter is ignored.
	 * 
	 * @param uuid
	 *            The UUID of the InventoryObject to get or set, ignored if set
	 *            to non-null value.
	 * @return The InventoryObject corresponding to <code>uuid</code>.
	 */
	public final InventoryBase getItem(UUID uuid)
	{
		InventoryNode node = Items.get(uuid);
		return node.getData();
	}

	public final void setItem(UUID uuid, InventoryBase value)
	{
		if (value != null)
		{
			// Log a warning if there is a UUID mismatch, this will cause
			// problems
			if (value.UUID != uuid)
			{
				Logger.Log(
						"Inventory[uuid]: uuid " + uuid.toString() + " is not equal to value.UUID "
								+ value.UUID.toString(), LogLevel.Warning, Client);
			}
			updateNodeFor(value);
		}
		else
		{
			if (Items.containsKey(uuid))
			{
				InventoryNode node = Items.get(uuid);
				removeNodeFor(node.getData());
			}
		}
	}
	// #endregion Operators
}
