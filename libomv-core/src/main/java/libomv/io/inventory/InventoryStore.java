/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package libomv.io.inventory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import libomv.inventory.InventoryException;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode;
import libomv.io.GridClient;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.MultiMap;

/**
 * Responsible for maintaining the inventory structure. InventoryStore
 * constructs nodes and manages node children as is necessary to maintain a
 * coherent hierarchy. Other classes should not manipulate or create
 * InventoryNodes explicitly. When a node's parent changes (when a folder is
 * moved, for example) simply pass InventoryStore.add(parentID, node) the
 * updated InventoryNode without changing the parentID yet and it will make the
 * appropriate changes to its internal representation.
 *
 * We use a lazy tree linking approach in order to handle nodes that arrive
 * before their parent has arrived. Each node also has a storage for the
 * parentID besides the direct reference to its parent. When inserting a new
 * node into the tree we attempt to lookup the parent folder and store its
 * reference in our node, but don't throw an exception if that fails. Instead we
 * store the node and it's parentID in a list of unresolved nodes. Whenever a
 * new node arrives, we also check this list if it has any nodes referring to us
 * and in that case append it as a child to our node. When inserting a coherent
 * list of tree nodes in that way we should end up with no nodes left in that
 * list at the end.
 */
public class InventoryStore extends InventoryFolder {
	private static final Logger logger = Logger.getLogger(InventoryStore.class);

	private static final long serialVersionUID = 1L;

	public class InventoryObjectUpdatedCallbackArgs implements CallbackArgs {
		private final InventoryNode oldObject;
		private final InventoryNode newObject;

		public InventoryObjectUpdatedCallbackArgs(InventoryNode oldObject, InventoryNode newObject) {
			this.oldObject = oldObject;
			this.newObject = newObject;
		}

		public final InventoryNode getOldObject() {
			return oldObject;
		}

		public final InventoryNode getNewObject() {
			return newObject;
		}

	}

	public class InventoryObjectRemovedCallbackArgs implements CallbackArgs {
		private final InventoryNode obj;

		public InventoryObjectRemovedCallbackArgs(InventoryNode obj) {
			this.obj = obj;
		}

		public final InventoryNode getObj() {
			return obj;
		}

	}

	public class InventoryObjectAddedCallbackArgs implements CallbackArgs {
		private final InventoryNode obj;

		public InventoryObjectAddedCallbackArgs(InventoryNode obj) {
			this.obj = obj;
		}

		public final InventoryNode getObj() {
			return obj;
		}

	}

	public CallbackHandler<InventoryObjectUpdatedCallbackArgs> onInventoryObjectUpdated = new CallbackHandler<>();

	public CallbackHandler<InventoryObjectRemovedCallbackArgs> onInventoryObjectRemoved = new CallbackHandler<>();

	public CallbackHandler<InventoryObjectAddedCallbackArgs> onInventoryObjectAdded = new CallbackHandler<>();

	private GridClient client;

	private Map<UUID, InventoryItem> items;
	private Map<UUID, InventoryFolder> folders;
	private MultiMap<UUID, InventoryNode> unresolved;

	private UUID inventoryID;
	private UUID libraryID;

	public InventoryStore(GridClient client) {
		this(client, client.agent.getAgentID());
	}

	public InventoryStore(GridClient client, UUID owner) {
		super(UUID.ZERO, UUID.ZERO, owner);
		this.client = client;

		if (owner == null || owner.equals(UUID.ZERO)) {
			logger.warn(GridClient.Log("Inventory owned by nobody!", client));
		}
		this.items = new HashMap<>();
		this.folders = new HashMap<>();
		this.unresolved = new MultiMap<>();

		this.folders.put(UUID.ZERO, this);

		this.name = "Root";
		this.preferredType = FolderType.Root;
	}

	// The root folder of the avatars inventory
	public final InventoryFolder getInventoryFolder() {
		if (children != null) {
			Iterator<InventoryNode> iter = children.iterator();
			while (iter.hasNext()) {
				InventoryFolder node = (InventoryFolder) iter.next();
				if (node.itemID.equals(inventoryID))
					return node;
			}
		}
		return null;
	}

	public final void setInventoryFolder(UUID folderID) {
		inventoryID = folderID;
	}

