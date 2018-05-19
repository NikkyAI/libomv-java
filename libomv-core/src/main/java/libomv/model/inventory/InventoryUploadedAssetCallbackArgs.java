package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Reply received when uploading an inventory asset
 *
 * @param success
 *            Has upload been successful
 * @param status
 *            Error message if upload failed
 * @param itemID
 *            Inventory asset UUID
 * @param assetID
 *            New asset UUID
 */
public class InventoryUploadedAssetCallbackArgs implements CallbackArgs {
	public boolean success;
	public String status;
	public UUID itemID;
	public UUID assetID;

	public InventoryUploadedAssetCallbackArgs(boolean success, String status, UUID itemID, UUID assetID) {
		this.success = success;
		this.status = status;
		this.itemID = itemID;
		this.assetID = assetID;
	}

}