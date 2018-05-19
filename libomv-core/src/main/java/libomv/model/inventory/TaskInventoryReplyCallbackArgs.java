package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class TaskInventoryReplyCallbackArgs implements CallbackArgs {
	private final UUID itemID;
	private final short serial;
	private final String assetFilename;

	public TaskInventoryReplyCallbackArgs(UUID itemID, short serial, String assetFilename) {
		this.itemID = itemID;
		this.serial = serial;
		this.assetFilename = assetFilename;
	}

	public final UUID getItemID() {
		return itemID;
	}

	public final short getSerial() {
		return serial;
	}

	public final String getAssetFilename() {
		return assetFilename;
	}

}