	// The root folder of the default shared library
	public final InventoryFolder getLibraryFolder() {
		if (children != null) {
			Iterator<InventoryNode> iter = children.iterator();
			while (iter.hasNext()) {
				InventoryFolder node = (InventoryFolder) iter.next();
				if (node.itemID.equals(libraryID))
					return node;
			}
		}
		return null;
	}

	public final void setLibraryFolder(UUID folderID, UUID ownerID) {
		libraryID = folderID;
	}

	/**
	 * Used to find out if Inventory contains the InventoryItem specified by
	 * <code>uuid</code>.
	 *
	 * @param uuid
	 *            The UUID to check.
	 * @return true if inventory contains an item with uuid, false otherwise
	 */
	public final boolean containsItem(UUID uuid) {
		return items.containsKey(uuid);
	}

	/**
	 * Used to find out if Inventory contains the InventoryFolder specified by
	 * <code>uuid</code>.
	 *
	 * @param uuid
	 *            The UUID to check.
	 * @return true if inventory contains an item with uuid, false otherwise
	 */
	public final boolean containsFolder(UUID uuid) {
		return folders.containsKey(uuid);
	}

	public final boolean contains(InventoryNode node) {
		if (node.getType() == InventoryType.Folder) {
			return folders.containsKey(node.itemID);
		}
		return items.containsKey(node.itemID);
	}

	/**
	 * Returns the item with the specified id
	 *
	 * @param uuid
	 *            The UUID of the InventoryItem to get
	 * @return The InventoryItem corresponding to <code>uuid</code>.
	 */
	public InventoryItem getItem(UUID uuid) {
		return items.get(uuid);
	}

	/**
	 * Returns the folder with the specified id
	 *
	 * @param uuid
	 *            The UUID of the InventoryFolder to get
	 * @return The InventoryFolder corresponding to <code>uuid</code>.
	 */
	protected InventoryFolder getFolder(UUID uuid) {
		return folders.get(uuid);
	}

	/**
	 * Returns either a folder or item with the specified id
	 *
	 * @param uuid
	 *            The UUID of the InventoryNode to get
	 * @return The InventoryNode corresponding to <code>uuid</code>.
	 */
	protected final InventoryNode getNode(UUID uuid) {
		if (folders.containsKey(uuid))
			return folders.get(uuid);
		return items.get(uuid);
	}

	/**
	 * Adds a node to a parent if it can be resolved and stores the node in the
	 * according HashMap for later reference and fast lookup by its ID, first
	 * removing it from its previous parent if present and not equal to the new
	 * parent.
	 *
	 * @param node
	 *            The node whose parentID to return
	 */
	protected final void add(InventoryNode node) {
		synchronized (folders) {
			// Check if there are any unresolved nodes referring to us
			if (node.getType() == InventoryType.Folder && unresolved.containsKey(node.itemID)) {
				InventoryFolder parent = (InventoryFolder) node;
				Iterator<InventoryNode> iter = unresolved.iterator(node.itemID);
				while (iter.hasNext()) {
					InventoryNode n = iter.next();
					n.parent = parent;
					if (parent.children == null)
						parent.children = new ArrayList<>(1);
					parent.children.add(n);
					iter.remove();
				}
			}

			// Check if there was already a parent and if it matches with the new parent
			if (node.parent != null && node.parent.itemID != null && !node.parent.itemID.equals(node.parentID)) {
				// This is a reassignment of the parent so remove us from the previous parent
				if (node.parent.children != null)
					node.parent.children.remove(node);
				node.parent = null;
			}

			if (node.parent == null) {
				// Link this node to its parent if it already exists, otherwise put it in the
				// unresolved list
				if (folders.containsKey(node.parentID)) {
					node.parent = folders.get(node.parentID);
					if (node.parent.children == null)
						node.parent.children = new ArrayList<>(1);

					if (!node.parent.children.contains(node)) {
						node.parent.children.add(node);
					}

					if (node.getType() == InventoryType.Folder) {
						folders.put(node.itemID, (InventoryFolder) node);
					} else {
						items.put(node.itemID, (InventoryItem) node);
					}
				} else {
					unresolved.put(node.parentID, node);
				}
			}
		}
	}

