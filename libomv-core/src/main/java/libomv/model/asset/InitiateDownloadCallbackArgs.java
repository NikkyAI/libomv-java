package libomv.model.asset;

import libomv.utils.CallbackArgs;

// Provides data for InitiateDownloaded event
public class InitiateDownloadCallbackArgs implements CallbackArgs {
	private final String m_SimFileName;
	private final String m_ViewerFileName;

	// Filename used on the simulator
	public final String getSimFileName() {
		return m_SimFileName;
	}

	// Filename used by the client
	public final String getViewerFileName() {
		return m_ViewerFileName;
	}

	public InitiateDownloadCallbackArgs(String simFilename, String viewerFilename) {
		this.m_SimFileName = simFilename;
		this.m_ViewerFileName = viewerFilename;
	}
}