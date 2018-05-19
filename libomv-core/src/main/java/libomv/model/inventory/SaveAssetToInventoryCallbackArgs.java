package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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