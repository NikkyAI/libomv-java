package libomv.model.parcel;

import java.util.ArrayList;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains blacklist and whitelist data returned from the simulator in
// response to an <see cref="RequestParcelAccesslist"/> request
public class ParcelObjectOwnersReplyCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final ArrayList<ParcelPrimOwners> owners;

	/**
	 * Construct a new instance of the ParcelObjectOwnersReplyCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel is located in
	 * @param primOwners
	 *            The list containing prim ownership counts
	 */
	public ParcelObjectOwnersReplyCallbackArgs(Simulator simulator, ArrayList<ParcelPrimOwners> primOwners) {
		this.simulator = simulator;
		this.owners = primOwners;
	}

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the list containing prim ownership counts
	public final ArrayList<ParcelPrimOwners> getPrimOwners() {
		return owners;
	}

}