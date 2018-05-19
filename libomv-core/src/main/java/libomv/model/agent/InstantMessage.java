package libomv.model.agent;

import java.util.Date;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

/* Instant Message */
public final class InstantMessage {
	/* Key of sender */
	public UUID FromAgentID;
	/* Name of sender */
	public String FromAgentName;
	/* Key of destination avatar */
	public UUID ToAgentID;
	/* ID of originating estate */

	public int ParentEstateID;
	/* Key of originating region */
	public UUID RegionID;
	/* Coordinates in originating region */
	public Vector3 Position;
	/* Instant message type */
	public InstantMessageDialog Dialog;
	/* Group IM session toggle */
	public boolean GroupIM;
	/* Key of IM session, for Group Messages, the groups UUID */
	public UUID IMSessionID;
	/* Timestamp of the instant message */
	public Date Timestamp;
	/* Instant message text */
	public String Message;
	/* Whether this message is held for offline avatars */
	public InstantMessageOnline Offline;
	/* Context specific packed data */
	public byte[] BinaryBucket;

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