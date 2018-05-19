package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains blacklist and whitelist data returned from the simulator in
// response to an <see cref="RequestParcelAccesslist"/> request
public class ParcelObjectOwnersReplyCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final java.util.ArrayList<ParcelPrimOwners> m_Owners;

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the list containing prim ownership counts
	public final java.util.ArrayList<ParcelPrimOwners> getPrimOwners() {
		return m_Owners;
	}

	/**
	 * Construct a new instance of the ParcelObjectOwnersReplyCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel is located in
	 * @param primOwners
	 *            The list containing prim ownership counts
	 */
	public ParcelObjectOwnersReplyCallbackArgs(Simulator simulator,
			java.util.ArrayList<ParcelPrimOwners> primOwners) {
		this.m_Simulator = simulator;
		this.m_Owners = primOwners;
	}
}