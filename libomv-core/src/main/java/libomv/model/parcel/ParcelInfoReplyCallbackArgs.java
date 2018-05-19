package libomv.model.parcel;

import libomv.utils.CallbackArgs;

// Contains basic parcel information data returned from the simulator in
// response to an <see cref="RequestParcelInfo"/> request
public class ParcelInfoReplyCallbackArgs implements CallbackArgs {
	private final ParcelInfo m_Parcel;

	// Get the <see cref="ParcelInfo"/> object containing basic parcel info
	public final ParcelInfo getParcel() {
		return m_Parcel;
	}

	/**
	 * Construct a new instance of the ParcelInfoReplyCallbackArgs class
	 *
	 * @param parcel
	 *            The <see cref="ParcelInfo"/> object containing basic parcel info
	 */
	public ParcelInfoReplyCallbackArgs(ParcelInfo parcel) {
		this.m_Parcel = parcel;
	}
}