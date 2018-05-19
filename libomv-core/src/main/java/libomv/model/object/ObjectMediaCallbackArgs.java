package libomv.model.object;

import libomv.primitives.MediaEntry;
import libomv.utils.CallbackArgs;

public class ObjectMediaCallbackArgs implements CallbackArgs {
	private boolean m_Success;
	private String m_Version;
	private MediaEntry[] m_FaceMedia;

	// Indicates if the operation was successful
	public final boolean getSuccess() {
		return m_Success;
	}

	public final void setSuccess(boolean value) {
		m_Success = value;
	}

	// Media version string
	public final String getVersion() {
		return m_Version;
	}

	public final void setVersion(String value) {
		m_Version = value;
	}

	// Array of media entries indexed by face number
	public final MediaEntry[] getFaceMedia() {
		return m_FaceMedia;
	}

	public final void setFaceMedia(MediaEntry[] value) {
		m_FaceMedia = value;
	}

	public ObjectMediaCallbackArgs(boolean success, String version, MediaEntry[] faceMedia) {
		this.m_Success = success;
		this.m_Version = version;
		this.m_FaceMedia = faceMedia;
	}
}