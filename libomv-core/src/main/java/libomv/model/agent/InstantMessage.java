package libomv.model.agent;

import java.util.Date;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

/* Instant Message */
public final class InstantMessage {
	/* Key of sender */
	public UUID fromAgentID;
	/* Name of sender */
	public String fromAgentName;
	/* Key of destination avatar */
	public UUID toAgentID;
	/* ID of originating estate */

	public int parentEstateID;
	/* Key of originating region */
	public UUID regionID;
	/* Coordinates in originating region */
	public Vector3 position;
	/* Instant message type */
	public InstantMessageDialog dialog;
	/* Group IM session toggle */
	public boolean groupIM;
	/* Key of IM session, for Group Messages, the groups UUID */
	public UUID imSessionID;
	/* Timestamp of the instant message */
	public Date timestamp;
	/* Instant message text */
	public String message;
	/* Whether this message is held for offline avatars */
	public InstantMessageOnline offline;
	/* Context specific packed data */
	public byte[] binaryBucket;

	/*
	 * Print the struct data as a string
	 *
	 * @return A string containing the field name, and field value
	 */
	@Override
	public String toString() {
		return Helpers.StructToString(this);
	}
}