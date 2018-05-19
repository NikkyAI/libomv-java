package libomv.model;

import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.model.Agent.InstantMessage;
import libomv.model.Asset.AssetType;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Original code came from InventoryManager
public interface Inventory {

	/* Used for converting shadow_id to asset_id */
	public static final UUID MAGIC_ID = new UUID("3c115e51-04f4-523c-9fa6-98aff1034730");

	/**
	 * Reverses a cheesy XORing with a fixed UUID to convert a shadow_id to an
	 * asset_id
	 *
	 * @param shadowID
	 *            Obfuscated shadow_id value
	 * @return Deobfuscated asset_id value
	 */
	public static UUID decryptShadowID(UUID shadowID) {
		UUID uuid = new UUID(shadowID);
		uuid.XOr(MAGIC_ID);
		return uuid;
	}

	/**
	 * Does a cheesy XORing with a fixed UUID to convert an asset_id to a shadow_id
	 *
	 * @param assetID
	 *            asset_id value to obfuscate
	 * @return Obfuscated shadow_id value
	 */
	public static UUID encryptAssetID(UUID assetID) {
		UUID uuid = new UUID(assetID);
		uuid.XOr(MAGIC_ID);
		return uuid;
	}

	// [Flags]
	public static class InventorySortOrder {
		/* Sort by name */
		public static final byte ByName = 0;
		/* Sort by date */
		public static final byte ByDate = 1;
		/*
		 * Sort folders by name, regardless of whether items are sorted by name or date
		 */
		public static final byte FoldersByName = 2;
		/* Place system folders at the top */
		public static final byte SystemFoldersToTop = 4;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static int getValue(byte value) {
			return value & _mask;
		}

		private static final byte _mask = 0x7;
	}

	/* Possible destinations for DeRezObject request */
	public enum DeRezDestination {
		/* */
		AgentInventorySave,
		/* Copy from in-world to agent inventory */
		AgentInventoryCopy,
		/* Derez to TaskInventory */
		TaskInventory,
		/* */
		Attachment,
		/* Take Object */
		AgentInventoryTake,
		/* */
		ForceToGodInventory,
		/* Delete Object */
		TrashFolder,
		/* Put an avatar attachment into agent inventory */
		AttachmentToInventory,
		/* */
		AttachmentExists,
		/* Return an object back to the owner's inventory */
		ReturnToOwner,
		/* Return a deeded object back to the last owner's inventory */
		ReturnToLastOwner;

		public static DeRezDestination setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/**
	 * Callback for inventory item creation finishing
	 *
	 * @param success
	 *            Whether the request to create an inventory item succeeded or not
	 * @param item
	 *            Inventory item being created. If success is false this will be
	 *            null
	 */
	public class ItemCreatedCallbackArgs implements CallbackArgs {
		boolean success;
		InventoryItem item;

		public boolean getSuccess() {
			return success;
		}

		public InventoryItem getInventoryItem() {
			return item;
		}

		public ItemCreatedCallbackArgs(boolean success, InventoryItem item) {
			this.success = success;
			this.item = item;
		}
	}

	/**
	 * Callback for an inventory item being create from an uploaded asset
	 *
	 * @param success
	 *            true if inventory item creation was successful
	 * @param status
	 * @param itemID
	 * @param assetID
	 */
	public class ItemCreatedFromAssetCallbackArgs implements CallbackArgs {
		boolean success;
		String status;
		UUID itemID;
		UUID assetID;

		public boolean getSuccess() {
			return success;
		}

		public String getStatus() {
			return status;
		}

		public UUID getItemID() {
			return itemID;
		}

		public UUID getAssetID() {
			return assetID;
		}

		public ItemCreatedFromAssetCallbackArgs(boolean success, String status, UUID itemID, UUID assetID) {
			this.success = success;
			this.status = status;
			this.itemID = itemID;
			this.assetID = assetID;
		}
	}

	/**
	 * Callback for an inventory item copying finished
	 *
	 * @param item
	 *            InventoryItem being copied
	 */
	public class ItemCopiedCallbackArgs implements CallbackArgs {
		InventoryItem item;

		public InventoryItem getInventoryItem() {
			return item;
		}

		public ItemCopiedCallbackArgs(InventoryItem item) {
			this.item = item;
		}
	}

	// #region CallbackArgs

	public class InventoryObjectOfferedCallbackArgs implements CallbackArgs {
		private final InstantMessage m_Offer;
		private final AssetType m_AssetType;
		private final UUID m_ObjectID;
		private final boolean m_FromTask;
		private boolean m_Accept;
		private UUID m_FolderID;

		/*
		 * Set to true to accept offer, false to decline it
		 */
		public final boolean getAccept() {
			return m_Accept;
		}

		public final void setAccept(boolean value) {
			m_Accept = value;
		}

		/*
		 * The folder to accept the inventory into, if null default folder for <see
		 * cref="AssetType"/> will be used
		 */
		public final UUID getFolderID() {
			return m_FolderID;
		}

