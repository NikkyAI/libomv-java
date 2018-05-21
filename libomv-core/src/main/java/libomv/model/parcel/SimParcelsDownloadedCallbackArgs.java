package libomv.model.parcel;

import java.util.Map;

import libomv.model.Parcel;
import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains the data returned when all parcel data has been retrieved from a
// simulator
public class SimParcelsDownloadedCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final Map<Integer, Parcel> parcels;
	private final int[] parcelMap;

	/**
	 * Construct a new instance of the SimParcelsDownloadedCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel data was retrieved from
	 * @param simParcels
	 *            The dictionary containing the parcel data
	 * @param is
	 *            The multidimensional array containing a x,y grid mapped to each
	 *            64x64 parcel's LocalID.
	 */
	public SimParcelsDownloadedCallbackArgs(Simulator simulator, Map<Integer, Parcel> simParcels, int[] is) {
		this.simulator = simulator;
		this.parcels = simParcels;
		this.parcelMap = is;
	}

	// Get the simulator the parcel data was retrieved from
	public final Simulator getSimulator() {
		return simulator;
	}

	// A dictionary containing the parcel data where the key correlates to
	// the ParcelMap entry
	public final Map<Integer, Parcel> getParcels() {
		return parcels;
	}

	// Get the multidimensional array containing a x,y grid mapped to each
	// 64x64 parcel's LocalID.
	public final int[] getParcelMap() {
		return parcelMap;
	}

}