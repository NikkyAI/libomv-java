package libomv.model.parcel;

import java.util.HashMap;

import libomv.model.Parcel;
import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains the data returned when all parcel data has been retrieved from a
// simulator
public class SimParcelsDownloadedCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final HashMap<Integer, Parcel> m_Parcels;
	private final int[] m_ParcelMap;

	// Get the simulator the parcel data was retrieved from
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// A dictionary containing the parcel data where the key correlates to
	// the ParcelMap entry
	public final HashMap<Integer, Parcel> getParcels() {
		return m_Parcels;
	}

	// Get the multidimensional array containing a x,y grid mapped to each
	// 64x64 parcel's LocalID.
	public final int[] getParcelMap() {
		return m_ParcelMap;
	}

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
	public SimParcelsDownloadedCallbackArgs(Simulator simulator, HashMap<Integer, Parcel> simParcels, int[] is) {
		this.m_Simulator = simulator;
		this.m_Parcels = simParcels;
		this.m_ParcelMap = is;
	}
}