	/**
	 * Convenience method
	 *
	 * @param parentID
	 *            The parent ID of this node to assign the node to
	 * @param node
	 *            The node
	 */
	protected final void add(UUID parentID, InventoryNode node) {
		node.parentID = parentID;
		add(node);
	}

	/**
	 * Removes the InventoryNode and all related node data from the Inventory
	 *
	 * @param item
	 *            The InventoryNode to remove.
	 */
	protected final void remove(InventoryNode node) {
		synchronized (folders) {
			if (node.getType() == InventoryType.Folder) {
				InventoryFolder folder = (InventoryFolder) node;
				if (folder.children != null) {
					Iterator<InventoryNode> iter = folder.children.iterator();
					while (iter.hasNext())
						remove(iter.next());
				}
				folders.remove(node.itemID);
			} else {
				items.remove(node.itemID);
			}

			if (node.parent != null && node.parent.children != null)
				node.parent.children.remove(node);
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
	protected final List<InventoryNode> getContents(UUID folder) throws InventoryException {
		synchronized (folders) {
			if (!folders.containsKey(folder)) {
				throw new InventoryException("Unknown folder: " + folder);
			}
			return folders.get(folder).getContents();
		}
	}

	protected final void printUnresolved() {
		if (unresolved.valueCount() > 0)
			System.out.println(unresolved.toString());
	}

	protected final void resolveList() {
		synchronized (folders) {
			Set<Entry<UUID, List<InventoryNode>>> set = unresolved.entrySet();
			while (set.iterator().hasNext()) {
				Entry<UUID, List<InventoryNode>> e = set.iterator().next();
				if (folders.containsKey(e.getKey())) {
					InventoryFolder parent = folders.get(e.getKey());
					Iterator<InventoryNode> iter = unresolved.get(e.getKey()).iterator();
					while (iter.hasNext()) {
						InventoryNode n = iter.next();
						n.parent = parent;
						if (parent.children == null)
							parent.children = new ArrayList<>(1);
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
	 * @param filename
	 *            Name of the cache file to save to
	 */
	public final void saveToDisk(String filename) throws IOException {
		resolveList();
		FileOutputStream fos = new FileOutputStream(filename);
		try {
			ObjectOutputStream out = new ObjectOutputStream(fos);
			try {
				logger.info("Caching inventory to " + filename);
				out.writeLong(serialVersionUID);
				out.writeObject(ownerID);
				super.writeObject(out);
			} finally {
				out.close();
			}
		} catch (Throwable ex) {
			logger.error(GridClient.Log("Error saving inventory cache to disk :" + ex.getMessage(), client), ex);
		} finally {
			fos.close();
		}
	}

	/**
	 * Loads in inventory cache file into the inventory structure. Note only valid
	 * to call after login has been successful.
	 *
	 * @param filename
	 *            Name of the cache file to load
	 * @return The number of inventory items sucessfully reconstructed into the
	 *         inventory node tree
	 */
	public final int restoreFromDisk(String filename) throws IOException {
		items.clear();
		folders.clear();
		unresolved.clear();

		FileInputStream fis = new FileInputStream(filename);
		try {
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				if (serialVersionUID != in.readLong())
					throw new InvalidObjectException("InventoryRoot serial version mismatch");
				ownerID = (UUID) in.readObject();
				super.readObject(in);
			} finally {
				in.close();
			}
		} catch (Throwable ex) {
			logger.error(GridClient.Log("Error accessing inventory cache file :" + ex.getMessage(), client), ex);
		} finally {
			fis.close();
		}

		Stack<InventoryFolder> stack = new Stack<>();
		stack.push(this);

		int folderCount = 0;
		int itemCount = 0;
		while (stack.size() > 0) {
			InventoryFolder folder = stack.pop();
			Iterator<InventoryNode> iter = folder.children.iterator();
			while (iter.hasNext()) {
				InventoryNode node = iter.next();
				if (node.getType() == InventoryType.Folder) {
					stack.push((InventoryFolder) node);
					folders.put(node.itemID, (InventoryFolder) node);
					folderCount++;
				} else {
					items.put(node.itemID, (InventoryItem) node);
					itemCount++;
				}
			}
		}
		logger.info(GridClient
				.Log("Read " + folderCount + " folders and " + itemCount + " items from inventory cache file", client));
		return itemCount;
	}
}
