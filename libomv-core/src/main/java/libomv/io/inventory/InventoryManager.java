/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpResponseException;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDUUID;
import libomv.assets.AssetWearable.WearableType;
import libomv.capabilities.CapsMessage.BulkUpdateInventoryMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.CopyInventoryFromNotecardMessage;
import libomv.capabilities.CapsMessage.ScriptRunningReplyMessage;
import libomv.capabilities.CapsMessage.UpdateScriptTaskUpdateMessage;
import libomv.capabilities.IMessage;
import libomv.inventory.InventoryException;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.io.GridClient;
import libomv.io.LibSettings;
import libomv.io.SimulatorManager;
import libomv.io.capabilities.CapsCallback;
import libomv.io.capabilities.CapsClient;
import libomv.model.Inventory;
import libomv.model.Simulator;
import libomv.model.agent.InstantMessageCallbackArgs;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.asset.AssetType;
import libomv.model.asset.XferDownload;
import libomv.model.inventory.DeRezDestination;
import libomv.model.inventory.FindObjectByPathReplyCallbackArgs;
import libomv.model.inventory.FolderUpdatedCallbackArgs;
import libomv.model.inventory.InventoryObjectOfferedCallbackArgs;
import libomv.model.inventory.InventorySortOrder;
import libomv.model.inventory.InventoryUploadedAssetCallbackArgs;
import libomv.model.inventory.ItemCopiedCallbackArgs;
import libomv.model.inventory.ItemCreatedCallbackArgs;
import libomv.model.inventory.ItemCreatedFromAssetCallbackArgs;
import libomv.model.inventory.ItemReceivedCallbackArgs;
import libomv.model.inventory.SaveAssetToInventoryCallbackArgs;
import libomv.model.inventory.ScriptRunningReplyCallbackArgs;
import libomv.model.inventory.ScriptUpdatedCallbackArgs;
import libomv.model.inventory.TaskInventoryReplyCallbackArgs;
import libomv.model.inventory.TaskItemReceivedCallbackArgs;
import libomv.model.login.LoginProgressCallbackArgs;
import libomv.model.login.LoginResponseData;
import libomv.model.login.LoginStatus;
import libomv.model.object.SaleType;
import libomv.packets.BulkUpdateInventoryPacket;
import libomv.packets.CopyInventoryFromNotecardPacket;
import libomv.packets.CopyInventoryItemPacket;
import libomv.packets.CreateInventoryFolderPacket;
import libomv.packets.CreateInventoryItemPacket;
import libomv.packets.DeRezObjectPacket;
import libomv.packets.FetchInventoryDescendentsPacket;
import libomv.packets.FetchInventoryPacket;
import libomv.packets.FetchInventoryReplyPacket;
import libomv.packets.GetScriptRunningPacket;
import libomv.packets.ImprovedInstantMessagePacket;
import libomv.packets.InventoryDescendentsPacket;
import libomv.packets.LinkInventoryItemPacket;
import libomv.packets.MoveInventoryFolderPacket;
import libomv.packets.MoveInventoryItemPacket;
import libomv.packets.MoveTaskInventoryPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PurgeInventoryDescendentsPacket;
import libomv.packets.RemoveInventoryObjectsPacket;
import libomv.packets.RemoveTaskInventoryPacket;
import libomv.packets.ReplyTaskInventoryPacket;
import libomv.packets.RequestTaskInventoryPacket;
import libomv.packets.RezObjectPacket;
import libomv.packets.RezRestoreToWorldPacket;
import libomv.packets.RezScriptPacket;
import libomv.packets.SaveAssetIntoInventoryPacket;
import libomv.packets.SetScriptRunningPacket;
import libomv.packets.UpdateCreateInventoryItemPacket;
import libomv.packets.UpdateInventoryFolderPacket;
import libomv.packets.UpdateInventoryItemPacket;
import libomv.packets.UpdateTaskInventoryPacket;
import libomv.types.PacketCallback;
import libomv.types.Permissions;
import libomv.types.Permissions.PermissionMask;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

