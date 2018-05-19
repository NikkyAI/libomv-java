package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class FindObjectByPathReplyCallbackArgs implements CallbackArgs {
	private final String path;
	private final UUID inventoryObjectID;

	public FindObjectByPathReplyCallbackArgs(String path, UUID inventoryObjectID) {
		this.path = path;
		this.inventoryObjectID = inventoryObjectID;
	}

	public final String getPath() {
		return path;
	}

	public final UUID getInventoryObjectID() {
		return inventoryObjectID;
	}

}