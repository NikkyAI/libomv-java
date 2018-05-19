package libomv.model.object;

import libomv.primitives.MediaEntry;
import libomv.utils.CallbackArgs;

public class ObjectMediaCallbackArgs implements CallbackArgs {
	private boolean success;
	private String version;
	private MediaEntry[] faceMedia;

	public ObjectMediaCallbackArgs(boolean success, String version, MediaEntry[] faceMedia) {
		this.success = success;
		this.version = version;
		this.faceMedia = faceMedia;
	}

	// Indicates if the operation was successful
	public final boolean getSuccess() {
		return success;
	}

	public final void setSuccess(boolean value) {
		success = value;
	}

	// Media version string
	public final String getVersion() {
		return version;
	}

	public final void setVersion(String value) {
		version = value;
	}

	// Array of media entries indexed by face number
	public final MediaEntry[] getFaceMedia() {
		return faceMedia;
	}

	public final void setFaceMedia(MediaEntry[] value) {
		faceMedia = value;
	}

}