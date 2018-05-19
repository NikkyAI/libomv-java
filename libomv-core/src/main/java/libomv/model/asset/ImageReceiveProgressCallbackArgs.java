package libomv.model.asset;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Provides data for ImageReceiveProgress event
public class ImageReceiveProgressCallbackArgs implements CallbackArgs {
	private final UUID imageID;
	private final long received;
	private final long total;

	public ImageReceiveProgressCallbackArgs(UUID imageID, long received, long total) {
		this.imageID = imageID;
		this.received = received;
		this.total = total;
	}

	// UUID of the image that is in progress
	public final UUID getImageID() {
		return imageID;
	}

	// Number of bytes received so far
	public final long getReceived() {
		return received;
	}

	// Image size in bytes
	public final long getTotal() {
		return total;
	}

}