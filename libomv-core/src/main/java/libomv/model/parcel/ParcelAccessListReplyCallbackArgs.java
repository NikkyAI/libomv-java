package libomv.model.parcel;

import java.util.ArrayList;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains blacklist and whitelist data returned from the simulator in
// response to an <see cref="RequestParcelAccesslist"/> request
public class ParcelAccessListReplyCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final int m_SequenceID;
	private final int m_LocalID;
	private final int m_Flags;
	private final ArrayList<ParcelAccessEntry> m_AccessList;

	// Get the simulator the parcel is located in
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the user assigned ID used to correlate a request with these
	// results
	public final int getSequenceID() {
		return m_SequenceID;
	}

	// Get the simulator specific ID of the parcel
	public final int getLocalID() {
		return m_LocalID;
	}

	// TODO:
	public final int getFlags() {
		return m_Flags;
	}

	// Get the list containing the white/blacklisted agents for the parcel
	public final ArrayList<ParcelAccessEntry> getAccessList() {
		return m_AccessList;
	}

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
			ArrayList<ParcelAccessEntry> accessEntries) {
		this.m_Simulator = simulator;
		this.m_SequenceID = sequenceID;
		this.m_LocalID = localID;
		this.m_Flags = flags;
		this.m_AccessList = accessEntries;
	}
}