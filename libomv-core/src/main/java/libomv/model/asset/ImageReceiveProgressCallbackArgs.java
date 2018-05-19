package libomv.model.asset;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Provides data for ImageReceiveProgress event
public class ImageReceiveProgressCallbackArgs implements CallbackArgs {
	private final UUID m_ImageID;
	private final long m_Received;
	private final long m_Total;

	// UUID of the image that is in progress
	public final UUID getImageID() {
		return m_ImageID;
	}

	// Number of bytes received so far
	public final long getReceived() {
		return m_Received;
	}

	// Image size in bytes
	public final long getTotal() {
		return m_Total;
	}

	public ImageReceiveProgressCallbackArgs(UUID imageID, long received, long total) {
		this.m_ImageID = imageID;
		this.m_Received = received;
		this.m_Total = total;
	}
}