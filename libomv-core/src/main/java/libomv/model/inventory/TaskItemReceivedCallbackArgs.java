package libomv.model.inventory;

import libomv.inventory.InventoryNode.InventoryType;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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