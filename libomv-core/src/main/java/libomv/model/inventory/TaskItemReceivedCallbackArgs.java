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
	private final UUID itemID;
	private final UUID folderID;
	private final UUID creatorID;
	private final UUID assetID;
	private final InventoryType type;

	public TaskItemReceivedCallbackArgs(UUID itemID, UUID folderID, UUID creatorID, UUID assetID, InventoryType type) {
		this.itemID = itemID;
		this.folderID = folderID;
		this.creatorID = creatorID;
		this.assetID = assetID;
		this.type = type;
	}

	public final UUID getItemID() {
		return itemID;
	}

	public final UUID getFolderID() {
		return folderID;
	}

	public final UUID getCreatorID() {
		return creatorID;
	}

	public final UUID getAssetID() {
		return assetID;
	}

	public final InventoryType getType() {
		return type;
	}

}