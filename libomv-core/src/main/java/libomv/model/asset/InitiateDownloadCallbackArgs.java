package libomv.model.asset;

import libomv.utils.CallbackArgs;

// Provides data for InitiateDownloaded event
public class InitiateDownloadCallbackArgs implements CallbackArgs {
	private final String simFileName;
	private final String viewerFileName;

	public InitiateDownloadCallbackArgs(String simFilename, String viewerFilename) {
		this.simFileName = simFilename;
		this.viewerFileName = viewerFilename;
	}

	// Filename used on the simulator
	public final String getSimFileName() {
		return simFileName;
	}

	// Filename used by the client
	public final String getViewerFileName() {
		return viewerFileName;
	}

}