package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class FolderUpdatedCallbackArgs implements CallbackArgs {
	private final UUID folderID;
	private final boolean success;

	public FolderUpdatedCallbackArgs(UUID folderID, boolean success) {
		this.folderID = folderID;
		this.success = success;
	}

	public final UUID getFolderID() {
		return folderID;
	}

	public final boolean getSuccess() {
		return success;
	}

}