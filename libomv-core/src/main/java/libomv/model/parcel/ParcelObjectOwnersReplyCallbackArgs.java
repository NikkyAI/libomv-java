package libomv.model.parcel;

import java.util.List;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains blacklist and whitelist data returned from the simulator in
// response to an <see cref="RequestParcelAccesslist"/> request
public class ParcelObjectOwnersReplyCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final List<ParcelPrimOwners> owners;

	/**
	 * Construct a new instance of the ParcelObjectOwnersReplyCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel is located in
	 * @param primOwners
	 *            The list containing prim ownership counts
	 */
	public ParcelObjectOwnersReplyCallbackArgs(Simulator simulator, List<ParcelPrimOwners> primOwners) {
		this.simulator = simulator;
		this.owners = primOwners;
	}

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the list containing prim ownership counts
	public final List<ParcelPrimOwners> getPrimOwners() {
		return owners;
	}

}