		public final void setFolderID(UUID value) {
			m_FolderID = value;
		}

		public final InstantMessage getOffer() {
			return m_Offer;
		}

		public final AssetType getAssetType() {
			return m_AssetType;
		}

		public final UUID getObjectID() {
			return m_ObjectID;
		}

		public final boolean getFromTask() {
			return m_FromTask;
		}

		public InventoryObjectOfferedCallbackArgs(InstantMessage offerDetails, AssetType type, UUID objectID,
				boolean fromTask, UUID folderID) {
			this.m_Accept = false;
			this.m_FolderID = folderID;
			this.m_Offer = offerDetails;
			this.m_AssetType = type;
			this.m_ObjectID = objectID;
			this.m_FromTask = fromTask;
		}
	}

	public class FolderUpdatedCallbackArgs implements CallbackArgs {
		private final UUID m_FolderID;
		private final boolean success;

		public final UUID getFolderID() {
			return m_FolderID;
		}

		public final boolean getSuccess() {
			return success;
		}

		public FolderUpdatedCallbackArgs(UUID folderID, boolean success) {
			this.m_FolderID = folderID;
			this.success = success;
		}
	}

	public class ItemReceivedCallbackArgs implements CallbackArgs {
		private final InventoryItem m_Item;

		public final InventoryItem getItem() {
			return m_Item;
		}

		public ItemReceivedCallbackArgs(InventoryItem item) {
			this.m_Item = item;
		}
	}

	public class FindObjectByPathReplyCallbackArgs implements CallbackArgs {
		private final String m_Path;
		private final UUID m_InventoryObjectID;

		public final String getPath() {
			return m_Path;
		}

		public final UUID getInventoryObjectID() {
			return m_InventoryObjectID;
		}

		public FindObjectByPathReplyCallbackArgs(String path, UUID inventoryObjectID) {
			this.m_Path = path;
			this.m_InventoryObjectID = inventoryObjectID;
		}
	}

	/**
	 * Callback when an inventory object is accepted and received from a task
	 * inventory. This is the callback in which you actually get the ItemID, as in
	 * ObjectOfferedCallback it is null when received from a task.
	 */
	public class TaskItemReceivedCallbackArgs implements CallbackArgs {
		private final UUID m_ItemID;
		private final UUID m_FolderID;
		private final UUID m_CreatorID;
		private final UUID m_AssetID;
		private final InventoryType m_Type;

		public final UUID getItemID() {
			return m_ItemID;
		}

		public final UUID getFolderID() {
			return m_FolderID;
		}

		public final UUID getCreatorID() {
			return m_CreatorID;
		}

		public final UUID getAssetID() {
			return m_AssetID;
		}

		public final InventoryType getType() {
			return m_Type;
		}

		public TaskItemReceivedCallbackArgs(UUID itemID, UUID folderID, UUID creatorID, UUID assetID,
				InventoryType type) {
			this.m_ItemID = itemID;
			this.m_FolderID = folderID;
			this.m_CreatorID = creatorID;
			this.m_AssetID = assetID;
			this.m_Type = type;
		}
	}

	public class TaskInventoryReplyCallbackArgs implements CallbackArgs {
		private final UUID m_ItemID;
		private final short m_Serial;
		private final String m_AssetFilename;

		public final UUID getItemID() {
			return m_ItemID;
		}

		public final short getSerial() {
			return m_Serial;
		}

		public final String getAssetFilename() {
			return m_AssetFilename;
		}

		public TaskInventoryReplyCallbackArgs(UUID itemID, short serial, String assetFilename) {
			this.m_ItemID = itemID;
			this.m_Serial = serial;
			this.m_AssetFilename = assetFilename;
		}
	}

	public class SaveAssetToInventoryCallbackArgs implements CallbackArgs {
		private final UUID m_ItemID;
		private final UUID m_NewAssetID;

		public final UUID getItemID() {
			return m_ItemID;
		}

		public final UUID getNewAssetID() {
			return m_NewAssetID;
		}

		public SaveAssetToInventoryCallbackArgs(UUID itemID, UUID newAssetID) {
			this.m_ItemID = itemID;
			this.m_NewAssetID = newAssetID;
		}
	}

	public class ScriptRunningReplyCallbackArgs implements CallbackArgs {
		private final UUID m_ObjectID;
		private final UUID m_ScriptID;
		private final boolean m_IsMono;
		private final boolean m_IsRunning;

		public final UUID getObjectID() {
			return m_ObjectID;
		}

		public final UUID getScriptID() {
			return m_ScriptID;
		}

		public final boolean getIsMono() {
			return m_IsMono;
		}

		public final boolean getIsRunning() {
			return m_IsRunning;
		}

		public ScriptRunningReplyCallbackArgs(UUID objectID, UUID sctriptID, boolean isMono, boolean isRunning) {
			this.m_ObjectID = objectID;
			this.m_ScriptID = sctriptID;
			this.m_IsMono = isMono;
			this.m_IsRunning = isRunning;
		}
	}
}
