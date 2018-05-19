package libomv.model.estate;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()</summary>
 */
public class EstateCovenantReplyCallbackArgs implements CallbackArgs {
	private final UUID covenantID;
	private final long timestamp;
	private final String estateName;
	private final UUID estateOwnerID;

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
		this.covenantID = covenantID;
		this.timestamp = timestamp;
		this.estateName = estateName;
		this.estateOwnerID = estateOwnerID;
	}

	// The Covenant
	public UUID getCovenantID() {
		return covenantID;
	}

	// The timestamp
	public long getTimestamp() {
		return timestamp;
	}

	// The Estate name
	public String getEstateName() {
		return estateName;
	}

	// The Estate Owner's ID (can be a GroupID)
	public UUID getEstateOwnerID() {
		return estateOwnerID;
	}

}