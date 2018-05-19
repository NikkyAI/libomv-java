package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains the data returned when a <see cref="RequestForceSelectObjects"/>
// request
public class ForceSelectObjectsReplyCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final int[] objectIDs;
	private final boolean resetList;

	/**
	 * Construct a new instance of the ForceSelectObjectsReplyCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel data was retrieved from
	 * @param objectIDs
	 *            The list of primitive IDs
	 * @param resetList
	 *            true if the list is clean and contains the information only for a
	 *            given request
	 */
	public ForceSelectObjectsReplyCallbackArgs(Simulator simulator, int[] objectIDs, boolean resetList) {
		this.simulator = simulator;
		this.objectIDs = objectIDs;
		this.resetList = resetList;
	}

	// Get the simulator the parcel data was retrieved from
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the list of primitive IDs
	public final int[] getObjectIDs() {
		return objectIDs;
	}

	// true if the list is clean and contains the information only for a
	// given request
	public final boolean getResetList() {
		return resetList;
	}

}