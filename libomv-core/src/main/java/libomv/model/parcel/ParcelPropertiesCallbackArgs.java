package libomv.model.parcel;

import libomv.model.Parcel;
import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains basic parcel information data returned from the simulator in
// response to an <see cref="RequestParcelInfo"/> request
public class ParcelPropertiesCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private Parcel parcel;
	private final ParcelResult result;
	private final int selectedPrims;
	private final int sequenceID;
	private final boolean snapSelection;

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
		this.simulator = simulator;
		this.parcel = parcel;
		this.result = result;
		this.selectedPrims = selectedPrims;
		this.sequenceID = sequenceID;
		this.snapSelection = snapSelection;
	}

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the <see cref="Parcel"/> object containing the details. If Result
	// is NoData, this object will not contain valid data
	public final Parcel getParcel() {
		return parcel;
	}

	// Get the result of the request
	public final ParcelResult getResult() {
		return result;
	}

	// Get the number of primitieves your agent is currently selecting and
	// or sitting on in this parcel
	public final int getSelectedPrims() {
		return selectedPrims;
	}

	// Get the user assigned ID used to correlate a request with these
	// results
	public final int getSequenceID() {
		return sequenceID;
	}

	// TODO:
	public final boolean getSnapSelection() {
		return snapSelection;
	}

}