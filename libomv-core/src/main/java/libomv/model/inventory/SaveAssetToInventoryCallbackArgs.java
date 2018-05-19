package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class SaveAssetToInventoryCallbackArgs implements CallbackArgs {
	private final UUID itemID;
	private final UUID newAssetID;

	public SaveAssetToInventoryCallbackArgs(UUID itemID, UUID newAssetID) {
		this.itemID = itemID;
		this.newAssetID = newAssetID;
	}

	public final UUID getItemID() {
		return itemID;
	}

	public final UUID getNewAssetID() {
		return newAssetID;
	}

}