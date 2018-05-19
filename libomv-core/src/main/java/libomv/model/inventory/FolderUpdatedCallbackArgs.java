package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class FolderUpdatedCallbackArgs implements CallbackArgs {
	private final UUID m_FolderID;
	private final boolean success;

	public final UUID getFolderID() {
		return m_FolderID;
	}

	public final boolean getSuccess() {
		return success;
	}

	public FolderUpdatedCallbackArgs(UUID folderID, boolean success) {
		this.m_FolderID = folderID;
		this.success = success;
	}
}