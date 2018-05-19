package libomv.model.parcel;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Contains a parcels dwell data returned from the simulator in response to
// an <see cref="RequestParcelDwell"/>
public class ParcelDwellReplyCallbackArgs implements CallbackArgs {
	private final UUID parcelID;
	private final int localID;
	private final float dwell;

	/**
	 * Construct a new instance of the ParcelDwellReplyCallbackArgs class
	 *
	 * @param parcelID
	 *            The global ID of the parcel
	 * @param localID
	 *            The simulator specific ID of the parcel
	 * @param dwell
	 *            The calculated dwell for the parcel
	 */
	public ParcelDwellReplyCallbackArgs(UUID parcelID, int localID, float dwell) {
		this.parcelID = parcelID;
		this.localID = localID;
		this.dwell = dwell;
	}

	// Get the global ID of the parcel
	public final UUID getParcelID() {
		return parcelID;
	}

	// Get the simulator specific ID of the parcel
	public final int getLocalID() {
		return localID;
	}

	// Get the calculated dwell
	public final float getDwell() {
		return dwell;
	}

}