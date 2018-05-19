package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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