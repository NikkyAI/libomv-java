package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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