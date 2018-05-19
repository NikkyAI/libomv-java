package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains the data returned when a <see cref="RequestForceSelectObjects"/>
// request
public class ForceSelectObjectsReplyCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final int[] m_ObjectIDs;
	private final boolean m_ResetList;

	// Get the simulator the parcel data was retrieved from
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the list of primitive IDs
	public final int[] getObjectIDs() {
		return m_ObjectIDs;
	}

	// true if the list is clean and contains the information only for a
	// given request
	public final boolean getResetList() {
		return m_ResetList;
	}

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
		this.m_Simulator = simulator;
		this.m_ObjectIDs = objectIDs;
		this.m_ResetList = resetList;
	}
}