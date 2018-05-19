package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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