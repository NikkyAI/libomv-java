package libomv.model.parcel;

import libomv.model.Parcel;
import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains basic parcel information data returned from the simulator in
// response to an <see cref="RequestParcelInfo"/> request
public class ParcelPropertiesCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private Parcel m_Parcel;
	private final ParcelResult m_Result;
	private final int m_SelectedPrims;
	private final int m_SequenceID;
	private final boolean m_SnapSelection;

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the <see cref="Parcel"/> object containing the details. If Result
	// is NoData, this object will not contain valid data
	public final Parcel getParcel() {
		return m_Parcel;
	}

	// Get the result of the request
	public final ParcelResult getResult() {
		return m_Result;
	}

	// Get the number of primitieves your agent is currently selecting and
	// or sitting on in this parcel
	public final int getSelectedPrims() {
		return m_SelectedPrims;
	}

	// Get the user assigned ID used to correlate a request with these
	// results
	public final int getSequenceID() {
		return m_SequenceID;
	}

	// TODO:
	public final boolean getSnapSelection() {
		return m_SnapSelection;
	}

	/**
	 * Construct a new instance of the ParcelPropertiesCallbackArgs class
	 *
	 * @param simulator
	 *            The <see cref="Parcel"/> object containing the details
	 * @param parcel
	 *            The <see cref="Parcel"/> object containing the details
	 * @param result
	 *            The result of the request
	 * @param selectedPrims
	 *            The number of primitieves your agent is currently selecting and or
	 *            sitting on in this parcel
	 * @param sequenceID
	 *            The user assigned ID used to correlate a request with these
	 *            results
	 * @param snapSelection
	 *            TODO:
	 */
	public ParcelPropertiesCallbackArgs(Simulator simulator, Parcel parcel, ParcelResult result, int selectedPrims,
			int sequenceID, boolean snapSelection) {
		this.m_Simulator = simulator;
		this.m_Parcel = parcel;
		this.m_Result = result;
		this.m_SelectedPrims = selectedPrims;
		this.m_SequenceID = sequenceID;
		this.m_SnapSelection = snapSelection;
	}
}