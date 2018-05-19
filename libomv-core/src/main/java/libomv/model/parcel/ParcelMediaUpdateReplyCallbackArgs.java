package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains data when the media data for a parcel the avatar is on changes
public class ParcelMediaUpdateReplyCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final ParcelMedia parcelMedia;

	/**
	 * Construct a new instance of the ParcelMediaUpdateReplyCallbackArgs class
	 *
	 * @param simulator
	 *            the simulator the parcel media data was updated in
	 * @param media
	 *            The updated media information
	 */
	public ParcelMediaUpdateReplyCallbackArgs(Simulator simulator, ParcelMedia media) {
		this.simulator = simulator;
		this.parcelMedia = media;
	}

	// Get the simulator the parcel media data was updated in
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the updated media information
	public final ParcelMedia getMedia() {
		return parcelMedia;
	}

}