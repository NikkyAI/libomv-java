package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains data when the media data for a parcel the avatar is on changes
public class ParcelMediaUpdateReplyCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final ParcelMedia m_ParcelMedia;

	// Get the simulator the parcel media data was updated in
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the updated media information
	public final ParcelMedia getMedia() {
		return m_ParcelMedia;
	}

	/**
	 * Construct a new instance of the ParcelMediaUpdateReplyCallbackArgs class
	 *
	 * @param simulator
	 *            the simulator the parcel media data was updated in
	 * @param media
	 *            The updated media information
	 */
	public ParcelMediaUpdateReplyCallbackArgs(Simulator simulator, ParcelMedia media) {
		this.m_Simulator = simulator;
		this.m_ParcelMedia = media;
	}
}