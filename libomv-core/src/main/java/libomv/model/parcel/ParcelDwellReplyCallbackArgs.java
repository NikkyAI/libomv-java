package libomv.model.parcel;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Contains a parcels dwell data returned from the simulator in response to
// an <see cref="RequestParcelDwell"/>
public class ParcelDwellReplyCallbackArgs implements CallbackArgs {
	private final UUID m_ParcelID;
	private final int m_LocalID;
	private final float m_Dwell;

	// Get the global ID of the parcel
	public final UUID getParcelID() {
		return m_ParcelID;
	}

	// Get the simulator specific ID of the parcel
	public final int getLocalID() {
		return m_LocalID;
	}

	// Get the calculated dwell
	public final float getDwell() {
		return m_Dwell;
	}

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
		this.m_ParcelID = parcelID;
		this.m_LocalID = localID;
		this.m_Dwell = dwell;
	}
}