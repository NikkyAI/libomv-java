package libomv.model.parcel;

import java.util.List;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains blacklist and whitelist data returned from the simulator in
// response to an <see cref="RequestParcelAccesslist"/> request
public class ParcelAccessListReplyCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final int sequenceID;
	private final int localID;
	private final int flags;
	private final List<ParcelAccessEntry> accessList;

	/**
	 * Construct a new instance of the ParcelAccessListReplyCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel is located in
	 * @param sequenceID
	 *            The user assigned ID used to correlate a request with these
	 *            results
	 * @param localID
	 *            The simulator specific ID of the parcel
	 * @param flags
	 *            TODO:
	 * @param accessEntries
	 *            The list containing the white/blacklisted agents for the parcel
	 */
	public ParcelAccessListReplyCallbackArgs(Simulator simulator, int sequenceID, int localID, int flags,
			List<ParcelAccessEntry> accessEntries) {
		this.simulator = simulator;
		this.sequenceID = sequenceID;
		this.localID = localID;
		this.flags = flags;
		this.accessList = accessEntries;
	}

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the user assigned ID used to correlate a request with these
	// results
	public final int getSequenceID() {
		return sequenceID;
	}

	// Get the simulator specific ID of the parcel
	public final int getLocalID() {
		return localID;
	}

	// TODO:
	public final int getFlags() {
		return flags;
	}

	// Get the list containing the white/blacklisted agents for the parcel
	public final List<ParcelAccessEntry> getAccessList() {
		return accessList;
	}

}