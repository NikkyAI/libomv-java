package libomv.model.estate;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()</summary>
 */
public class EstateCovenantReplyCallbackArgs implements CallbackArgs {
	private final UUID m_covenantID;
	private final long m_timestamp;
	private final String m_estateName;
	private final UUID m_estateOwnerID;

	// The Covenant
	public UUID getCovenantID() {
		return m_covenantID;
	}

	// The timestamp
	public long getTimestamp() {
		return m_timestamp;
	}

	// The Estate name
	public String getEstateName() {
		return m_estateName;
	}

	// The Estate Owner's ID (can be a GroupID)
	public UUID getEstateOwnerID() {
		return m_estateOwnerID;
	}

	/**
	 * Construct a new instance of the EstateCovenantReplyEventArgs class
	 *
	 * @param covenantID
	 *            The Covenant ID
	 * @param timestamp
	 *            The timestamp
	 * @param estateName
	 *            The estate's name
	 * @param estateOwnerID
	 *            The Estate Owner's ID (can be a GroupID)
	 */
	public EstateCovenantReplyCallbackArgs(UUID covenantID, long timestamp, String estateName, UUID estateOwnerID) {
		this.m_covenantID = covenantID;
		this.m_timestamp = timestamp;
		this.m_estateName = estateName;
		this.m_estateOwnerID = estateOwnerID;
	}
}