/* Tools for dealing with agents inventory */
public class InventoryManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(InventoryManager.class);

	/* Partial mapping of AssetTypes to folder names */
	private static final String[] NEW_FOLDER_NAMES = new String[] { "Textures", // 0
			"Sounds", // 1
			"Calling Cards", // 2
			"Landmarks", // 3
			Helpers.EmptyString, // 4
			"Clothing", // 5
			"Objects", // 6
			"Notecards", // 7
			"My Inventory", // 8
			Helpers.EmptyString, // 9
			"Scripts", // 10
			Helpers.EmptyString, // 11
			Helpers.EmptyString, // 12
			"Body Parts", // 13
			"Trash", // 14
			"Photo Album", // 15
			"Lost And Found", // 16
			Helpers.EmptyString, // 17
			Helpers.EmptyString, // 18
			Helpers.EmptyString, // 19
			"Animations", // 20
			"Gestures", // 21
			Helpers.EmptyString, // 22
			"Favorites", // 23
			Helpers.EmptyString, // 24
			Helpers.EmptyString, // 25
			"New Folder", // 26
			"New Folder", // 27
			"New Folder", // 28
			"New Folder", // 29
			"New Folder", // 30
			"New Folder", // 31
			"New Folder", // 32
			"New Folder", // 33
			"New Folder", // 34
			"New Folder", // 35
			"New Folder", // 36
			"New Folder", // 37
			"New Folder", // 38
			"New Folder", // 39
			"New Folder", // 40
			"New Folder", // 41
			"New Folder", // 42
			"New Folder", // 43
			"New Folder", // 44
			"New Folder", // 45
			"Current Outfit", // 46
			"New Outfit", // 47
			"My Outfits", // 48
			"Meshes", // 49
			"Received Items", // 50
			"Merchant Outbox", // 51
			"Basic Root", // 52
			"Marketplace Listings", // 53
			"New Stock", // 54
	};

	protected final class InventorySearch {
		public UUID folder;
		public UUID owner;
		public String[] path;
		public int level;
	}

	private class Self_InstantMessage implements Callback<InstantMessageCallbackArgs> {
		@Override
		public boolean callback(InstantMessageCallbackArgs e) {
			// TODO: MainAvatar.InstantMessageDialog.GroupNotice can also be an
			// inventory offer, should we handle it here?

			if (onInventoryObjectOffered != null && (e.getIM().dialog == InstantMessageDialog.InventoryOffered
					|| e.getIM().dialog == InstantMessageDialog.TaskInventoryOffered)) {
				AssetType type = AssetType.Unknown;
				UUID objectID = UUID.ZERO;
				boolean fromTask = false;

				if (e.getIM().dialog == InstantMessageDialog.InventoryOffered) {
					if (e.getIM().binaryBucket.length == 17) {
						type = AssetType.setValue(e.getIM().binaryBucket[0]);
						objectID = new UUID(e.getIM().binaryBucket, 1);
						fromTask = false;
					} else {
						logger.warn(GridClient.Log("Malformed inventory offer from agent", client));
						return false;
					}
				} else if (e.getIM().dialog == InstantMessageDialog.TaskInventoryOffered) {
					if (e.getIM().binaryBucket.length == 1) {
						type = AssetType.setValue(e.getIM().binaryBucket[0]);
						fromTask = true;
					} else {
						logger.warn(GridClient.Log("Malformed inventory offer from object", client));
						return false;
					}
				}

				// Fire the callback
				try {
					// Find the folder where this is going to go
					InventoryFolder parent = findFolderForType(type);
					ImprovedInstantMessagePacket imp = new ImprovedInstantMessagePacket();
					imp.AgentData.AgentID = client.agent.getAgentID();
					imp.AgentData.SessionID = client.agent.getSessionID();
					imp.MessageBlock.FromGroup = false;
					imp.MessageBlock.ToAgentID = e.getIM().fromAgentID;
					imp.MessageBlock.Offline = 0;
					imp.MessageBlock.ID = e.getIM().imSessionID;
					imp.MessageBlock.Timestamp = 0;
					imp.MessageBlock.setFromAgentName(Helpers.stringToBytes(client.agent.getName()));
					imp.MessageBlock.setMessage(Helpers.EmptyBytes);
					imp.MessageBlock.ParentEstateID = 0;
					imp.MessageBlock.RegionID = UUID.ZERO;
					imp.MessageBlock.Position = client.agent.getAgentPosition();

					InventoryObjectOfferedCallbackArgs args = new InventoryObjectOfferedCallbackArgs(e.getIM(), type,
							objectID, fromTask, parent.itemID);

					onInventoryObjectOffered.dispatch(args);

					if (args.getAccept()) {
						// Accept the inventory offer
						switch (e.getIM().dialog) {
						case InventoryOffered:
							imp.MessageBlock.Dialog = InstantMessageDialog.InventoryAccepted.getValue();
							break;
						case TaskInventoryOffered:
							imp.MessageBlock.Dialog = InstantMessageDialog.TaskInventoryAccepted.getValue();
							break;
						case GroupNotice:
							imp.MessageBlock.Dialog = InstantMessageDialog.GroupNoticeInventoryAccepted.getValue();
							break;
						default:
							break;
						}
						imp.MessageBlock.setBinaryBucket(args.getFolderID().getBytes());
					} else {
						// Decline the inventory offer
						switch (e.getIM().dialog) {
						case InventoryOffered:
							imp.MessageBlock.Dialog = InstantMessageDialog.InventoryDeclined.getValue();
							break;
						case TaskInventoryOffered:
							imp.MessageBlock.Dialog = InstantMessageDialog.TaskInventoryDeclined.getValue();
							break;
						case GroupNotice:
							imp.MessageBlock.Dialog = InstantMessageDialog.GroupNoticeInventoryDeclined.getValue();
							break;
						default:
							break;
						}
						imp.MessageBlock.setBinaryBucket(Helpers.EmptyBytes);
					}

					e.getSimulator().sendPacket(imp);
				} catch (Exception ex) {
					logger.error(GridClient.Log(ex.getMessage(), client), ex);
				}
			}
			return false;
		}
	}

	private class CreateItemFromAssetResponse implements FutureCallback<OSD> {
		private final Callback<ItemCreatedFromAssetCallbackArgs> callback;
		private final byte[] itemData;
		private final long timeout;
		private final OSDMap request;

		public CreateItemFromAssetResponse(Callback<ItemCreatedFromAssetCallbackArgs> callback, byte[] data,
				long timeout, OSDMap query) {
			this.callback = callback;
			this.itemData = data;
			this.timeout = timeout;
			this.request = query;
		}

		@Override
		public void completed(OSD result) {
			if (result.getType() == OSD.OSDType.Unknown) {
				failed(new Exception("Failed to parse asset and item UUIDs"));
			}

			OSDMap contents = (OSDMap) result;

			String status = contents.get("state").asString().toLowerCase();

			if (status.equals("upload")) {
				String uploadURL = contents.get("uploader").asString();

				logger.debug("CreateItemFromAsset: uploading to " + uploadURL);

				// This makes the assumption that all uploads go to CurrentSim,
				// to avoid the problem of HttpRequestState not knowing anything
				// about simulators
				try {
					CapsClient upload = new CapsClient(client, CapsEventType.UploadObjectAsset.toString());
					upload.executeHttpPost(new URI(uploadURL), itemData, "application/octet-stream", null,
							new CreateItemFromAssetResponse(callback, itemData, timeout, request), timeout);
				} catch (Exception ex) {
					failed(ex);
				}
			} else if (status.equals("complete")) {
				logger.debug("CreateItemFromAsset: completed");

				if (contents.containsKey("new_inventory_item") && contents.containsKey("new_asset")) {
					UUID item = contents.get("new_inventory_item").asUUID();
					UUID asset = contents.get("new_asset").asUUID();
					// Request full update on the item in order to update the
					// local store
					try {
						requestFetchInventory(item, client.agent.getAgentID());
					} catch (Exception ex) {
					}

					if (callback != null)
						callback.callback(new ItemCreatedFromAssetCallbackArgs(true, Helpers.EmptyString, item, asset));
				} else {
					if (callback != null)
						callback.callback(new ItemCreatedFromAssetCallbackArgs(false,
								"Failed to parse asset and item UUIDs", UUID.ZERO, UUID.ZERO));
				}
			} else {
				// Failure
				if (callback != null)
					callback.callback(new ItemCreatedFromAssetCallbackArgs(false, status, UUID.ZERO, UUID.ZERO));
			}
		}

		@Override
		public void failed(Exception ex) {
			if (callback != null)
				callback.callback(new ItemCreatedFromAssetCallbackArgs(false, ex.getMessage(), UUID.ZERO, UUID.ZERO));
		}

		@Override
		public void cancelled() {
			if (callback != null)
				callback.callback(
						new ItemCreatedFromAssetCallbackArgs(false, "Operation canceled", UUID.ZERO, UUID.ZERO));
		}
	}

	private class Network_OnLoginProgress implements Callback<LoginProgressCallbackArgs> {
		@Override
		public boolean callback(LoginProgressCallbackArgs e) {
			if (e.getStatus() == LoginStatus.Success) {
				LoginResponseData replyData = e.getReply();
				// Initialize the store here so we know who owns it:
				store = new InventoryStore(client);

				synchronized (store) {
					logger.debug(
							GridClient.Log("Setting InventoryRoot to " + replyData.inventoryRoot.toString(), client));
					store.setInventoryFolder(replyData.inventoryRoot);
					for (int i = 0; i < replyData.inventorySkeleton.length; i++) {
						store.add(replyData.inventorySkeleton[i]);
					}

					logger.debug(GridClient.Log("Setting LibraryRoot to " + replyData.libraryRoot.toString(), client));
					store.setLibraryFolder(replyData.libraryRoot, replyData.libraryOwner);
					for (int i = 0; i < replyData.librarySkeleton.length; i++) {
						store.add(replyData.librarySkeleton[i]);
					}
				}
				store.printUnresolved();
			}
			return false;
		}
	}

	public class UploadInventoryAssetComplete implements FutureCallback<OSD> {
		private final Callback<InventoryUploadedAssetCallbackArgs> callback;
		private final byte[] itemData;
		private final UUID assetID;

		public UploadInventoryAssetComplete(Callback<InventoryUploadedAssetCallbackArgs> callback, byte[] itemData,
				UUID assetID) {
			this.callback = callback;
			this.itemData = itemData;
			this.assetID = assetID;
		}

		@Override
		public void completed(OSD result) {
			OSDMap contents = (OSDMap) ((result instanceof OSDMap) ? result : null);
			if (contents != null) {
				String status = contents.get("state").asString();
				if (status.equals("upload")) {
					URI uploadURL = contents.get("uploader").asUri();
					if (uploadURL != null) {
						// This makes the assumption that all uploads go to
						// CurrentSim, to avoid the problem of HttpRequestState
						// not knowing anything about simulators
						try {
							CapsClient upload = new CapsClient(client, CapsEventType.UploadObjectAsset.toString());
							upload.executeHttpPost(uploadURL, itemData, "application/octet-stream", null,
									new UploadInventoryAssetComplete(callback, itemData, assetID),
									client.settings.CAPS_TIMEOUT);
						} catch (Exception ex) {
							failed(ex);
						}
					} else {
						if (callback != null)
							callback.callback(new InventoryUploadedAssetCallbackArgs(false, "Missing uploader URL",
									UUID.ZERO, UUID.ZERO));
					}
				} else if (status.equals("complete")) {
					if (contents.containsKey("new_asset")) {
						UUID newAsset = contents.get("new_asset").asUUID();
						// Request full item update so we keep store in sync
						try {
							requestFetchInventory(assetID, newAsset);
						} catch (Exception ex) {
						}

						if (callback != null)
							callback.callback(new InventoryUploadedAssetCallbackArgs(true, Helpers.EmptyString, assetID,
									newAsset));
					} else {
						if (callback != null)
							callback.callback(new InventoryUploadedAssetCallbackArgs(false,
									"Failed to parse asset and item UUIDs", UUID.ZERO, UUID.ZERO));
					}
				} else {
					if (callback != null)
						callback.callback(new InventoryUploadedAssetCallbackArgs(false, status, UUID.ZERO, UUID.ZERO));
				}
			} else {
				if (callback != null)
					callback.callback(new InventoryUploadedAssetCallbackArgs(false, "Unrecognized or empty response",
							UUID.ZERO, UUID.ZERO));
			}
		}

		@Override
		public void failed(Exception ex) {
			if (callback != null) {
				String message;
				if (ex instanceof HttpResponseException)
					message = String.format("HTTP Status: %d, %s", ((HttpResponseException) ex).getStatusCode(),
							ex.getMessage());
				else
					message = ex.getMessage();

				callback.callback(new InventoryUploadedAssetCallbackArgs(false, message, UUID.ZERO, UUID.ZERO));
			}
		}

		@Override
		public void cancelled() {
			if (callback != null)
				callback.callback(
						new InventoryUploadedAssetCallbackArgs(false, "Operation cancelled", UUID.ZERO, UUID.ZERO));
		}
	}

	public class UpdateScriptAgentInventoryResponse implements FutureCallback<OSD> {
		private final Callback<ScriptUpdatedCallbackArgs> callback;
		private final byte[] itemData;
		private final UUID scriptID;

		public UpdateScriptAgentInventoryResponse(Callback<ScriptUpdatedCallbackArgs> callback, byte[] itemData,
				UUID scriptID) {
			this.callback = callback;
			this.itemData = itemData;
			this.scriptID = scriptID;
		}

		@Override
		public void completed(OSD result) {
			OSDMap contents = (OSDMap) result;
			String status = contents.get("state").asString();
			if (status.equals("upload")) {
				String uploadURL = contents.get("uploader").asString();

				try {
					CapsClient upload = new CapsClient(client, CapsEventType.UpdateScriptAgent.toString());
					upload.executeHttpPost(new URI(uploadURL), itemData, "application/octet-stream", null,
							new UpdateScriptAgentInventoryResponse(callback, itemData, scriptID),
							client.settings.CAPS_TIMEOUT);
				} catch (Exception ex) {
					failed(ex);
				}
			} else if (status.equals("complete")) {
				if (contents.containsKey("new_asset")) {
					UUID newAsset = contents.get("new_asset").asUUID();
					// Request full item update so we keep store in sync
					try {
						requestFetchInventory(scriptID, newAsset);
					} catch (Exception ex) {
					}

					List<String> compileErrors = null;

					if (contents.containsKey("errors")) {
						OSDArray errors = (OSDArray) contents.get("errors");
						compileErrors = new ArrayList<>(errors.size());

						for (int i = 0; i < errors.size(); i++) {
							compileErrors.add(errors.get(i).asString());
						}
					}
					if (callback != null)
						callback.callback(new ScriptUpdatedCallbackArgs(true, status,
								contents.get("compiled").asBoolean(), compileErrors, scriptID, newAsset));
				} else {
					if (callback != null)
						callback.callback(new ScriptUpdatedCallbackArgs(false, "Failed to parse asset UUID", false,
								null, UUID.ZERO, UUID.ZERO));
				}
			}
		}

		@Override
		public void failed(Exception ex) {
			if (callback != null)
				callback.callback(
						new ScriptUpdatedCallbackArgs(false, ex.getMessage(), false, null, UUID.ZERO, UUID.ZERO));
		}

		@Override
		public void cancelled() {
			if (callback != null)
				callback.callback(
						new ScriptUpdatedCallbackArgs(false, "Operation cancelled", false, null, UUID.ZERO, UUID.ZERO));
		}
	}

	public CallbackHandler<ItemReceivedCallbackArgs> onItemReceived = new CallbackHandler<>();
	public CallbackHandler<FolderUpdatedCallbackArgs> onFolderUpdated = new CallbackHandler<>();
	public CallbackHandler<InventoryObjectOfferedCallbackArgs> onInventoryObjectOffered = new CallbackHandler<>();
	public CallbackHandler<TaskItemReceivedCallbackArgs> onTaskItemReceived = new CallbackHandler<>();
	public CallbackHandler<FindObjectByPathReplyCallbackArgs> onFindObjectByPathReply = new CallbackHandler<>();
	public CallbackHandler<TaskInventoryReplyCallbackArgs> onTaskInventoryReply = new CallbackHandler<>();
	public CallbackHandler<SaveAssetToInventoryCallbackArgs> onSaveAssetToInventory = new CallbackHandler<>();
	public CallbackHandler<ScriptRunningReplyCallbackArgs> onScriptRunningReply = new CallbackHandler<>();
	public CallbackHandler<ItemCreatedCallbackArgs> onItemCreatedCallback = new CallbackHandler<>();
	public CallbackHandler<ItemCreatedFromAssetCallbackArgs> onItemCreatedFromAssetCallback = new CallbackHandler<>();
	public CallbackHandler<ItemCopiedCallbackArgs> onItemCopiedCallback = new CallbackHandler<>();

	private Map<Integer, Callback<ItemCreatedCallbackArgs>> itemCreatedCallbacks = new Hashtable<>();
	private Map<Integer, Callback<ItemCopiedCallbackArgs>> itemCopiedCallbacks = new Hashtable<>();
	private Map<Integer, InventoryType> itemInventoryTypeRequest = new Hashtable<>();

	private Object callbacksLock = new Object();
	private int callbackPos;

	private GridClient client;
	private InventoryStore store;
	// private Random _RandNumbers = new Random();
	private List<InventorySearch> searches = new ArrayList<>();

	private Callback<InstantMessageCallbackArgs> instantMessageCallback;
	private Callback<LoginProgressCallbackArgs> loginProgressCallback;

	/**
	 * Default constructor
	 *
	 * @param client
	 *            Reference to the GridClient object
	 */
	public InventoryManager(GridClient client) {
		this.client = client;

		// Watch for inventory given to us through instant message
		this.instantMessageCallback = new Self_InstantMessage();
		this.client.agent.onInstantMessage.add(instantMessageCallback, false);

		// Register extra parameters with login and parse the inventory data
		// that comes back
		this.loginProgressCallback = new Network_OnLoginProgress();
		this.client.login.registerLoginProgressCallback(loginProgressCallback, new String[] { "inventory-root",
				"inventory-skeleton", "inventory-lib-root", "inventory-lib-owner", "inventory-skel-lib" }, false);

		this.client.network.registerCallback(PacketType.UpdateCreateInventoryItem, this);
		this.client.network.registerCallback(PacketType.SaveAssetIntoInventory, this);
		this.client.network.registerCallback(PacketType.BulkUpdateInventory, this);
		this.client.network.registerCallback(CapsEventType.BulkUpdateInventory, this);
		this.client.network.registerCallback(PacketType.MoveInventoryItem, this);
		this.client.network.registerCallback(PacketType.InventoryDescendents, this);
		this.client.network.registerCallback(PacketType.FetchInventoryReply, this);
		this.client.network.registerCallback(PacketType.ReplyTaskInventory, this);
		this.client.network.registerCallback(CapsEventType.ScriptRunningReply, this);
	}

	@Override
	protected void finalize() throws Throwable {
		client.agent.onInstantMessage.remove(instantMessageCallback);
		client.login.unregisterLoginProgressCallback(loginProgressCallback);
		super.finalize();
	}

	/* Get this agents Inventory data */
	public final InventoryFolder getRootNode(boolean library) {
		if (library)
			return store.getLibraryFolder();
		return store.getInventoryFolder();
	}

	public InventoryFolder getRoot() {
		return store;
	}

	public List<InventoryNode> getChildren(InventoryFolder folder) {
		if (folder != null)
			return folder.children;
		return null;
	}

	public void updateChild(InventoryFolder parent, Object element, Object value) {
		if (parent.children == null)
			parent.children = new ArrayList<>(1);
		int index = parent.children.indexOf(element);
		InventoryNode node = (InventoryNode) value;
		if (index >= 0) {
			parent.children.set(index, node);
		} else {
			parent.children.add(node);
		}
		node.parent = parent;
		node.parentID = parent.itemID;
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case UpdateCreateInventoryItem:
			handleUpdateCreateInventoryItem(packet, simulator);
			break;
		case SaveAssetIntoInventory:
			handleSaveAssetIntoInventory(packet, simulator);
			break;
		case BulkUpdateInventory:
			handleBulkUpdateInventory(packet, simulator);
			break;
		case MoveInventoryItem:
			handleMoveInventoryItem(packet, simulator);
			break;
		case InventoryDescendents:
			handleInventoryDescendents(packet, simulator);
			break;
		case FetchInventoryReply:
			handleFetchInventoryReply(packet, simulator);
			break;
		case ReplyTaskInventory:
			handleReplyTaskInventory(packet, simulator);
			break;
		default:
			break;
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case ScriptRunningReply:
			handleScriptRunningReply(message, simulator);
		case BulkUpdateInventory:
			handleBulkUpdateInventory(message, simulator);
		default:
			break;
		}
	}

	// #region Fetch

	/**
	 * Fetch an inventory item from the dataserver Items will also be sent to the
	 * {@link InventoryManager.OnItemReceived} event
	 *
	 * @param itemID
	 *            The items {@link UUID}
	 * @param ownerID
	 *            The item Owners {@link OpenMetaverse.UUID}
	 * @param timeout
	 *            a integer representing the number of milliseconds to wait for
	 *            results
	 * @return An {@link InventoryItem} object on success, or null if no item was
	 *         found
	 * @throws Exception
	 */
	public final InventoryItem fetchItem(final UUID itemID, UUID ownerID, int timeout) throws Exception {
		final TimeoutEvent<InventoryItem> fetchEvent = new TimeoutEvent<>();

		final class FetchedItemsCallback implements Callback<ItemReceivedCallbackArgs> {
			@Override
			public boolean callback(ItemReceivedCallbackArgs e) {
				if (e.getItem().itemID.equals(itemID)) {
					fetchEvent.set(e.getItem());
				}
				return false;
			}
		}

		Callback<ItemReceivedCallbackArgs> callback = new FetchedItemsCallback();

		onItemReceived.add(callback, true);
		requestFetchInventory(itemID, ownerID);
		InventoryItem item = fetchEvent.waitOne(timeout);
		onItemReceived.remove(callback);
		return item;
	}

	/**
	 * Request A single inventory item {@link InventoryManager.OnItemReceived}
	 *
	 * @param itemID
	 *            The items {@link OpenMetaverse.UUID}
	 * @param ownerID
	 *            The item Owners {@link OpenMetaverse.UUID}
	 * @throws Exception
	 */
	public void requestFetchInventory(UUID itemID, UUID ownerID) throws Exception {
		List<UUID> itemIDs = new ArrayList<>();
		List<UUID> ownerIDs = new ArrayList<>();
		itemIDs.add(itemID);
		ownerIDs.add(ownerID);
		requestFetchInventory(itemIDs, ownerIDs);
	}

	/**
	 * Request inventory items {@link InventoryManager.OnItemReceived}
	 *
	 * @param itemIDs
	 *            Inventory items to request
	 * @param ownerIDs
	 *            Owners of the inventory items
	 * @throws Exception
	 */
	public void requestFetchInventory(List<UUID> itemIDs, List<UUID> ownerIDs) throws Exception {
		if (itemIDs.size() != ownerIDs.size()) {
			throw new IllegalArgumentException("itemIDs and ownerIDs must contain the same number of entries");
		}

		if (client.settings.getBool(LibSettings.HTTP_INVENTORY)) {
			if (requestFetchInventoryCap(itemIDs, ownerIDs))
				return;
		}

		FetchInventoryPacket fetch = new FetchInventoryPacket();
		fetch.AgentData = fetch.new AgentDataBlock();
		fetch.AgentData.AgentID = client.agent.getAgentID();
		fetch.AgentData.SessionID = client.agent.getSessionID();

		fetch.InventoryData = new FetchInventoryPacket.InventoryDataBlock[itemIDs.size()];
		for (int i = 0; i < itemIDs.size(); i++) {
			fetch.InventoryData[i] = fetch.new InventoryDataBlock();
			fetch.InventoryData[i].ItemID = itemIDs.get(i);
			fetch.InventoryData[i].OwnerID = ownerIDs.get(i);
		}
		client.network.sendPacket(fetch);
		return;
	}

	/**
	 * Request inventory items over Caps {@link InventoryManager.OnItemReceived}
	 *
	 * @param itemIDs
	 *            Inventory items to request
	 * @param ownerIDs
	 *            Owners of the inventory items
	 * @return True if the request could be sent off
	 * @throws IOReactorException
	 */
	private boolean requestFetchInventoryCap(List<UUID> itemIDs, List<UUID> ownerIDs) throws IOReactorException {
		URI url = client.network.getCurrentSim().getCapabilityURI("FetchInventory2");
		if (url == null) {
			logger.warn(GridClient.Log("FetchInventory2 capability not available in the current sim", client));
			return false;
		}
		CapsClient request = new CapsClient(client, "FetchInventory2");

		final class CapsCallback implements FutureCallback<OSD> {
			@Override
			public void completed(OSD result) {
				OSDMap res = (OSDMap) result;
				OSDArray itemsOSD = (OSDArray) res.get("items");

				for (int i = 0; i < itemsOSD.size(); i++) {
					InventoryItem item = (InventoryItem) InventoryItem.fromOSD(itemsOSD.get(i));
					store.add(item.itemID, item);
					onItemReceived.dispatch(new ItemReceivedCallbackArgs(item));
				}
			}

			@Override
			public void cancelled() {
				logger.error(GridClient.Log("Failed getting data from FetchInventory2 capability.", client));
			}

			@Override
			public void failed(Exception ex) {
				logger.error(GridClient.Log("Failed getting data from FetchInventory2 capability.", client), ex);
			}
		}
		;

		OSDMap osdRequest = new OSDMap();
		osdRequest.put("agent_id", new OSDUUID(client.agent.getAgentID()));

		OSDArray items = new OSDArray(itemIDs.size());
		for (int i = 0; i < itemIDs.size(); i++) {
			OSDMap item = new OSDMap(2);
			item.put("item_id", new OSDUUID(itemIDs.get(i)));
			item.put("owner_id", new OSDUUID(ownerIDs.get(i)));
			items.add(item);
		}
		osdRequest.put("items", items);
		request.executeHttpPost(url, osdRequest, OSDFormat.Xml, new CapsCallback(), client.settings.CAPS_TIMEOUT);
		return true;
	}

	/**
	 * Get contents of a folder {@link InventoryManager.OnRequestFolderContents}
	 * InventoryFolder.DescendentCount will only be accurate if both folders and
	 * items are requested
	 *
	 * @param folder
	 *            The {@link UUID} of the folder to search
	 * @param owner
	 *            The {@link UUID} of the folders owner
	 * @param folders
	 *            true to retrieve folders
	 * @param items
	 *            true to retrieve items
	 * @param order
	 *            sort order to return results in
	 * @param timeout
	 *            a integer representing the number of milliseconds to wait for
	 *            results
	 * @return A list of inventory items matching search criteria within folder
	 * @throws Exception
	 */
	public final List<InventoryNode> folderContents(final UUID folderID, UUID ownerID, boolean folders, boolean items,
			byte order, int timeout) throws Exception {
		return folderContents(folderID, ownerID, folders, items, order, false, timeout);
	}

	/**
	 * Get contents of a folder {@link InventoryManager.OnRequestFolderContents}
	 * InventoryFolder.DescendentCount will only be accurate if both folders and
	 * items are requested
	 *
	 * @param folder
	 *            The {@link UUID} of the folder to search
	 * @param owner
	 *            The {@link UUID} of the folders owner
	 * @param folders
	 *            true to retrieve folders
	 * @param items
	 *            true to retrieve items
	 * @param order
	 *            sort order to return results in
	 * @param httpOnly
	 *            only use HTTP download
	 * @param timeout
	 *            a integer representing the number of milliseconds to wait for
	 *            results
	 * @return A list of inventory items matching search criteria within folder
	 * @throws Exception
	 */
	public final List<InventoryNode> folderContents(final UUID folderID, UUID ownerID, boolean folders, boolean items,
			byte order, boolean httpOnly, int timeout) throws Exception {
		final TimeoutEvent<List<InventoryNode>> fetchEvent = new TimeoutEvent<>();

		Callback<FolderUpdatedCallbackArgs> callback = new Callback<FolderUpdatedCallbackArgs>() {
			@Override
			public boolean callback(FolderUpdatedCallbackArgs e) {
				if (e.getFolderID().equals(folderID)) {
					synchronized (store) {
						fetchEvent.set(store.getFolder(folderID).getContents());
					}
				}
				return false;
			}
		};

		List<InventoryNode> contents = null;
		onFolderUpdated.add(callback, true);
		if (requestFolderContents(folderID, ownerID, folders, items, order, httpOnly)) {
			contents = fetchEvent.waitOne(timeout);
		}
		onFolderUpdated.remove(callback);
		return contents;
	}

	/**
	 * Request the contents of an inventory folder
	 * {@link InventoryManager.FolderContents}
	 *
	 * @param folderID
	 *            The folder to search
	 * @param ownerID
	 *            The folder owner {@link UUID}
	 * @param fetchFolders
	 *            true to return {@link InventoryManager.InventoryFolder}'s
	 *            contained in folder
	 * @param fetchItems
	 *            true to return {@link InventoryManager.InventoryItem}'s contained
	 *            in folder
	 * @param order
	 *            the sort order to return items in {@link InventorySortOrder}
	 * @return True if the request could be sent off
	 * @throws Exception
	 */
	public boolean requestFolderContents(UUID folderID, UUID ownerID, boolean fetchFolders, boolean fetchItems,
			byte order, boolean httpOnly) throws Exception {
		if (client.settings.getBool(LibSettings.HTTP_INVENTORY)) {
			String capability = client.agent.getAgentID().equals(ownerID) ? "FetchInventoryDescendents2"
					: "FetchLibDescendents2";
			URI url = client.network.getCapabilityURI(capability);
			if (url == null) {
				logger.warn(GridClient.Log(capability + " capability not available in the current sim", client));
			} else {
				List<InventoryNode> list = new ArrayList<>(1);
				InventoryNode node = new InventoryFolder(folderID);
				node.ownerID = ownerID;
				list.add(node);
				if (requestFolderContents(url, list, fetchFolders, fetchItems, order)) {
					return true;
				}
			}
		}

		if (!httpOnly) {
			FetchInventoryDescendentsPacket fetch = new FetchInventoryDescendentsPacket();
			fetch.AgentData.AgentID = client.agent.getAgentID();
			fetch.AgentData.SessionID = client.agent.getSessionID();

			fetch.InventoryData.FetchFolders = fetchFolders;
			fetch.InventoryData.FetchItems = fetchItems;
			fetch.InventoryData.FolderID = folderID;
			fetch.InventoryData.OwnerID = ownerID;
			fetch.InventoryData.SortOrder = order;

			client.network.sendPacket(fetch);
			return true;
		}
		onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(folderID, false));
		return false;
	}

	/**
	 * Request the contents of an inventory folder using HTTP capabilities
	 *
	 * @param capabilityUrl
	 *            The url to request the folder contents from
	 * @param folders
	 *            The folders to request the contents of
	 * @param fetchFolders
	 *            true to return {@link InventoryManager.InventoryFolder}'s
	 *            contained in folder
	 * @param fetchItems
	 *            true to return {@link InventoryManager.InventoryItem}'s contained
	 *            in folder
	 * @param order
	 *            the sort order to return items in {@link InventorySortOrder}
	 * @return True if the request could be sent off
	 *         {@link InventoryManager.FolderContents}
	 */
	public boolean requestFolderContents(URI capabilityUrl, final List<InventoryNode> batch, boolean fetchFolders,
			boolean fetchItems, byte order) {
		try {
			String cap = CapsEventType.FetchInventoryDescendents.toString();
			CapsClient request = new CapsClient(client, cap);

			final class CapsCallback implements FutureCallback<OSD> {
				@Override
				public void completed(OSD result) {
					try {
						OSDArray fetchedFolders = (OSDArray) ((OSDMap) result).get("folders");
						if (fetchedFolders != null) {
							for (int i = 0; i < fetchedFolders.size(); i++) {
								OSDMap res = (OSDMap) fetchedFolders.get(i);
								UUID parentID;
								UUID folderID = res.get("folder_id").asUUID();

								InventoryFolder fetchedFolder = safeCreateInventoryFolder(folderID,
										res.get("owner_id").asUUID());
								fetchedFolder.descendentCount = res.get("descendents").asInteger();
								fetchedFolder.version = res.get("version").asInteger();

								// Do we have any descendants
								if (fetchedFolder.descendentCount > 0) {
									// Fetch descendent folders
									OSDArray folders = (OSDArray) res.get("categories");
									if (folders != null) {
										for (int j = 0; j < folders.size(); j++) {
											OSDMap descFolder = (OSDMap) folders.get(j);
											parentID = descFolder.get("parent_id").asUUID();
											folderID = descFolder.get("category_id").asUUID();
											if (folderID == null) {
												folderID = descFolder.get("folder_id").asUUID();
											}
											InventoryFolder category = safeCreateInventoryFolder(folderID, parentID,
													descFolder.get("agent_id").asUUID());
											category.name = descFolder.get("name").asString();
											category.version = descFolder.get("version").asInteger();
											category.preferredType = FolderType
													.setValue(descFolder.get("type_default").asInteger());
										}
									}
									// Fetch descendent items
									OSDArray items = (OSDArray) res.get("items");
									if (items != null) {
										for (int j = 0; j < items.size(); j++) {
											InventoryNode item = InventoryItem.fromOSD(items.get(j));
											store.add(item);
										}
									}
								}
								onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(folderID, true));
							}
						}
					} catch (Exception ex) {
						logger.warn(GridClient.Log("Failed to fetch inventory descendants", client), ex);
						for (InventoryNode node : batch) {
							onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(node.itemID, false));
						}
					}
				}

				@Override
				public void cancelled() {
					logger.warn(GridClient.Log("Fetch inventory descendants canceled", client));
					for (InventoryNode node : batch) {
						onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(node.itemID, false));
					}
				}

				@Override
				public void failed(Exception ex) {
					logger.warn(GridClient.Log("Failed to fetch inventory descendants", client), ex);
					for (InventoryNode node : batch) {
						onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(node.itemID, false));
					}
				}
			}
			;

			// Construct request
			OSDArray requestedFolders = new OSDArray(batch.size());
			for (InventoryNode node : batch) {
				OSDMap requestedFolder = new OSDMap(1);
				requestedFolder.put("folder_id", OSD.fromUUID(node.itemID));
				requestedFolder.put("owner_id", OSD.fromUUID(node.ownerID));
				requestedFolder.put("fetch_folders", OSD.fromBoolean(fetchFolders));
				requestedFolder.put("fetch_items", OSD.fromBoolean(fetchItems));
				requestedFolder.put("sort_order", OSD.fromInteger(order));
				requestedFolders.add(requestedFolder);
			}
			OSDMap req = new OSDMap(1);
			req.put("folders", requestedFolders);

			request.executeHttpPost(capabilityUrl, req, OSDFormat.Xml, new CapsCallback(),
					client.settings.CAPS_TIMEOUT);
			return true;
		} catch (Exception ex) {
			logger.warn(GridClient.Log("Failed to fetch inventory descendants", client), ex);
			for (InventoryNode node : batch) {
				onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(node.itemID, false));
			}
			return false;
		}
	}

	// #endregion Fetch

	// #region Find

	/**
	 * Returns the UUID of the folder (category) that defaults to containing 'type'.
	 * The folder is not necessarily only for that type
	 *
	 * This will return the root folder if one does not exist
	 *
	 * @param type
	 * @return The UUID of the desired folder if found, the UUID of the RootFolder
	 *         if not found, or UUID.Zero on failure
	 * @throws InventoryException
	 */
	public final InventoryFolder findFolderForType(AssetType type) throws InventoryException {
		return findFolderForType(FolderType.setValue(type.getValue()));
	}

	public final InventoryFolder findFolderForType(FolderType type) throws InventoryException {
		if (store == null) {
			logger.error(GridClient.Log("Inventory is null, FindFolderForType() lookup cannot continue", client));
			return null;
		}

		synchronized (store) {
			// Loop through each top-level directory and check if PreferredType
			// matches the requested type

			Iterator<InventoryNode> iter = store.getInventoryFolder().children.iterator();
			while (iter.hasNext()) {
				InventoryNode node = iter.next();
				if (node.getType() == InventoryType.Folder) {
					InventoryFolder folder = (InventoryFolder) node;

					if (folder.preferredType == type) {
						return folder;
					}
				}
			}

			// No match found, return Root Folder ID
			return store.getInventoryFolder();
		}
	}

	/**
	 * Find an object in inventory using a specific path to search
	 *
	 * @param baseFolder
	 *            The folder to begin the search in
	 * @param inventoryOwner
	 *            The object owners {@link UUID}
	 * @param path
	 *            A string path to search
	 * @param timeout
	 *            milliseconds to wait for a reply
	 * @return Found items {@link UUID} or {@link UUID.Zero} if timeout occurs or
	 *         item is not found
	 * @throws Exception
	 */
	public final UUID findObjectByPath(UUID baseFolder, UUID inventoryOwner, final String path, int timeout)
			throws Exception {
		final TimeoutEvent<UUID> findEvent = new TimeoutEvent<>();

		Callback<FindObjectByPathReplyCallbackArgs> callback = new Callback<FindObjectByPathReplyCallbackArgs>() {
			@Override
			public boolean callback(FindObjectByPathReplyCallbackArgs e) {
				if (e.getPath() == path) {
					findEvent.set(e.getInventoryObjectID());
				}
				return false;
			}
		};

		onFindObjectByPathReply.add(callback, true);
		requestFindObjectByPath(baseFolder, inventoryOwner, path);
		UUID foundItem = findEvent.waitOne(timeout);
		onFindObjectByPathReply.remove(callback);

		return foundItem == null ? UUID.ZERO : foundItem;
	}

	/**
	 * Find inventory items by path
	 *
	 * @param baseFolder
	 *            The folder to begin the search in
	 * @param inventoryOwner
	 *            The object owners {@link UUID}
	 * @param path
	 *            A string path to search, folders/objects separated by a '/'
	 *            Results are sent to the
	 *            {@link InventoryManager.OnFindObjectByPath} event
	 */
	public final void requestFindObjectByPath(UUID baseFolder, UUID inventoryOwner, String path) throws Exception {
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("Empty path is not supported");
		}

		// Store this search
		InventorySearch search = new InventorySearch();
		search.folder = baseFolder;
		search.owner = inventoryOwner;
		search.path = path.split("/");
		search.level = 0;
		synchronized (searches) {
			searches.add(search);
		}

		// Start the search
		requestFolderContents(baseFolder, inventoryOwner, true, true, InventorySortOrder.ByName, false);
	}

	/**
	 * Search inventory Store object for an item or folder
	 *
	 * @param baseFolder
	 *            The folder to begin the search in
	 * @param path
	 *            An array which creates a path to search
	 * @param level
	 *            Number of levels below baseFolder to conduct searches
	 * @param firstOnly
	 *            if True, will stop searching after first match is found
	 * @return A list of inventory items found
	 * @throws InventoryException
	 */
	public final List<InventoryNode> localFind(UUID baseFolder, String[] path, int level, boolean firstOnly)
			throws InventoryException {
		List<InventoryNode> objects = new ArrayList<>();
		synchronized (store) {
			List<InventoryNode> contents = store.getContents(baseFolder);

			for (InventoryNode inv : contents) {
				if (inv.name.equals(path[level])) {
					if (level == path.length - 1) {
						objects.add(inv);
						if (firstOnly) {
							return objects;
						}
					} else if (inv instanceof InventoryFolder) {
						objects.addAll(localFind(inv.itemID, path, level + 1, firstOnly));
					}
				}
			}
			return objects;
		}
	}

	// #endregion Find

	// #region Move/Rename

	/**
	 * Move an inventory item or folder to a new location
	 *
	 * @param item
	 *            The {@link T:InventoryBase} item or folder to move
	 * @param newParent
	 *            The {@link T:InventoryFolder} to move item or folder to
	 * @throws Exception
	 */
	public final void move(InventoryNode item, InventoryFolder newParent) throws Exception {
		if (item instanceof InventoryFolder) {
			moveFolder(item.itemID, newParent.itemID);
		} else {
			moveItem(item.itemID, newParent.itemID);
		}
	}

	/**
	 * Move an inventory item or folder to a new location and change its name
	 *
	 * @param item
	 *            The {@link T:InventoryBase} item or folder to move
	 * @param newParent
	 *            The {@link T:InventoryFolder} to move item or folder to
	 * @param newName
	 *            The name to change the item or folder to
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void move(InventoryNode item, InventoryFolder newParent, String newName)
			throws UnsupportedEncodingException, Exception {
		if (item instanceof InventoryFolder) {
			moveFolder(item.itemID, newParent.itemID, newName);
		} else {
			moveItem(item.itemID, newParent.itemID, newName);
		}
	}

	/**
	 * Move and rename a folder
	 *
	 * @param folderID
	 *            The source folders {@link UUID}
	 * @param newparentID
	 *            The destination folders {@link UUID}
	 * @param newName
	 *            The name to change the folder to
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void moveFolder(UUID folderID, UUID newparentID, String newName)
			throws UnsupportedEncodingException, Exception {
		updateFolderProperties(folderID, newparentID, newName, FolderType.None);
	}

	/**
	 * Update folder properties
	 *
	 * @param folderID
	 *            {@link UUID} of the folder to update
	 * @param parentID
	 *            Sets folder's parent to {@link UUID}
	 * @param name
	 *            Folder name
	 * @param type
	 *            Folder type
	 * @throws Exception
	 */
	public final void updateFolderProperties(UUID folderID, UUID parentID, String name, FolderType type)
			throws Exception {
		synchronized (store) {
			if (store.containsFolder(folderID)) {
				InventoryFolder inv = store.getFolder(folderID);
				inv.name = name;
				inv.preferredType = type;
				store.add(parentID, inv);
			}
		}

		UpdateInventoryFolderPacket invFolder = new UpdateInventoryFolderPacket();
		invFolder.AgentData.AgentID = client.agent.getAgentID();
		invFolder.AgentData.SessionID = client.agent.getSessionID();
		invFolder.FolderData = new UpdateInventoryFolderPacket.FolderDataBlock[1];
		invFolder.FolderData[0] = invFolder.new FolderDataBlock();
		invFolder.FolderData[0].FolderID = folderID;
		invFolder.FolderData[0].ParentID = parentID;
		invFolder.FolderData[0].setName(Helpers.stringToBytes(name));
		invFolder.FolderData[0].Type = type.getValue();

		client.network.sendPacket(invFolder);
	}

	/**
	 * Move a folder
	 *
	 * @param folderID
	 *            The source folders {@link UUID}
	 * @param newParentID
	 *            The destination folders {@link UUID}
	 * @throws Exception
	 */
	public final void moveFolder(UUID folderID, UUID newParentID) throws Exception {
		MoveInventoryFolderPacket move = new MoveInventoryFolderPacket();
		move.AgentData.AgentID = client.agent.getAgentID();
		move.AgentData.SessionID = client.agent.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryFolderPacket.InventoryDataBlock[1];
		move.InventoryData[0] = move.new InventoryDataBlock();
		move.InventoryData[0].FolderID = folderID;
		move.InventoryData[0].ParentID = newParentID;

		client.network.sendPacket(move);

		synchronized (store) {
			if (store.containsFolder(folderID)) {
				store.add(newParentID, store.getItem(folderID));
			}
		}
	}

	/**
	 * Move multiple folders, the keys in the Dictionary parameter, to a new
	 * parents, the value of that folder's key.
	 *
	 * @param foldersNewParents
	 *            A Dictionary containing the {@link UUID} of the source as the key,
	 *            and the {@link UUID} of the destination as the value
	 * @throws Exception
	 */
	public final void moveFolders(Map<UUID, UUID> foldersNewParents) throws Exception {
		// TODO: Test if this truly supports multiple-folder move
		MoveInventoryFolderPacket move = new MoveInventoryFolderPacket();
		move.AgentData.AgentID = client.agent.getAgentID();
		move.AgentData.SessionID = client.agent.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryFolderPacket.InventoryDataBlock[foldersNewParents.size()];

		int index = 0;
		for (Entry<UUID, UUID> folder : foldersNewParents.entrySet()) {
			MoveInventoryFolderPacket.InventoryDataBlock block = move.new InventoryDataBlock();
			block.FolderID = folder.getKey();
			block.ParentID = folder.getValue();
			move.InventoryData[index++] = block;
		}
		client.network.sendPacket(move);

		// FIXME: Use two List<UUID> to stay consistent
		synchronized (store) {
			for (Entry<UUID, UUID> entry : foldersNewParents.entrySet()) {
				if (store.containsFolder(entry.getKey())) {
					store.add(entry.getValue(), store.getItem(entry.getKey()));
				}
			}
		}
	}

	/**
	 * Move an inventory item to a new folder
	 *
	 * @param itemID
	 *            The {@link UUID} of the source item to move
	 * @param folderID
	 *            The {@link UUID} of the destination folder
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void moveItem(UUID itemID, UUID folderID) throws UnsupportedEncodingException, Exception {
		moveItem(itemID, folderID, Helpers.EmptyString);
	}

	/**
	 * Move and rename an inventory item
	 *
	 * @param itemID
	 *            The {@link UUID} of the source item to move
	 * @param folderID
	 *            The {@link UUID} of the destination folder
	 * @param newName
	 *            The name to change the folder to, use null or an empty name to
	 *            indicate that the name hasn't changed
	 * @throws Exception
	 */
	public final void moveItem(UUID itemID, UUID folderID, String newName) throws Exception {
		MoveInventoryItemPacket move = new MoveInventoryItemPacket();
		move.AgentData.AgentID = client.agent.getAgentID();
		move.AgentData.SessionID = client.agent.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryItemPacket.InventoryDataBlock[1];
		move.InventoryData[0] = move.new InventoryDataBlock();
		move.InventoryData[0].ItemID = itemID;
		move.InventoryData[0].FolderID = folderID;
		move.InventoryData[0].setNewName(Helpers.stringToBytes(newName));

		client.network.sendPacket(move);

		// Update our local copy
		synchronized (store) {
			if (store.containsItem(itemID)) {
				InventoryNode inv = store.getItem(itemID);
				if (!Helpers.isEmpty(newName)) {
					inv.name = newName;
				}
				store.add(folderID, inv);
			}
		}
	}

	/**
	 * Move multiple inventory items to new locations
	 *
	 * @param itemsNewParents
	 *            A Dictionary containing the {@link UUID} of the source item as the
	 *            key, and the {@link UUID} of the destination folder as the value
	 * @throws Exception
	 */
	public final void moveItems(Map<UUID, UUID> itemsNewParents) throws Exception {
		MoveInventoryItemPacket move = new MoveInventoryItemPacket();
		move.AgentData.AgentID = client.agent.getAgentID();
		move.AgentData.SessionID = client.agent.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryItemPacket.InventoryDataBlock[itemsNewParents.size()];

		int index = 0;
		for (Entry<UUID, UUID> entry : itemsNewParents.entrySet()) {
			MoveInventoryItemPacket.InventoryDataBlock block = move.new InventoryDataBlock();
			block.ItemID = entry.getKey();
			block.FolderID = entry.getValue();
			block.setNewName(Helpers.EmptyBytes);
			move.InventoryData[index++] = block;
		}

		client.network.sendPacket(move);

		// Update our local copy
		synchronized (store) {
			for (Entry<UUID, UUID> entry : itemsNewParents.entrySet()) {
				if (store.containsItem(entry.getKey())) {
					store.add(entry.getValue(), store.getItem(entry.getKey()));
				}
			}
		}
	}

	// #endregion Move

	// #region Remove

	/**
	 * Remove descendants of a folder, basically emptying the folder
	 *
	 * @param folder
	 *            The {@link UUID} of the folder
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void removeDescendants(UUID folderID) throws Exception, InventoryException {
		PurgeInventoryDescendentsPacket purge = new PurgeInventoryDescendentsPacket();
		purge.AgentData.AgentID = client.agent.getAgentID();
		purge.AgentData.SessionID = client.agent.getSessionID();
		purge.FolderID = folderID;
		client.network.sendPacket(purge);

		// Update our local copy
		synchronized (store) {
			if (store.containsFolder(folderID)) {
				Iterator<InventoryNode> iter = store.getFolder(folderID).children.iterator();
				while (iter.hasNext()) {
					store.remove(iter.next());
				}
			}
		}
	}

	/**
	 * Remove a single item from inventory
	 *
	 * @param item
	 *            The {@link UUID} of the inventory item to remove
	 * @throws Exception
	 */
	public final void removeItem(UUID item) throws Exception {
		List<UUID> items = new ArrayList<>(1);
		items.add(item);

		remove(items, null);
	}

	/**
	 * Remove a folder from inventory
	 *
	 * @param folder
	 *            The {@link UUID} of the folder to remove
	 * @throws Exception
	 */
	public final void removeFolder(UUID folder) throws Exception {
		List<UUID> folders = new ArrayList<>(1);
		folders.add(folder);

		remove(null, folders);
	}

	/**
	 * Remove multiple items or folders from inventory
	 *
	 * @param items
	 *            A List containing the {@link UUID} s of items to remove
	 * @param folders
	 *            A List containing the {@link UUID} s of the folders to remove
	 * @throws Exception
	 */
	public final void remove(List<UUID> items, List<UUID> folders) throws Exception {
		if ((items == null || items.isEmpty()) && (folders == null || folders.isEmpty())) {
			return;
		}

		RemoveInventoryObjectsPacket rem = new RemoveInventoryObjectsPacket();
		rem.AgentData.AgentID = client.agent.getAgentID();
		rem.AgentData.SessionID = client.agent.getSessionID();

		if (items == null || items.isEmpty()) {
			// To indicate that we want no items removed:
			rem.ItemID = new UUID[1];
			rem.ItemID[0] = UUID.ZERO;
		} else {
			synchronized (store) {
				rem.ItemID = new UUID[items.size()];
				for (int i = 0; i < items.size(); i++) {
					UUID uuid = items.get(i);
					rem.ItemID[i] = uuid;

					// Update local copy
					if (store.containsItem(uuid)) {
						store.remove(store.getItem(uuid));
					}
				}
			}
		}

		if (folders == null || folders.isEmpty()) {
			// To indicate we want no folders removed:
			rem.FolderID = new UUID[1];
			rem.FolderID[0] = UUID.ZERO;
		} else {
			synchronized (store) {
				rem.FolderID = new UUID[folders.size()];
				for (int i = 0; i < folders.size(); i++) {
					UUID uuid = folders.get(i);
					rem.FolderID[i] = uuid;

					// Update local copy
					if (store.containsFolder(uuid)) {
						store.remove(store.getFolder(uuid));
					}
				}
			}
		}
		client.network.sendPacket(rem);
	}

	/**
	 * Empty the Lost and Found folder
	 *
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void emptyLostAndFound() throws InventoryException, Exception {
		emptySystemFolder(FolderType.LostAndFound);
	}

	/**
	 * Empty the Trash folder
	 *
	 * @throws Exception
	 */
	public final void emptyTrash() throws Exception {
		emptySystemFolder(FolderType.Trash);
	}

	/**
	 * Empty the Lost and Found folder
	 *
	 * @param folderType
	 *            The type of folder to empty
	 * @throws Exception
	 */
	private void emptySystemFolder(FolderType folderType) throws Exception {
		synchronized (store) {
			Iterator<InventoryNode> iter = store.getInventoryFolder().children.iterator();
			InventoryFolder folder = null;
			while (iter.hasNext()) {
				InventoryNode node = iter.next();
				if (node.getType() == InventoryType.Folder) {
					if (((InventoryFolder) node).preferredType == folderType) {
						folder = (InventoryFolder) node;
					}
				}
			}

			if (folder != null) {
				iter = folder.children.iterator();

				List<UUID> remItems = new ArrayList<>();
				List<UUID> remFolders = new ArrayList<>();
				while (iter.hasNext()) {
					InventoryNode node = iter.next();
					if (node.getType() == InventoryType.Folder) {
						remFolders.add(node.itemID);
					} else {
						remItems.add(node.itemID);
					}
				}
				remove(remItems, remFolders);
			}
		}
	}

	// #endregion Remove

	// /#region Create

	/**
	 *
	 *
	 * @param parentFolder
	 * @param name
	 * @param description
	 * @param type
	 * @param assetTransactionID
	 *            Proper use is to upload the inventory's asset first, then provide
	 *            the Asset's TransactionID here.
	 * @param invType
	 * @param nextOwnerMask
	 * @param callback
	 * @throws Exception
	 */
	public final void requestCreateItem(UUID parentFolder, String name, String description, AssetType type,
			UUID assetTransactionID, InventoryType invType, int nextOwnerMask,
			Callback<ItemCreatedCallbackArgs> callback) throws Exception {
		// Even though WearableType.Shape, in this context it is treated as NOT_WEARABLE
		requestCreateItem(parentFolder, name, description, type, assetTransactionID, invType, WearableType.Shape,
				nextOwnerMask, callback);
	}

	/**
	 *
	 *
	 * @param parentFolder
	 * @param name
	 * @param description
	 * @param type
	 * @param assetTransactionID
	 *            Proper use is to upload the inventory's asset first, then provide
	 *            the Asset's TransactionID here.
	 * @param invType
	 * @param wearableType
	 * @param nextOwnerMask
	 * @param callback
	 * @throws Exception
	 */
	public final void requestCreateItem(UUID parentFolder, String name, String description, AssetType type,
			UUID assetTransactionID, InventoryType invType, WearableType wearableType, int nextOwnerMask,
			Callback<ItemCreatedCallbackArgs> callback) throws Exception {
		CreateInventoryItemPacket create = new CreateInventoryItemPacket();
		create.AgentData.AgentID = client.agent.getAgentID();
		create.AgentData.SessionID = client.agent.getSessionID();

		create.InventoryBlock.CallbackID = registerItemCreatedCallback(callback);
		create.InventoryBlock.FolderID = parentFolder;
		create.InventoryBlock.TransactionID = assetTransactionID;
		create.InventoryBlock.NextOwnerMask = nextOwnerMask;
		create.InventoryBlock.Type = type.getValue();
		create.InventoryBlock.InvType = invType.getValue();
		create.InventoryBlock.WearableType = WearableType.getValue(wearableType);
		create.InventoryBlock.setName(Helpers.stringToBytes(name));
		create.InventoryBlock.setDescription(Helpers.stringToBytes(description));

		client.network.sendPacket(create);
	}

	/**
	 * Creates a new inventory folder
	 *
	 * @param parentID
	 *            ID of the folder to put this folder in
	 * @param name
	 *            Name of the folder to create
	 * @return The UUID of the newly created folder
	 * @throws Exception
	 */
	public final UUID createFolder(UUID parentID, String name) throws Exception {
		return createFolder(parentID, name, FolderType.None);
	}

	/**
	 * Creates a new inventory folder. If you specify a preferred type of
	 * <code>FolderType.Root</code> it will create a new root folder which may
	 * likely cause all sorts of strange problems
	 *
	 * @param parentID
	 *            ID of the folder to put this folder in
	 * @param name
	 *            Name of the folder to create
	 * @param preferredType
	 *            Sets this folder as the default folder for new assets of the
	 *            specified type. Use <code>FolderType.None</code> to create a
	 *            normal folder, otherwise it will likely create a duplicate of an
	 *            existing folder type
	 * @return The UUID of the newly created folder
	 * @throws Exception
	 */
	public final UUID createFolder(UUID parentID, String name, FolderType preferredType) throws Exception {
		UUID id = new UUID();

		// Assign a folder name if one is not already set
		if (Helpers.isEmpty(name)) {
			if (preferredType.getValue() >= FolderType.Texture.getValue()
					&& preferredType.getValue() <= FolderType.Gesture.getValue()) {
				name = NEW_FOLDER_NAMES[preferredType.getValue()];
			} else {
				name = "New Folder";
			}
		}

		// Create the new folder locally
		InventoryFolder newFolder = new InventoryFolder(id, parentID, client.agent.getAgentID());
		newFolder.version = 1;
		newFolder.preferredType = preferredType;
		newFolder.name = name;

		// Update the local store
		store.add(newFolder);

		// Create the create folder packet and send it
		CreateInventoryFolderPacket create = new CreateInventoryFolderPacket();
		create.AgentData.AgentID = client.agent.getAgentID();
		create.AgentData.SessionID = client.agent.getSessionID();

		create.FolderData.FolderID = id;
		create.FolderData.ParentID = parentID;
		create.FolderData.Type = preferredType.getValue();
		create.FolderData.setName(Helpers.stringToBytes(name));

		client.network.sendPacket(create);

		return id;
	}

	/**
	 * Create an inventory item and upload asset data
	 *
	 * @param data
	 *            Asset data
	 * @param name
	 *            Inventory item name
	 * @param description
	 *            Inventory item description
	 * @param assetType
	 *            Asset type
	 * @param invType
	 *            Inventory type
	 * @param folderID
	 *            Put newly created inventory in this folder
	 * @param callback
	 *            Callback that will receive feedback on success or failure
	 * @throws Exception
	 */
	public final void requestCreateItemFromAsset(byte[] data, String name, String description, AssetType assetType,
			InventoryType invType, UUID folderID, Callback<ItemCreatedFromAssetCallbackArgs> callback)
			throws Exception {
		Permissions permissions = new Permissions();
		permissions.everyoneMask = PermissionMask.None;
		permissions.groupMask = PermissionMask.None;
		permissions.nextOwnerMask = PermissionMask.All;

		requestCreateItemFromAsset(data, name, description, assetType, invType, folderID, permissions, callback);
	}

	/**
	 * Create an inventory item and upload asset data
	 *
	 * @param data
	 *            Asset data
	 * @param name
	 *            Inventory item name
	 * @param description
	 *            Inventory item description
	 * @param assetType
	 *            Asset type
	 * @param invType
	 *            Inventory type
	 * @param folderID
	 *            Put newly created inventory in this folder
	 * @param permissions
	 *            Permission of the newly created item (EveryoneMask, GroupMask, and
	 *            NextOwnerMask of Permissions struct are supported)
	 * @param callback
	 *            Delegate that will receive feedback on success or failure
	 * @throws Exception
	 */
	public final void requestCreateItemFromAsset(byte[] data, String name, String description, AssetType assetType,
			InventoryType invType, UUID folderID, Permissions permissions,
			Callback<ItemCreatedFromAssetCallbackArgs> callback) throws Exception {
		String cap = CapsEventType.NewFileAgentInventory.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			OSDMap query = new OSDMap();
			query.put("folder_id", OSD.fromUUID(folderID));
			query.put("asset_type", OSD.fromString(assetType.toString()));
			query.put("inventory_type", OSD.fromString(invType.toString()));
			query.put("name", OSD.fromString(name));
			query.put("description", OSD.fromString(description));
			query.put("everyone_mask", OSD.fromInteger(permissions.everyoneMask));
			query.put("group_mask", OSD.fromInteger(permissions.groupMask));
			query.put("next_owner_mask", OSD.fromInteger(permissions.nextOwnerMask));
			query.put("expected_upload_cost", OSD.fromInteger(client.settings.getUploadPrice()));

			// Make the request
			CapsClient request = new CapsClient(client, cap);
			FutureCallback<OSD> cb = new CreateItemFromAssetResponse(callback, data, client.settings.CAPS_TIMEOUT,
					query);
			request.executeHttpPost(url, query, OSDFormat.Xml, cb, client.settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("NewFileAgentInventory capability is not currently available");
		}
	}

	/**
	 * Creates inventory link to another inventory item or folder
	 *
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param bse
	 *            Inventory item or folder
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void createLink(UUID folderID, InventoryNode bse, Callback<ItemCreatedCallbackArgs> callback)
			throws Exception {
		if (bse instanceof InventoryFolder) {
			InventoryFolder folder = (InventoryFolder) bse;
			createLink(folderID, folder, callback);
		} else if (bse instanceof InventoryItem) {
			InventoryItem item = (InventoryItem) bse;
			createLink(folderID, item.itemID, item.name, item.description, AssetType.Link, item.getType(), new UUID(),
					callback);
		}
	}

	/**
	 * Creates inventory link to another inventory item
	 *
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param item
	 *            Original inventory item
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void createLink(UUID folderID, InventoryItem item, Callback<ItemCreatedCallbackArgs> callback)
			throws Exception {
		createLink(folderID, item.itemID, item.name, item.description, AssetType.Link, item.getType(), new UUID(),
				callback);
	}

	/**
	 * Creates inventory link to another inventory folder
	 *
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param folder
	 *            Original inventory folder
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void createLink(UUID folderID, InventoryFolder folder, Callback<ItemCreatedCallbackArgs> callback)
			throws Exception {
		createLink(folderID, folder.itemID, folder.name, "", AssetType.LinkFolder, InventoryType.Folder, new UUID(),
				callback);
	}

	/**
	 * Creates inventory link to another inventory item or folder
	 *
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param itemID
	 *            Original item's UUID
	 * @param name
	 *            Name
	 * @param description
	 *            Description
	 * @param assetType
	 *            Asset Type
	 * @param invType
	 *            Inventory Type
	 * @param transactionID
	 *            Transaction UUID
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void createLink(UUID folderID, UUID itemID, String name, String description, AssetType assetType,
			InventoryType invType, UUID transactionID, Callback<ItemCreatedCallbackArgs> callback) throws Exception {
		LinkInventoryItemPacket create = new LinkInventoryItemPacket();
		create.AgentData.AgentID = client.agent.getAgentID();
		create.AgentData.SessionID = client.agent.getSessionID();

		create.InventoryBlock.CallbackID = registerItemCreatedCallback(callback);
		itemInventoryTypeRequest.put(create.InventoryBlock.CallbackID, invType);

		create.InventoryBlock.FolderID = folderID;
		create.InventoryBlock.TransactionID = transactionID;
		create.InventoryBlock.OldItemID = itemID;
		create.InventoryBlock.Type = assetType.getValue();
		create.InventoryBlock.InvType = invType.getValue();
		create.InventoryBlock.setName(Helpers.stringToBytes(name));
		create.InventoryBlock.setDescription(Helpers.stringToBytes(description));

		client.network.sendPacket(create);
	}

	// #endregion Create

	// #region Copy

	/**
	 * Copy an item to a new location (folder)
	 *
	 * @param item
	 *            The UUID of the item to copy
	 * @param newParent
	 *            The UUID of the folder to copy the item to
	 * @param newName
	 *            An optional name to assign to the new item Can be null if the
	 *            existing name should be used.
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void requestCopyItem(UUID item, UUID newParent, String newName,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception {
		requestCopyItem(item, newParent, newName, client.agent.getAgentID(), callback);
	}

	/**
	 * Copy an item to a new location (folder)
	 *
	 * @param item
	 *            The UUID of the item to copy
	 * @param newParent
	 *            The UUID of the folder to copy the item to
	 * @param newName
	 *            An optional name to assign to the new item Can be null if the
	 *            existing name should be used.
	 * @param oldOwnerID
	 *            The previous owner of the item
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void requestCopyItem(UUID item, UUID newParent, String newName, UUID oldOwnerID,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception {
		List<UUID> items = new ArrayList<>(1);
		items.add(item);

		List<UUID> folders = new ArrayList<>(1);
		folders.add(newParent);

		if (newName != null) {
			List<String> newNames = new ArrayList<>(1);
			newNames.add(newName);

			requestCopyItems(items, folders, newNames, oldOwnerID, callback);
		} else {
			requestCopyItems(items, folders, null, oldOwnerID, callback);
		}
	}

	/**
	 * Copy one or more items to a new location (folder)
	 *
	 * @param items
	 *            The UUIDs of the items to copy
	 * @param newParent
	 *            The UUID of the folder to copy the item to
	 * @param newNames
	 *            An optional array of names to assign to the new items. Can be null
	 *            if the existing name should be used.
	 * @param oldOwnerID
	 *            The previous owner of the items
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void requestCopyItems(List<UUID> items, UUID newParent, List<String> newNames, UUID oldOwnerID,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception {
		List<UUID> folders = new ArrayList<>(1);
		folders.add(newParent);

		requestCopyItems(items, folders, newNames, oldOwnerID, callback);
	}

	/**
	 * Copy one or more items to new locations (folders)
	 *
	 * @param items
	 *            The UUIDs of the items to copy
	 * @param targetFolders
	 *            The UUIDs of the folders to copy the items to
	 * @param newNames
	 *            An optional array of names to assign to the new items Can be null
	 *            if the existing name should be used.
	 * @param oldOwnerID
	 *            The previous owner of the items
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void requestCopyItems(List<UUID> items, List<UUID> targetFolders, List<String> newNames,
			UUID oldOwnerID, Callback<ItemCopiedCallbackArgs> callback) throws Exception {
		if (newNames != null && items.size() != newNames.size()) {
			throw new IllegalArgumentException("All list arguments must have an equal number of entries");
		}

		int callbackID = registerItemsCopiedCallback(callback);
		int lastTarget = targetFolders.size() - 1;

		CopyInventoryItemPacket copy = new CopyInventoryItemPacket();
		copy.AgentData.AgentID = client.agent.getAgentID();
		copy.AgentData.SessionID = client.agent.getSessionID();

		copy.InventoryData = new CopyInventoryItemPacket.InventoryDataBlock[items.size()];
		for (int i = 0; i < items.size(); ++i) {
			copy.InventoryData[i] = copy.new InventoryDataBlock();
			copy.InventoryData[i].CallbackID = callbackID;
			copy.InventoryData[i].NewFolderID = targetFolders.get(lastTarget > i ? i : lastTarget);
			copy.InventoryData[i].OldAgentID = oldOwnerID;
			copy.InventoryData[i].OldItemID = items.get(i);

			if (newNames != null && !Helpers.isEmpty(newNames.get(i))) {
				copy.InventoryData[i].setNewName(Helpers.stringToBytes(newNames.get(i)));
			} else {
				copy.InventoryData[i].setNewName(Helpers.EmptyBytes);
			}
		}
		client.network.sendPacket(copy);
	}

	/**
	 * Request a copy of an asset embedded within a notecard
	 *
	 * @param objectID
	 *            Usually UUID.Zero for copying an asset from a notecard
	 * @param notecardID
	 *            UUID of the notecard to request an asset from
	 * @param folderID
	 *            Target folder for asset to go to in your inventory
	 * @param itemID
	 *            UUID of the embedded asset
	 * @param callback
	 *            callback to run when item is copied to inventory
	 * @throws Exception
	 */
	public final void requestCopyItemFromNotecard(UUID objectID, UUID notecardID, UUID folderID, UUID itemID,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception {
		itemCopiedCallbacks.put(0, callback); // Notecards always use callback ID 0

		String cap = CapsEventType.CopyInventoryFromNotecard.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			CopyInventoryFromNotecardMessage message = client.messages.new CopyInventoryFromNotecardMessage();
			message.callbackID = 0;
			message.folderID = folderID;
			message.itemID = itemID;
			message.notecardID = notecardID;
			message.objectID = objectID;

			new CapsClient(client, cap).executeHttpPost(url, message, null, client.settings.CAPS_TIMEOUT);
		} else {
			CopyInventoryFromNotecardPacket copy = new CopyInventoryFromNotecardPacket();
			copy.AgentData.AgentID = client.agent.getAgentID();
			copy.AgentData.SessionID = client.agent.getSessionID();

			copy.NotecardData.ObjectID = objectID;
			copy.NotecardData.NotecardItemID = notecardID;

			copy.InventoryData = new CopyInventoryFromNotecardPacket.InventoryDataBlock[1];
			copy.InventoryData[0] = copy.new InventoryDataBlock();
			copy.InventoryData[0].FolderID = folderID;
			copy.InventoryData[0].ItemID = itemID;

			client.network.sendPacket(copy);
		}
	}

	// #endregion Copy

	// /#region Update

	/**
	 *
	 *
	 * @param item
	 * @throws Exception
	 */
	public final UUID requestUpdateItem(InventoryItem item) throws Exception {
		List<InventoryItem> items = new ArrayList<>(1);
		items.add(item);

		return requestUpdateItems(items, new UUID());
	}

	/**
	 *
	 *
	 * @param items
	 * @throws Exception
	 */
	public final UUID requestUpdateItems(List<InventoryItem> items) throws Exception {
		return requestUpdateItems(items, new UUID());
	}

	/**
	 *
	 *
	 * @param items
	 * @param items
	 * @param transactionID
	 * @throws Exception
	 */
	public final UUID requestUpdateItems(List<InventoryItem> items, UUID transactionID) throws Exception {
		UpdateInventoryItemPacket update = new UpdateInventoryItemPacket();
		update.AgentData.AgentID = client.agent.getAgentID();
		update.AgentData.SessionID = client.agent.getSessionID();
		update.AgentData.TransactionID = transactionID;

		update.InventoryData = new UpdateInventoryItemPacket.InventoryDataBlock[items.size()];
		for (int i = 0; i < items.size(); i++) {
			InventoryItem item = items.get(i);

			UpdateInventoryItemPacket.InventoryDataBlock block = update.new InventoryDataBlock();
			block.BaseMask = item.permissions.baseMask;
			block.CRC = itemCRC(item);
			block.CreationDate = (int) Helpers.dateTimeToUnixTime(item.creationDate);
			block.CreatorID = item.permissions.creatorID;
			block.setDescription(Helpers.stringToBytes(item.description));
			block.EveryoneMask = item.permissions.everyoneMask;
			block.Flags = item.itemFlags;
			block.FolderID = item.parent.itemID;
			block.GroupID = item.permissions.groupID;
			block.GroupMask = item.permissions.groupMask;
			block.GroupOwned = item.permissions.isGroupOwned;
			block.InvType = item.getType().getValue();
			block.ItemID = item.itemID;
			block.setName(Helpers.stringToBytes(item.name));
			block.NextOwnerMask = item.permissions.nextOwnerMask;
			block.OwnerID = store.getOwnerID();
			block.OwnerMask = item.permissions.ownerMask;
			block.SalePrice = item.salePrice;
			block.SaleType = item.saleType.getValue();
			block.TransactionID = transactionID;
			block.Type = item.assetType.getValue();

			update.InventoryData[i] = block;
		}
		client.network.sendPacket(update);
		return transactionID;
	}

	/**
	 *
	 *
	 * @param data
	 * @param notecardID
	 * @param callback
	 * @throws Exception
	 */
	public final void requestUpdateNotecardAgentInventory(byte[] data, UUID notecardID,
			Callback<InventoryUploadedAssetCallbackArgs> callback) throws Exception {
		String cap = CapsEventType.UpdateNotecardAgentInventory.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			OSDMap query = new OSDMap();
			query.put("item_id", OSD.fromUUID(notecardID));

			// Make the request
			CapsClient request = new CapsClient(client, cap);
			request.executeHttpPost(url, query, OSDFormat.Xml,
					new UploadInventoryAssetComplete(callback, data, notecardID), client.settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("UpdateNotecardAgentInventory capability is not currently available");
		}
	}

	/**
	 * Save changes to notecard embedded in object contents
	 *
	 * @param data
	 *            Encoded notecard asset data
	 * @param notecardID
	 *            Notecard UUID
	 * @param taskID
	 *            Object's UUID
	 * @param callback
	 *            Called upon finish of the upload with status information
	 * @throws Exception
	 */
	public final void requestUpdateNotecardTaskInventory(byte[] data, UUID notecardID, UUID taskID,
			Callback<InventoryUploadedAssetCallbackArgs> callback) throws Exception {
		String cap = CapsEventType.UpdateNotecardTaskInventory.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			OSDMap query = new OSDMap();
			query.put("item_id", OSD.fromUUID(notecardID));
			query.put("task_id", OSD.fromUUID(taskID));

			// Make the request
			CapsClient request = new CapsClient(client, cap);
			request.executeHttpPost(url, query, OSDFormat.Xml,
					new UploadInventoryAssetComplete(callback, data, notecardID), client.settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("UpdateNotecardTaskInventory capability is not currently available");
		}
	}

	/**
	 * Upload new gesture asset for an inventory gesture item
	 *
	 * @param data
	 *            Encoded gesture asset
	 * @param gestureID
	 *            Gesture inventory UUID
	 * @param callback
	 *            Callback whick will be called when upload is complete
	 * @throws Exception
	 */
	public final void requestUpdateGestureAgentInventory(byte[] data, UUID gestureID,
			Callback<InventoryUploadedAssetCallbackArgs> callback) throws Exception {
		String cap = CapsEventType.UpdateGestureAgentInventory.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			OSDMap query = new OSDMap();
			query.put("item_id", OSD.fromUUID(gestureID));

			// Make the request
			CapsClient request = new CapsClient(client, cap);
			request.executeHttpPost(url, query, OSDFormat.Xml,
					new UploadInventoryAssetComplete(callback, data, gestureID), client.settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("UpdateGestureAgentInventory capability is not currently available");
		}
	}

	/**
	 * Update an existing script in an agents Inventory
	 *
	 * @param data
	 *            A byte[] array containing the encoded scripts contents
	 * @param itemID
	 *            the itemID of the script
	 * @param mono
	 *            if true, sets the script content to run on the mono interpreter
	 * @param callback
	 * @throws Exception
	 */
	public final void requestUpdateScriptAgent(byte[] data, UUID itemID, boolean mono,
			Callback<ScriptUpdatedCallbackArgs> callback) throws Exception {
		String cap = CapsEventType.UpdateScriptAgent.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			OSDMap map = new OSDMap(2);
			map.put("item_id", OSD.fromUUID(itemID));
			map.put("target", OSD.fromString(mono ? "mono" : "lsl2"));

			CapsClient request = new CapsClient(client, cap);
			request.executeHttpPost(url, map, OSDFormat.Xml,
					new UpdateScriptAgentInventoryResponse(callback, data, itemID), client.settings.CAPS_TIMEOUT);

		} else {
			throw new Exception("UpdateScriptAgent capability is not currently available");
		}
	}

	/**
	 * Update an existing script in an task Inventory
	 *
	 * @param data
	 *            A byte[] array containing the encoded scripts contents
	 * @param itemID
	 *            the itemID of the script
	 * @param taskID
	 *            UUID of the prim containting the script
	 * @param mono
	 *            if true, sets the script content to run on the mono interpreter
	 * @param running
	 *            if true, sets the script to running
	 * @param callback
	 * @throws Exception
	 */
	public final void requestUpdateScriptTask(byte[] data, UUID itemID, UUID taskID, boolean mono, boolean running,
			Callback<ScriptUpdatedCallbackArgs> callback) throws Exception {
		String cap = CapsEventType.UpdateScriptTask.toString();
		URI url = client.network.getCapabilityURI(cap);
		if (url != null) {
			UpdateScriptTaskUpdateMessage msg = client.messages.new UpdateScriptTaskUpdateMessage();
			msg.itemID = itemID;
			msg.taskID = taskID;
			msg.scriptRunning = running;
			msg.target = mono ? "mono" : "lsl2";

			CapsClient request = new CapsClient(client, cap);
			request.executeHttpPost(url, msg, new UpdateScriptAgentInventoryResponse(callback, data, itemID),
					client.settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("UpdateScriptTask capability is not currently available");
		}
	}

	// #endregion Update

	// #region Rez/Give

	/**
	 * Rez an object from inventory
	 *
	 * @param simulator
	 *            Simulator to place object in
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @return the @see UUID that identifies this query
	 * @throws Exception
	 */
	public final UUID requestRezFromInventory(Simulator simulator, Quaternion rotation, Vector3 position,
			InventoryItem item) throws Exception {
		return requestRezFromInventory(simulator, rotation, position, item, client.agent.getActiveGroup(), new UUID(),
				true);
	}

	/**
	 * Rez an object from inventory
	 *
	 * @param simulator
	 *            Simulator to place object in
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @param groupOwner
	 *            UUID of group to own the object
	 * @return the @see UUID that identifies this query
	 * @throws Exception
	 */
	public final UUID requestRezFromInventory(Simulator simulator, Quaternion rotation, Vector3 position,
			InventoryItem item, UUID groupOwner) throws Exception {
		return requestRezFromInventory(simulator, rotation, position, item, groupOwner, new UUID(), true);
	}

	/**
	 * Rez an object from inventory
	 *
	 * @param simulator
	 *            Simulator to place object in
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @param groupOwner
	 *            UUID of group to own the object
	 * @param rezSelected
	 *            If set to true, the CreateSelected flag will be set on the rezzed
	 *            object
	 * @return the @see UUID that identifies this query
	 * @throws Exception
	 */
	public final UUID requestRezFromInventory(Simulator simulator, Quaternion rotation, Vector3 position,
			InventoryItem item, UUID groupOwner, UUID queryID, boolean rezSelected) throws Exception {
		return requestRezFromInventory(simulator, new UUID(), rotation, position, item, groupOwner, queryID,
				rezSelected);
	}

	/**
	 * Rez an object from inventory
	 *
	 * @param simulator
	 *            Simulator to place object in
	 * @param taskID
	 *            TaskID object when rezzed
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @param groupOwner
	 *            UUID of group to own the object
	 * @param rezSelected
	 *            If set to true, the CreateSelected flag will be set on the rezzed
	 *            object
	 * @return the @see UUID that identifies this query
	 * @throws Exception
	 */
	public final UUID requestRezFromInventory(Simulator simulator, UUID taskID, Quaternion rotation, Vector3 position,
			InventoryItem item, UUID groupOwner, UUID queryID, boolean rezSelected) throws Exception {
		RezObjectPacket add = new RezObjectPacket();

		add.AgentData.AgentID = client.agent.getAgentID();
		add.AgentData.SessionID = client.agent.getSessionID();
		add.AgentData.GroupID = groupOwner;

		add.RezData.FromTaskID = taskID;
		add.RezData.BypassRaycast = 1;
		add.RezData.RayStart = position;
		add.RezData.RayEnd = position;
		add.RezData.RayTargetID = UUID.ZERO;
		add.RezData.RayEndIsIntersection = false;
		add.RezData.RezSelected = rezSelected;
		add.RezData.RemoveItem = false;
		add.RezData.ItemFlags = item.itemFlags;
		add.RezData.GroupMask = item.permissions.groupMask;
		add.RezData.EveryoneMask = item.permissions.everyoneMask;
		add.RezData.NextOwnerMask = item.permissions.nextOwnerMask;

		add.InventoryData.ItemID = item.itemID;
		add.InventoryData.FolderID = item.parent.itemID;
		add.InventoryData.CreatorID = item.permissions.creatorID;
		add.InventoryData.OwnerID = item.permissions.ownerID;
		add.InventoryData.GroupID = item.permissions.groupID;
		add.InventoryData.BaseMask = item.permissions.baseMask;
		add.InventoryData.OwnerMask = item.permissions.ownerMask;
		add.InventoryData.GroupMask = item.permissions.groupMask;
		add.InventoryData.EveryoneMask = item.permissions.everyoneMask;
		add.InventoryData.NextOwnerMask = item.permissions.nextOwnerMask;
		add.InventoryData.GroupOwned = item.permissions.isGroupOwned;
		add.InventoryData.TransactionID = queryID;
		add.InventoryData.Type = item.getType().getValue();
		add.InventoryData.InvType = item.getType().getValue();
		add.InventoryData.Flags = item.itemFlags;
		add.InventoryData.SaleType = item.saleType.getValue();
		add.InventoryData.SalePrice = item.salePrice;
		add.InventoryData.setName(Helpers.stringToBytes(item.name));
		add.InventoryData.setDescription(Helpers.stringToBytes(item.description));
		add.InventoryData.CreationDate = (int) Helpers.dateTimeToUnixTime(item.creationDate);

		simulator.sendPacket(add);

		return queryID;
	}

	/**
	 * DeRez an object from the simulator to the agents Objects folder in the agents
	 * Inventory
	 *
	 * @param objectLocalID
	 *            The simulator Local ID of the object If objectLocalID is a child
	 *            primitive in a linkset, the entire linkset will be derezzed
	 * @throws Exception
	 */
	public final void requestDeRezToInventory(int objectLocalID) throws Exception {
		requestDeRezToInventory(objectLocalID, DeRezDestination.AgentInventoryTake,
				findFolderForType(FolderType.Object).itemID, new UUID());
	}

	/**
	 * DeRez an object from the simulator and return to inventory
	 *
	 * @param objectLocalID
	 *            The simulator Local ID of the object
	 * @param destType
	 *            The type of destination from the {@link DeRezDestination} enum
	 * @param destFolder
	 *            The destination inventory folders {@link UUID} -or- if DeRezzing
	 *            object to a tasks Inventory, the Tasks {@link UUID}
	 * @param transactionID
	 *            The transaction ID for this request which can be used to correlate
	 *            this request with other packets. If objectLocalID is a child
	 *            primitive in a linkset, the entire linkset will be derezzed
	 * @throws Exception
	 */
	public final void requestDeRezToInventory(int objectLocalID, DeRezDestination destType, UUID destFolder,
			UUID transactionID) throws Exception {
		DeRezObjectPacket take = new DeRezObjectPacket();

		take.AgentData.AgentID = client.agent.getAgentID();
		take.AgentData.SessionID = client.agent.getSessionID();
		take.AgentBlock = take.new AgentBlockBlock();
		take.AgentBlock.GroupID = UUID.ZERO;
		take.AgentBlock.Destination = destType.getValue();
		take.AgentBlock.DestinationID = destFolder;
		take.AgentBlock.PacketCount = 1;
		take.AgentBlock.PacketNumber = 1;
		take.AgentBlock.TransactionID = transactionID;

		take.ObjectLocalID = new int[1];
		take.ObjectLocalID[0] = objectLocalID;

		client.network.sendPacket(take);
	}

	/**
	 * Rez an item from inventory to its previous simulator location
	 *
	 * @param simulator
	 * @param item
	 * @param queryID
	 * @return
	 * @throws Exception
	 */
	public final UUID requestRestoreRezFromInventory(Simulator simulator, InventoryItem item, UUID queryID)
			throws Exception {
		RezRestoreToWorldPacket add = new RezRestoreToWorldPacket();

		add.AgentData.AgentID = client.agent.getAgentID();
		add.AgentData.SessionID = client.agent.getSessionID();

		add.InventoryData.ItemID = item.itemID;
		add.InventoryData.FolderID = item.parent.itemID;
		add.InventoryData.CreatorID = item.permissions.creatorID;
		add.InventoryData.OwnerID = item.permissions.ownerID;
		add.InventoryData.GroupID = item.permissions.groupID;
		add.InventoryData.BaseMask = item.permissions.baseMask;
		add.InventoryData.OwnerMask = item.permissions.ownerMask;
		add.InventoryData.GroupMask = item.permissions.groupMask;
		add.InventoryData.EveryoneMask = item.permissions.everyoneMask;
		add.InventoryData.NextOwnerMask = item.permissions.nextOwnerMask;
		add.InventoryData.GroupOwned = item.permissions.isGroupOwned;
		add.InventoryData.TransactionID = queryID;
		add.InventoryData.Type = item.getType().getValue();
		add.InventoryData.InvType = item.getType().getValue();
		add.InventoryData.Flags = item.itemFlags;
		add.InventoryData.SaleType = item.saleType.getValue();
		add.InventoryData.SalePrice = item.salePrice;
		add.InventoryData.setName(Helpers.stringToBytes(item.name));
		add.InventoryData.setDescription(Helpers.stringToBytes(item.description));
		add.InventoryData.CreationDate = (int) Helpers.dateTimeToUnixTime(item.creationDate);

		simulator.sendPacket(add);

		return queryID;
	}

	/**
	 * Give an inventory item to another avatar
	 *
	 * @param itemID
	 *            The {@link UUID} of the item to give
	 * @param itemName
	 *            The name of the item
	 * @param assetType
	 *            The type of the item from the {@link AssetType} enum
	 * @param recipient
	 *            The {@link UUID} of the recipient
	 * @param doEffect
	 *            true to generate a beameffect during transfer
	 * @throws Exception
	 */
	public final void giveItem(UUID itemID, String itemName, AssetType assetType, UUID recipient, boolean doEffect)
			throws Exception {
		byte[] bucket;

		bucket = new byte[17];
		bucket[0] = assetType.getValue();
		itemID.toBytes(bucket, 1);

		client.agent.instantMessage(client.agent.getName(), recipient, itemName, new UUID(),
				InstantMessageDialog.InventoryOffered, InstantMessageOnline.Online, null, null, 0, bucket);

		if (doEffect) {
			client.agent.beamEffect(client.agent.getAgentID(), recipient, Vector3d.ZERO,
					client.settings.DEFAULT_EFFECT_COLOR, 1f, new UUID());
		}
	}

	/**
	 * Give an inventory Folder with contents to another avatar
	 *
	 * @param folderID
	 *            The {@link UUID} of the Folder to give
	 * @param folderName
	 *            The name of the folder
	 * @param assetType
	 *            The type of the item from the {@link AssetType} enum
	 * @param recipient
	 *            The {@link UUID} of the recipient
	 * @param doEffect
	 *            true to generate a beameffect during transfer
	 * @throws Exception
	 */
	public final void giveFolder(UUID folderID, String folderName, AssetType assetType, UUID recipient,
			boolean doEffect) throws Exception {
		byte[] bucket;

		List<InventoryItem> folderContents = new ArrayList<>();
		List<InventoryNode> ibl = folderContents(folderID, client.agent.getAgentID(), false, true,
				InventorySortOrder.ByDate, false, 1000 * 15);
		for (InventoryNode ib : ibl) {
			folderContents.add(fetchItem(ib.itemID, client.agent.getAgentID(), 1000 * 10));
		}
		bucket = new byte[17 * (folderContents.size() + 1)];

		// Add parent folder (first item in bucket)
		bucket[0] = assetType.getValue();
		folderID.toBytes(bucket, 1);

		// Add contents to bucket after folder
		for (int i = 1; i <= folderContents.size(); ++i) {
			bucket[i * 17] = folderContents.get(i - 1).assetType.getValue();
			folderContents.get(i - 1).itemID.toBytes(bucket, i * 17 + 1);
		}
		client.agent.instantMessage(client.agent.getName(), recipient, folderName, new UUID(),
				InstantMessageDialog.InventoryOffered, InstantMessageOnline.Online, null, null, 0, bucket);

		if (doEffect) {
			client.agent.beamEffect(client.agent.getAgentID(), recipient, Vector3d.ZERO,
					client.settings.DEFAULT_EFFECT_COLOR, 1f, new UUID());
		}
	}

	// #endregion Rez/Give

	// /#region Task

	/**
	 * Copy or move an <see cref="InventoryItem"/> from agent inventory to a task
	 * (primitive) inventory
	 *
	 * @param objectLocalID
	 *            The target object
	 * @param item
	 *            The item to copy or move from inventory
	 * @return For items with copy permissions a copy of the item is placed in the
	 *         tasks inventory, for no-copy items the object is moved to the tasks
	 *         inventory TODO: what does the return UUID correlate to if anything?
	 * @throws Exception
	 */
	public final UUID updateTaskInventory(int objectLocalID, InventoryItem item) throws Exception {
		UUID transactionID = new UUID();

		UpdateTaskInventoryPacket update = new UpdateTaskInventoryPacket();
		update.AgentData.AgentID = client.agent.getAgentID();
		update.AgentData.SessionID = client.agent.getSessionID();
		update.UpdateData.Key = 0;
		update.UpdateData.LocalID = objectLocalID;

		update.InventoryData.ItemID = item.itemID;
		update.InventoryData.FolderID = item.parent.itemID;
		update.InventoryData.CreatorID = item.permissions.creatorID;
		update.InventoryData.OwnerID = item.permissions.ownerID;
		update.InventoryData.GroupID = item.permissions.groupID;
		update.InventoryData.BaseMask = item.permissions.baseMask;
		update.InventoryData.OwnerMask = item.permissions.ownerMask;
		update.InventoryData.GroupMask = item.permissions.groupMask;
		update.InventoryData.EveryoneMask = item.permissions.everyoneMask;
		update.InventoryData.NextOwnerMask = item.permissions.nextOwnerMask;
		update.InventoryData.GroupOwned = item.permissions.isGroupOwned;
		update.InventoryData.TransactionID = transactionID;
		update.InventoryData.Type = item.assetType.getValue();
		update.InventoryData.InvType = item.getType().getValue();
		update.InventoryData.Flags = item.itemFlags;
		update.InventoryData.SaleType = item.saleType.getValue();
		update.InventoryData.SalePrice = item.salePrice;
		update.InventoryData.setName(Helpers.stringToBytes(item.name));
		update.InventoryData.setDescription(Helpers.stringToBytes(item.description));
		update.InventoryData.CreationDate = (int) Helpers.dateTimeToUnixTime(item.creationDate);
		update.InventoryData.CRC = itemCRC(item);

		client.network.sendPacket(update);

		return transactionID;
	}

	/**
	 * Retrieve a listing of the items contained in a task (Primitive) This request
	 * blocks until the response from the simulator arrives or timeoutMS is exceeded
	 *
	 * NOTE: This requires the asset manager to be instantiated in order for this
	 * function to succeed
	 *
	 * @param objectID
	 *            The tasks {@link UUID}
	 * @param objectLocalID
	 *            The tasks simulator local ID
	 * @param timeout
	 *            milliseconds to wait for reply from simulator
	 * @return A list containing the inventory items inside the task or null if a
	 *         timeout occurs
	 * @throws Exception
	 */
	public final List<InventoryNode> getTaskInventory(final UUID objectID, int objectLocalID, int timeout)
			throws Exception {
		if (client.assets == null)
			throw new RuntimeException("Can't get task inventory without the asset manager being instantiated.");

		final TimeoutEvent<String> taskReplyEvent = new TimeoutEvent<>();
		Callback<TaskInventoryReplyCallbackArgs> callback = new Callback<TaskInventoryReplyCallbackArgs>() {
			@Override
			public boolean callback(TaskInventoryReplyCallbackArgs e) {
				if (e.getItemID().equals(objectID)) {
					taskReplyEvent.set(e.getAssetFilename());
				}
				return false;
			}
		};
		onTaskInventoryReply.add(callback, true);
		requestTaskInventory(objectLocalID);
		String filename = taskReplyEvent.waitOne(timeout);
		onTaskInventoryReply.remove(callback);

		if (filename != null) {
			if (!filename.isEmpty()) {
				final TimeoutEvent<String> taskDownloadEvent = new TimeoutEvent<>();
				final long xferID = 0;

				Callback<XferDownload> xferCallback = new Callback<XferDownload>() {
					@Override
					public boolean callback(XferDownload download) {
						if (download.xferID == xferID) {
							try {
								taskDownloadEvent.set(Helpers.bytesToString(download.assetData));
							} catch (UnsupportedEncodingException e1) {
								taskDownloadEvent.set(Helpers.EmptyString);
							}
						}
						return false;
					}
				};
				// Start the actual asset xfer
				client.assets.onXferReceived.add(xferCallback, true);
				client.assets.requestAssetXfer(filename, true, false, UUID.ZERO, AssetType.Unknown, true);
				String taskList = taskDownloadEvent.waitOne(timeout);
				client.assets.onXferReceived.remove(xferCallback);
				if (taskList != null && !taskList.isEmpty()) {
					return parseTaskInventory(taskList);
				}

				logger.warn(GridClient.Log("Timed out waiting for task inventory download for " + filename, client));
				return null;
			}

			logger.debug(GridClient.Log("Task is empty for " + objectLocalID, client));
			return new ArrayList<>(0);
		}

		logger.warn(GridClient.Log("Timed out waiting for task inventory reply for " + objectLocalID, client));
		return null;
	}

	/**
	 * Request the contents of a tasks (primitives) inventory from the current
	 * simulator {@link TaskInventoryReply}
	 *
	 * @param objectLocalID
	 *            The LocalID of the object
	 * @throws Exception
	 */
	public final void requestTaskInventory(int objectLocalID) throws Exception {
		requestTaskInventory(objectLocalID, null);
	}

	/**
	 * Request the contents of a tasks (primitives) inventory
	 * {@link TaskInventoryReply}
	 *
	 * @param objectLocalID
	 *            The simulator Local ID of the object
	 * @param simulator
	 *            A reference to the simulator object that contains the object
	 * @throws Exception
	 */
	public final void requestTaskInventory(int objectLocalID, Simulator simulator) throws Exception {
		RequestTaskInventoryPacket request = new RequestTaskInventoryPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.LocalID = objectLocalID;

		simulator.sendPacket(request);
	}

	/**
	 * Move an item from a tasks (Primitive) inventory to the specified folder in
	 * the avatars inventory Raises the <see cref="OnTaskItemReceived"/> event
	 *
	 * @param objectLocalID
	 *            LocalID of the object in the simulator
	 * @param taskItemID
	 *            UUID of the task item to move
	 * @param inventoryFolderID
	 *            The ID of the destination folder in this agents inventory
	 * @param simulator
	 *            Simulator Object
	 * @throws Exception
	 */
	public final void moveTaskInventory(int objectLocalID, UUID taskItemID, UUID inventoryFolderID, Simulator simulator)
			throws Exception {
		MoveTaskInventoryPacket request = new MoveTaskInventoryPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();

		request.AgentData.FolderID = inventoryFolderID;

		request.InventoryData.ItemID = taskItemID;
		request.InventoryData.LocalID = objectLocalID;

		simulator.sendPacket(request);
	}

	/**
	 * Remove an item from an objects (Prim) Inventory You can confirm the removal
	 * by comparing the tasks inventory serial before and after the request with the
	 * <see cref="RequestTaskInventory"/> request combined with the
	 * {@link TaskInventoryReply} event
	 *
	 * @param objectLocalID
	 *            LocalID of the object in the simulator
	 * @param taskItemID
	 *            UUID of the task item to remove
	 * @param simulator
	 *            Simulator Object
	 * @throws Exception
	 */
	public final void removeTaskInventory(int objectLocalID, UUID taskItemID, Simulator simulator) throws Exception {
		RemoveTaskInventoryPacket remove = new RemoveTaskInventoryPacket();
		remove.AgentData.AgentID = client.agent.getAgentID();
		remove.AgentData.SessionID = client.agent.getSessionID();

		remove.InventoryData.ItemID = taskItemID;
		remove.InventoryData.LocalID = objectLocalID;

		simulator.sendPacket(remove);
	}

	/**
	 * Copy an InventoryScript item from the Agents Inventory into a primitives task
	 * inventory
	 *
	 * @param objectLocalID
	 *            An unsigned integer representing a primitive being simulated
	 * @param item
	 *            An {@link InventoryItem} which represents a script object from the
	 *            agents inventory
	 * @param enableScript
	 *            true to set the scripts running state to enabled
	 * @return A Unique Transaction ID <example> The following example shows the
	 *         basic steps necessary to copy a script from the agents inventory into
	 *         a tasks inventory and assumes the script exists in the agents
	 *         inventory. <code>
	 *    int primID = 95899503; // Fake prim ID
	 *    UUID scriptID = UUID.Parse("92a7fe8a-e949-dd39-a8d8-1681d8673232"); // Fake Script UUID in Inventory
	 *
	 *    _Client.Inventory.FolderContents(_Client.Inventory.FindFolderForType(AssetType.LSLText), _Client.Self.AgentID,
	 *        false, true, InventorySortOrder.ByName, 10000);
	 *
	 *    _Client.Inventory.RezScript(primID, (InventoryItem)_Client.Inventory.getStore().get(scriptID));
	 * </code> </example> TODO: what does the return UUID correlate to if anything?
	 * @throws Exception
	 */
	public final UUID copyScriptToTask(int objectLocalID, InventoryItem item, boolean enableScript) throws Exception {
		UUID transactionID = new UUID();

		RezScriptPacket ScriptPacket = new RezScriptPacket();
		ScriptPacket.AgentData.AgentID = client.agent.getAgentID();
		ScriptPacket.AgentData.SessionID = client.agent.getSessionID();

		ScriptPacket.UpdateBlock.ObjectLocalID = objectLocalID;
		ScriptPacket.UpdateBlock.Enabled = enableScript;

		ScriptPacket.InventoryBlock.ItemID = item.itemID;
		ScriptPacket.InventoryBlock.FolderID = item.parent.itemID;
		ScriptPacket.InventoryBlock.CreatorID = item.permissions.creatorID;
		ScriptPacket.InventoryBlock.OwnerID = item.permissions.ownerID;
		ScriptPacket.InventoryBlock.GroupID = item.permissions.groupID;
		ScriptPacket.InventoryBlock.BaseMask = item.permissions.baseMask;
		ScriptPacket.InventoryBlock.OwnerMask = item.permissions.ownerMask;
		ScriptPacket.InventoryBlock.GroupMask = item.permissions.groupMask;
		ScriptPacket.InventoryBlock.EveryoneMask = item.permissions.everyoneMask;
		ScriptPacket.InventoryBlock.NextOwnerMask = item.permissions.nextOwnerMask;
		ScriptPacket.InventoryBlock.GroupOwned = item.permissions.isGroupOwned;
		ScriptPacket.InventoryBlock.TransactionID = transactionID;
		ScriptPacket.InventoryBlock.Type = item.assetType.getValue();
		ScriptPacket.InventoryBlock.InvType = item.getType().getValue();
		ScriptPacket.InventoryBlock.Flags = item.itemFlags;
		ScriptPacket.InventoryBlock.SaleType = item.saleType.getValue();
		ScriptPacket.InventoryBlock.SalePrice = item.salePrice;
		ScriptPacket.InventoryBlock.setName(Helpers.stringToBytes(item.name));
		ScriptPacket.InventoryBlock.setDescription(Helpers.stringToBytes(item.description));
		ScriptPacket.InventoryBlock.CreationDate = (int) Helpers.dateTimeToUnixTime(item.creationDate);
		ScriptPacket.InventoryBlock.CRC = itemCRC(item);

		client.network.sendPacket(ScriptPacket);

		return transactionID;
	}

	/**
	 * Request the running status of a script contained in a task (primitive)
	 * inventory The <see cref="ScriptRunningReply"/> event can be used to obtain
	 * the results of the request {@link ScriptRunningReply}
	 *
	 * @param objectID
	 *            The ID of the primitive containing the script
	 * @param scriptID
	 *            The ID of the script
	 * @throws Exception
	 */
	public final void requestGetScriptRunning(UUID objectID, UUID scriptID) throws Exception {
		GetScriptRunningPacket request = new GetScriptRunningPacket();
		request.Script.ObjectID = objectID;
		request.Script.ItemID = scriptID;

		client.network.sendPacket(request);
	}

	/**
	 * Send a request to set the running state of a script contained in a task
	 * (primitive) inventory To verify the change you can use the
	 * <see cref="RequestGetScriptRunning"/> method combined with the
	 * <see cref="ScriptRunningReply"/> event
	 *
	 * @param objectID
	 *            The ID of the primitive containing the script
	 * @param scriptID
	 *            The ID of the script
	 * @param running
	 *            true to set the script running, false to stop a running script
	 * @throws Exception
	 */
	public final void requestSetScriptRunning(UUID objectID, UUID scriptID, boolean running) throws Exception {
		SetScriptRunningPacket request = new SetScriptRunningPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.Script.Running = running;
		request.Script.ItemID = scriptID;
		request.Script.ObjectID = objectID;

		client.network.sendPacket(request);
	}

	// #endregion Task

	// #region Helper Functions

	private int registerItemCreatedCallback(Callback<ItemCreatedCallbackArgs> callback) {
		return registerItemCreatedCallback(callback, -1);
	}

	private int registerItemCreatedCallback(Callback<ItemCreatedCallbackArgs> callback, int id) {
		synchronized (callbacksLock) {
			if (id < 0) {
				if (callbackPos == Integer.MAX_VALUE) {
					callbackPos = 0;
				}
				id = ++callbackPos;
				if (itemCreatedCallbacks.containsKey(id)) {
					logger.warn(GridClient.Log("Overwriting an existing ItemCreatedCallback", client));
				}
				itemCreatedCallbacks.put(id, callback);
			}
			onItemCreatedCallback.add(callback);
			return id;
		}
	}

	private int registerItemsCopiedCallback(Callback<ItemCopiedCallbackArgs> callback) {
		return registerItemsCopiedCallback(callback, -1);
	}

	private int registerItemsCopiedCallback(Callback<ItemCopiedCallbackArgs> callback, int id) {
		synchronized (callbacksLock) {
			if (id <= 0) {
				if (callbackPos == Integer.MAX_VALUE) {
					callbackPos = 0;
				}
				id = ++callbackPos;

				if (itemCopiedCallbacks.containsKey(id)) {
					logger.warn(GridClient.Log("Overwriting an existing ItemsCopiedCallback", client));
				}
				itemCopiedCallbacks.put(id, callback);
			}
			onItemCopiedCallback.add(callback);
			return id;
		}
	}

	/**
	 * Create a CRC from an InventoryItem
	 *
	 * @param iitem
	 *            The source InventoryItem
	 * @return A int representing the source InventoryItem as a CRC
	 */
	public static int itemCRC(InventoryItem iitem) {
		int CRC = 0;

		// IDs
		CRC += iitem.assetID.crc(); // AssetID
		CRC += iitem.parent.itemID.crc(); // FolderID
		CRC += iitem.itemID.crc(); // ItemID

		// Permission stuff
		CRC += iitem.permissions.creatorID.crc(); // CreatorID
		CRC += iitem.permissions.ownerID.crc(); // OwnerID
		CRC += iitem.permissions.groupID.crc(); // GroupID

		// CRC += another 4 words which always seem to be zero -- unclear if
		// this is a UUID or what
		CRC += iitem.permissions.ownerMask; // owner_mask; // Either owner_mask
											// or next_owner_mask may need to be
		CRC += iitem.permissions.nextOwnerMask; // next_owner_mask; // switched
												// with base_mask -- 2 values go
												// here and in my
		CRC += iitem.permissions.everyoneMask; // everyone_mask; // study item,
												// the three were identical.
		CRC += iitem.permissions.groupMask; // group_mask;

		// The rest of the CRC fields
		CRC += iitem.itemFlags; // Flags
		CRC += iitem.getType().getValue(); // InvType
		CRC += iitem.assetType.getValue(); // Type
		CRC += Helpers.dateTimeToUnixTime(iitem.creationDate); // CreationDate
		CRC += iitem.salePrice; // SalePrice
		CRC += (iitem.saleType.getValue() * 0x07073096); // SaleType

		return CRC;
	}

	private InventoryFolder safeCreateInventoryFolder(UUID folderID, UUID ownerID) {
		synchronized (store) {
			InventoryFolder folder = store.getFolder(folderID);
			if (folder == null) {
				folder = new InventoryFolder(folderID, ownerID);
				store.add(folder);
			}
			return folder;
		}
	}

	private InventoryFolder safeCreateInventoryFolder(UUID folderID, UUID parentID, UUID ownerID) {
		synchronized (store) {
			InventoryFolder folder = store.getFolder(folderID);
			if (folder == null) {
				folder = new InventoryFolder(folderID, parentID, ownerID);
				store.add(folder);
			}
			return folder;
		}
	}

	private InventoryItem safeCreateInventoryItem(InventoryType type, UUID itemID, UUID parentID, UUID ownerID) {
		synchronized (store) {
			InventoryItem item = store.getItem(itemID);
			if (item == null) {
				item = InventoryItem.create(type, itemID, parentID, ownerID);
				store.add(item);
			}
			return item;
		}
	}

	private static boolean parseLine(String line, RefObject<String> key, RefObject<String> value) {
		// Clean up and convert tabs to spaces
		line = line.trim();
		line = line.replace('\t', ' ');

		// Shrink all whitespace down to single spaces
		while (line.indexOf("  ") > 0) {
			line = line.replace("  ", " ");
		}

		if (line.length() > 2) {
			int sep = line.indexOf(' ');
			if (sep > 0) {
				key.argvalue = line.substring(0, sep);
				value.argvalue = line.substring(sep + 1);

				return true;
			}
		} else if (line.length() == 1) {
			key.argvalue = line;
			value.argvalue = Helpers.EmptyString;
			return true;
		}

		key.argvalue = null;
		value.argvalue = null;
		return false;
	}

	/**
	 * Parse the results of a RequestTaskInventory() response
	 *
	 * @param taskData
	 *            A string which contains the data from the task reply
	 * @return A List containing the items contained within the tasks inventory
	 */
	public List<InventoryNode> parseTaskInventory(String taskData) {
		List<InventoryNode> items = new ArrayList<>();
		int lineNum = 0;
		String[] lines = taskData.replace("\r\n", "\n").split("\n");
		String key = Helpers.EmptyString;
		String val = Helpers.EmptyString;
		RefObject<String> keyref = new RefObject<>(key);
		RefObject<String> valref = new RefObject<>(val);

		while (lineNum < lines.length) {
			if (parseLine(lines[lineNum++], keyref, valref)) {
				UUID itemID = UUID.ZERO;
				UUID parentID = UUID.ZERO;
				String name = Helpers.EmptyString;

				if (key.equals("inv_object")) {
					FolderType folderType = FolderType.None;
					// In practice this appears to only be used for folders

					while (lineNum < lines.length) {
						if (parseLine(lines[lineNum++], keyref, valref)) {
							if (key.equals("{")) {
								continue;
							} else if (key.equals("}")) {
								break;
							} else if (key.equals("obj_id")) {
								itemID = UUID.parse(val);
							} else if (key.equals("parent_id")) {
								parentID = UUID.parse(val);
							} else if (key.equals("type")) {
								folderType = FolderType.setValue(val);
							} else if (key.equals("name")) {
								name = val.substring(0, val.indexOf('|'));
							}
						}
					}

					InventoryFolder folder = new InventoryFolder(itemID, parentID, client.agent.getAgentID());
					folder.name = name;
					folder.preferredType = folderType;
					items.add(folder);
				} else if (key.equals("inv_item")) {
					AssetType assetType = AssetType.Unknown;

					// Any inventory item that links to an assetID, has
					// permissions, etc
					UUID assetID = UUID.ZERO;
					String desc = Helpers.EmptyString;
					InventoryType inventoryType = InventoryType.Unknown;
					Date creationDate = Helpers.Epoch;
					int flags = 0;
					Permissions perms = Permissions.NoPermissions;
					SaleType saleType = SaleType.Not;
					int salePrice = 0;

					while (lineNum < lines.length) {
						if (parseLine(lines[lineNum++], keyref, valref)) {
							if (key.equals("{")) {
								continue;
							} else if (key.equals("}")) {
								break;
							} else if (key.equals("item_id")) {
								itemID = UUID.parse(val);
							} else if (key.equals("parent_id")) {
								parentID = UUID.parse(val);
							} else if (key.equals("permissions")) {
								while (lineNum < lines.length) {
									if (parseLine(lines[lineNum++], keyref, valref)) {
										if (key.equals("{")) {
											continue;
										} else if (key.equals("}")) {
											break;
										} else if (key.equals("creator_mask")) {
											int i = (int) Helpers.tryParseHex(val);
											if (i != 0) {
												perms.baseMask = i;
											}
										} else if (key.equals("base_mask")) {
											int i = (int) Helpers.tryParseHex(val);
											if (i != 0) {
												perms.baseMask = i;
											}
										} else if (key.equals("owner_mask")) {
											int i = (int) Helpers.tryParseHex(val);
											if (i != 0) {
												perms.ownerMask = i;
											}
										} else if (key.equals("group_mask")) {
											int i = (int) Helpers.tryParseHex(val);
											if (i != 0) {
												perms.groupMask = i;
											}
										} else if (key.equals("everyone_mask")) {
											int i = (int) Helpers.tryParseHex(val);
											if (i != 0) {
												perms.everyoneMask = i;
											}
										} else if (key.equals("next_owner_mask")) {
											int i = (int) Helpers.tryParseHex(val);
											if (i != 0) {
												perms.nextOwnerMask = i;
											}
										} else if (key.equals("creator_id")) {
											perms.creatorID = new UUID(val);
										} else if (key.equals("owner_id")) {
											perms.ownerID = new UUID(val);
										} else if (key.equals("last_owner_id")) {
											perms.lastOwnerID = new UUID(val);
										} else if (key.equals("group_id")) {
											perms.groupID = new UUID(val);
										} else if (key.equals("group_owned")) {
											long i = Helpers.tryParseLong(val);
											if (i != 0) {
												perms.isGroupOwned = (i != 0);
											}
										}
									}
								}
							} else if (key.equals("sale_info")) {
								while (lineNum < lines.length) {
									if (parseLine(lines[lineNum++], keyref, valref)) {
										if (key.equals("{")) {
											continue;
										} else if (key.equals("}")) {
											break;
										} else if (key.equals("sale_type")) {
											saleType = SaleType.setValue(val);
										} else if (key.equals("sale_price")) {
											salePrice = Helpers.tryParseInt(val);
										}
									}
								}
							} else if (key.equals("shadow_id")) {
								assetID = Inventory.decryptShadowID(new UUID(val));
							} else if (key.equals("asset_id")) {
								assetID = new UUID(val);
							} else if (key.equals("type")) {
								assetType = AssetType.valueOf(val);
							} else if (key.equals("inv_type")) {
								inventoryType = InventoryType.valueOf(val);
							} else if (key.equals("flags")) {
								flags = (int) Helpers.tryParseLong(val);
							} else if (key.equals("name")) {
								name = val.substring(0, val.indexOf('|'));
							} else if (key.equals("desc")) {
								desc = val.substring(0, val.indexOf('|'));
							} else if (key.equals("creation_date")) {
								int timestamp = Helpers.tryParseInt(val);
								if (timestamp != 0) {
									creationDate = Helpers.unixTimeToDateTime(timestamp);
								} else {
									logger.warn("Failed to parse creation_date " + val);
								}
							}
						}
					}

					InventoryItem item = InventoryItem.create(inventoryType, itemID, parentID, perms.ownerID);
					item.assetID = assetID;
					item.assetType = assetType;
					item.creationDate = creationDate;
					item.description = desc;
					item.itemFlags = flags;
					item.name = name;
					item.permissions = perms;
					item.salePrice = salePrice;
					item.saleType = saleType;

					items.add(item);
					// #endregion inv_item
				} else {
					logger.error("Unrecognized token " + key + " in: " + taskData);
				}
			}
		}
		return items;
	}

	// /#endregion Helper Functions

	// /#region Internal Callbacks
	// #endregion Internal Handlers

	// #region Packet Handlers
	private final void handleSaveAssetIntoInventory(Packet packet, Simulator simulator) throws Exception {
		SaveAssetIntoInventoryPacket save = (SaveAssetIntoInventoryPacket) packet;
		onSaveAssetToInventory.dispatch(
				new SaveAssetToInventoryCallbackArgs(save.InventoryData.ItemID, save.InventoryData.NewAssetID));
	}

	private final void handleInventoryDescendents(Packet packet, Simulator simulator) throws Exception {
		InventoryDescendentsPacket reply = (InventoryDescendentsPacket) packet;

		synchronized (store) {
			if (reply.AgentData.Descendents > 0) {
				// Iterate folders in this packet
				for (int i = 0; i < reply.FolderData.length; i++) {
					// InventoryDescendantsReply sends a null folder if the
					// parent doesnt contain any folders
					if (reply.FolderData[0].FolderID.equals(UUID.ZERO)) {
						break;
					}
					// If folder already exists then ignore, we assume the version cache
					// logic is working and if the folder is stale then it should not be present.
					else if (!store.containsFolder(reply.FolderData[i].FolderID)) {
						InventoryFolder folder = new InventoryFolder(reply.FolderData[i].FolderID,
								reply.FolderData[i].ParentID, reply.AgentData.OwnerID);
						folder.name = Helpers.bytesToString(reply.FolderData[i].getName());
						folder.preferredType = FolderType.setValue(reply.FolderData[i].Type);
						store.add(folder);
					}
				}

				// Iterate items in this packet
				for (int i = 0; i < reply.ItemData.length; i++) {
					// InventoryDescendantsReply sends a null item if the parent
					// doesnt contain any items.
					if (reply.ItemData[i].ItemID.equals(UUID.ZERO)) {
						break;
					}

					/*
					 * Objects that have been attached in-world prior to being stored on the asset
					 * server are stored with the InventoryType of 0 (Texture) instead of 17
					 * (Attachment)
					 *
					 * This corrects that behavior by forcing Object Asset types that have an
					 * invalid InventoryType with the proper InventoryType of Attachment.
					 */
					InventoryType invType = InventoryType.setValue(reply.ItemData[i].InvType);
					if (AssetType.Object.equals(AssetType.setValue(reply.ItemData[i].Type))
							&& InventoryType.Texture.equals(invType)) {
						invType = InventoryType.Attachment;
					}
					InventoryItem item = InventoryItem.create(invType, reply.ItemData[i].ItemID,
							reply.ItemData[i].FolderID, reply.AgentData.OwnerID);
					item.name = Helpers.bytesToString(reply.ItemData[i].getName());
					item.assetType = AssetType.setValue(reply.ItemData[i].Type);
					item.assetID = reply.ItemData[i].AssetID;
					item.creationDate = Helpers.unixTimeToDateTime(reply.ItemData[i].CreationDate);
					item.description = Helpers.bytesToString(reply.ItemData[i].getDescription());
					item.itemFlags = reply.ItemData[i].Flags;
					item.permissions = new Permissions(reply.ItemData[i].CreatorID, reply.ItemData[i].OwnerID, null,
							reply.ItemData[i].GroupID, reply.ItemData[i].GroupOwned, reply.ItemData[i].BaseMask,
							reply.ItemData[i].EveryoneMask, reply.ItemData[i].GroupMask,
							reply.ItemData[i].NextOwnerMask, reply.ItemData[i].OwnerMask);
					item.salePrice = reply.ItemData[i].SalePrice;
					item.saleType = SaleType.setValue(reply.ItemData[i].SaleType);
					store.add(item);
				}
			}

			InventoryFolder parent = null;

			if (store.containsFolder(reply.AgentData.FolderID)) {
				parent = store.getFolder(reply.AgentData.FolderID);
			}

			if (parent == null) {
				logger.error(GridClient.Log("Don't have a reference to FolderID " + reply.AgentData.FolderID.toString()
						+ " or it is not a folder", client));
				return;
			}

			if (reply.AgentData.Version < parent.version) {
				logger.warn(GridClient.Log("Got an outdated InventoryDescendents packet for folder " + parent.name
						+ ", this version = " + reply.AgentData.Version + ", latest version = " + parent.version,
						client));
				return;
			}

			parent.version = reply.AgentData.Version;
			// FIXME: reply.AgentData.Descendants is not parentFolder.DescendentCount
			// if we didn't request items and folders
			parent.descendentCount = reply.AgentData.Descendents;

			// #region FindObjectByPath Handling

			if (searches.size() > 0) {
				List<InventorySearch> remaining = new ArrayList<>();

				synchronized (searches) {
					// Iterate over all of the outstanding searches
					for (int i = 0; i < searches.size(); i++) {
						InventorySearch search = searches.get(i);
						List<InventoryNode> folderContents;
						folderContents = store.getContents(search.folder);

						// Iterate over all of the inventory objects in the base
						// search folder
						for (int j = 0; j < folderContents.size(); j++) {
							// Check if this inventory object matches the
							// current path node
							if (folderContents.get(j).name.equals(search.path[search.level])) {
								String string = "";
								int k = 0;
								for (; k < search.path.length - 1; k++) {
									string.concat(search.path[k] + "/");
								}
								string.concat(search.path[k]);

								if (search.level == search.path.length - 1) {
									logger.debug(GridClient.Log("Finished path search of " + string, client));

									// This is the last node in the path, fire
									// the callback and clean up
									onFindObjectByPathReply.dispatch(new FindObjectByPathReplyCallbackArgs(string,
											folderContents.get(j).itemID));
									break;
								}

								// We found a match but it is not the end of the
								// path, request the next level
								logger.debug(GridClient.Log(String.format("Matched level %d/%d in a path search of %s",
										search.level, search.path.length - 1, string), client));

								search.folder = folderContents.get(j).itemID;
								search.level++;
								remaining.add(search);

								requestFolderContents(search.folder, search.owner, true, true,
										InventorySortOrder.ByName, false);
							}
						}
					}
					searches = remaining;
				}
			}
			// #endregion FindObjectByPath Handling

			// Callback for inventory folder contents being updated
			onFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(parent.itemID, true));
		}
	}

	/**
	 * UpdateCreateInventoryItem packets are received when a new inventory item is
	 * created. This may occur when an object that's rezzed in world is taken into
	 * inventory, when an item is created using the CreateInventoryItem packet, or
	 * when an object is purchased
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The CallbackArgs object containing the packet data
	 */
	private final void handleUpdateCreateInventoryItem(Packet packet, Simulator simulator) throws Exception {
		UpdateCreateInventoryItemPacket reply = (UpdateCreateInventoryItemPacket) packet;

		for (UpdateCreateInventoryItemPacket.InventoryDataBlock dataBlock : reply.InventoryData) {
			if (dataBlock.InvType == InventoryType.Folder.getValue()) {
				logger.error(GridClient.Log(
						"Received InventoryFolder in an UpdateCreateInventoryItem packet, this should not happen!",
						client));
				continue;
			}

			InventoryItem item = InventoryItem.create(InventoryType.setValue(dataBlock.InvType), dataBlock.ItemID,
					dataBlock.FolderID, dataBlock.OwnerID);
			item.name = Helpers.bytesToString(dataBlock.getName());
			item.assetType = AssetType.setValue(dataBlock.Type);
			item.assetID = dataBlock.AssetID;
			item.creationDate = Helpers.unixTimeToDateTime(dataBlock.CreationDate);
			item.description = Helpers.bytesToString(dataBlock.getDescription());
			item.itemFlags = dataBlock.Flags;
			item.permissions = new Permissions(dataBlock.CreatorID, dataBlock.OwnerID, null, dataBlock.GroupID,
					dataBlock.GroupOwned, dataBlock.BaseMask, dataBlock.EveryoneMask, dataBlock.GroupMask,
					dataBlock.NextOwnerMask, dataBlock.OwnerMask);
			item.salePrice = dataBlock.SalePrice;
			item.saleType = SaleType.setValue(dataBlock.SaleType);

			/*
			 * When attaching new objects, an UpdateCreateInventoryItem packet will be
			 * returned by the server that has a FolderID/ParentUUID of zero. It is up to
			 * the client to make sure that the item gets a good folder, otherwise it will
			 * end up inaccessible in inventory.
			 */
			if (dataBlock.FolderID.equals(UUID.ZERO)) {
				// assign default folder for type
				item.parent = findFolderForType(item.assetType);

				logger.info(
						"Received an item through UpdateCreateInventoryItem with no parent folder, assigning to folder "
								+ item.parent.itemID);

				// send update to the sim
				requestUpdateItem(item);
			}

			synchronized (store) {
				// Update the local copy
				store.add(item);
			}

			// Look for an "item created" callback

			if (itemCreatedCallbacks.containsKey(dataBlock.CallbackID)) {
				Callback<ItemCreatedCallbackArgs> callback = itemCreatedCallbacks.remove(dataBlock.CallbackID);

				try {
					callback.callback(new ItemCreatedCallbackArgs(true, item));
				} catch (Throwable ex) {
					logger.error(GridClient.Log(ex.getMessage(), client), ex);
				}
			}

			// TODO: Is this callback even triggered when items are copied?
			// Look for an "item copied" callback

			if (itemCopiedCallbacks.containsKey(dataBlock.CallbackID)) {
				Callback<ItemCopiedCallbackArgs> callback = itemCopiedCallbacks.remove(dataBlock.CallbackID);

				try {
					callback.callback(new ItemCopiedCallbackArgs(item));
				} catch (Throwable ex) {
					logger.error(GridClient.Log(ex.getMessage(), client), ex);
				}
			}

			// This is triggered when an item is received from a task
			onTaskItemReceived.dispatch(new TaskItemReceivedCallbackArgs(item.itemID, dataBlock.FolderID,
					item.permissions.creatorID, item.assetID, item.getType()));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handleMoveInventoryItem(Packet packet, Simulator simulator) throws Exception {
		MoveInventoryItemPacket move = (MoveInventoryItemPacket) packet;

		for (MoveInventoryItemPacket.InventoryDataBlock block : move.InventoryData) {
			InventoryNode node = store.getNode(block.ItemID);
			if (block.getNewName().length > 0)
				node.name = Helpers.bytesToString(block.getNewName());
			store.add(block.FolderID, node);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handleBulkUpdateInventory(Packet packet, Simulator simulator) throws Exception {
		BulkUpdateInventoryPacket update = (BulkUpdateInventoryPacket) packet;

		if (update.FolderData.length > 0 && !update.FolderData[0].FolderID.equals(UUID.ZERO)) {
			synchronized (store) {
				for (BulkUpdateInventoryPacket.FolderDataBlock dataBlock : update.FolderData) {
					InventoryFolder folder;
					if (!store.containsFolder(dataBlock.FolderID)) {
						folder = new InventoryFolder(dataBlock.FolderID, dataBlock.ParentID, update.AgentData.AgentID);
					} else {
						folder = store.getFolder(dataBlock.FolderID);
						folder.parentID = dataBlock.ParentID;
						folder.ownerID = update.AgentData.AgentID;
					}
					if (dataBlock.getName() != null)
						folder.name = Helpers.bytesToString(dataBlock.getName());
					folder.preferredType = FolderType.setValue(dataBlock.Type);
					store.add(folder);
				}
			}
		}

		if (update.ItemData.length > 0 && !update.ItemData[0].ItemID.equals(UUID.ZERO)) {
			for (int i = 0; i < update.ItemData.length; i++) {
				BulkUpdateInventoryPacket.ItemDataBlock dataBlock = update.ItemData[i];
				InventoryItem item = safeCreateInventoryItem(InventoryType.setValue(dataBlock.InvType),
						dataBlock.ItemID, dataBlock.FolderID, dataBlock.OwnerID);

				item.assetType = AssetType.setValue(dataBlock.Type);
				if (!dataBlock.AssetID.equals(UUID.ZERO)) {
					item.assetID = dataBlock.AssetID;
				}
				item.creationDate = Helpers.unixTimeToDateTime(dataBlock.CreationDate);
				item.description = Helpers.bytesToString(dataBlock.getDescription());
				item.itemFlags = dataBlock.Flags;
				item.name = Helpers.bytesToString(dataBlock.getName());
				item.permissions = new Permissions(dataBlock.CreatorID, dataBlock.OwnerID, null, dataBlock.GroupID,
						dataBlock.GroupOwned, dataBlock.BaseMask, dataBlock.EveryoneMask, dataBlock.GroupMask,
						dataBlock.NextOwnerMask, dataBlock.OwnerMask);
				item.salePrice = dataBlock.SalePrice;
				item.saleType = SaleType.setValue(dataBlock.SaleType);

				// Look for an "item created" callback
				if (itemCreatedCallbacks.containsKey(dataBlock.CallbackID)) {
					Callback<ItemCreatedCallbackArgs> callback = itemCreatedCallbacks.remove(dataBlock.CallbackID);

					try {
						callback.callback(new ItemCreatedCallbackArgs(true, item));
					} catch (Throwable ex) {
						logger.error(GridClient.Log(ex.getMessage(), client), ex);
					}
				}

				// Look for an "item copied" callback
				if (itemCopiedCallbacks.containsKey(dataBlock.CallbackID)) {
					Callback<ItemCopiedCallbackArgs> callback = itemCopiedCallbacks.remove(dataBlock.CallbackID);

					try {
						callback.callback(new ItemCopiedCallbackArgs(item));
					} catch (Throwable ex) {
						logger.error(GridClient.Log(ex.getMessage(), client), ex);
					}
				}
			}
		}
	}

	private final void handleBulkUpdateInventory(IMessage message, Simulator simulator) {
		BulkUpdateInventoryMessage msg = (BulkUpdateInventoryMessage) message;

		for (BulkUpdateInventoryMessage.FolderDataInfo newFolder : msg.folderData) {
			if (UUID.isZeroOrNull(newFolder.folderID))
				continue;

			synchronized (store) {
				InventoryFolder folder;

				if (!store.containsFolder(newFolder.folderID)) {
					folder = new InventoryFolder(newFolder.folderID, newFolder.parentID, msg.agentID);
				} else {
					folder = store.getFolder(newFolder.folderID);
					folder.parentID = newFolder.parentID;
				}

				folder.name = newFolder.name;
				folder.preferredType = newFolder.type;
				store.add(folder);
			}
		}

		for (BulkUpdateInventoryMessage.ItemDataInfo newItem : msg.itemData) {
			if (UUID.isZeroOrNull(newItem.itemID))
				continue;
			InventoryType invType = newItem.inventoryType;
			synchronized (itemInventoryTypeRequest) {
				if (itemInventoryTypeRequest.containsKey(newItem.callbackID)) {
					invType = itemInventoryTypeRequest.remove(newItem.callbackID);
				}
			}
			InventoryItem item = safeCreateInventoryItem(invType, newItem.itemID, newItem.folderID, newItem.ownerID);

			item.assetType = newItem.assetType;
			item.assetID = newItem.assetID;
			item.creationDate = newItem.creationDate;
			item.description = newItem.description;
			item.itemFlags = newItem.flags;
			item.name = newItem.name;
			item.permissions = new Permissions(newItem.creatorID, newItem.ownerID, null, newItem.groupID,
					newItem.groupOwned, newItem.baseMask, newItem.everyoneMask, newItem.groupMask,
					newItem.nextOwnerMask, newItem.ownerMask);
			item.salePrice = newItem.salePrice;
			item.saleType = newItem.saleType;

			// Look for an "item created" callback
			if (itemCreatedCallbacks.containsKey(newItem.callbackID)) {
				Callback<ItemCreatedCallbackArgs> callback = itemCreatedCallbacks.remove(newItem.callbackID);

				try {
					callback.callback(new ItemCreatedCallbackArgs(true, item));
				} catch (Throwable ex) {
					logger.error(GridClient.Log(ex.getMessage(), client), ex);
				}
			}

			// Look for an "item copied" callback
			if (itemCopiedCallbacks.containsKey(newItem.callbackID)) {
				Callback<ItemCopiedCallbackArgs> callback = itemCopiedCallbacks.remove(newItem.callbackID);

				try {
					callback.callback(new ItemCopiedCallbackArgs(item));
				} catch (Throwable ex) {
					logger.error(GridClient.Log(ex.getMessage(), client), ex);
				}
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handleFetchInventoryReply(Packet packet, Simulator simulator) throws Exception {
		FetchInventoryReplyPacket reply = (FetchInventoryReplyPacket) packet;

		for (FetchInventoryReplyPacket.InventoryDataBlock dataBlock : reply.InventoryData) {
			if (dataBlock.InvType == InventoryType.Folder.getValue()) {
				logger.error(GridClient
						.Log("Received FetchInventoryReply for an inventory folder, this should not happen!", client));
				continue;
			}

			InventoryItem item = InventoryItem.create(InventoryType.setValue(dataBlock.InvType), dataBlock.ItemID,
					dataBlock.FolderID, dataBlock.OwnerID);
			item.assetType = AssetType.setValue(dataBlock.Type);
			item.assetID = dataBlock.AssetID;
			item.creationDate = Helpers.unixTimeToDateTime(dataBlock.CreationDate);
			item.description = Helpers.bytesToString(dataBlock.getDescription());
			item.itemFlags = dataBlock.Flags;
			item.name = Helpers.bytesToString(dataBlock.getName());
			item.permissions = new Permissions(dataBlock.CreatorID, dataBlock.OwnerID, null, dataBlock.GroupID,
					dataBlock.GroupOwned, dataBlock.BaseMask, dataBlock.EveryoneMask, dataBlock.GroupMask,
					dataBlock.NextOwnerMask, dataBlock.OwnerMask);
			item.salePrice = dataBlock.SalePrice;
			item.saleType = SaleType.setValue(dataBlock.SaleType);

			store.add(item);

			// Fire the callback for an item being fetched
			onItemReceived.dispatch(new ItemReceivedCallbackArgs(item));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handleReplyTaskInventory(Packet packet, Simulator simulator) throws Exception {
		ReplyTaskInventoryPacket reply = (ReplyTaskInventoryPacket) packet;
		onTaskInventoryReply.dispatch(new TaskInventoryReplyCallbackArgs(reply.InventoryData.TaskID,
				reply.InventoryData.Serial, Helpers.bytesToString(reply.InventoryData.getFilename())));
	}

	private final void handleScriptRunningReply(IMessage message, Simulator simulator) {
		ScriptRunningReplyMessage msg = (ScriptRunningReplyMessage) message;
		onScriptRunningReply
				.dispatch(new ScriptRunningReplyCallbackArgs(msg.objectID, msg.itemID, msg.mono, msg.running));
	}

	// #endregion Packet Handlers

}
