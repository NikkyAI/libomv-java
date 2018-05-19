package libomv.model.group;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the members of a group
public class GroupMembersReplyCallbackArgs implements CallbackArgs {
	private final UUID m_RequestID;
	private final UUID m_GroupID;
	private final HashMap<UUID, GroupMember> m_Members;

	// Get the ID as returned by the request to correlate this result set
	// and the request
	public final UUID getRequestID() {
		return m_RequestID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// Get the dictionary of members
	public final HashMap<UUID, GroupMember> getMembers() {
		return m_Members;
	}

	/**
	 * Construct a new instance of the GroupMembersReplyCallbackArgs class
	 *
	 * @param requestID
	 *            The ID of the request
	 * @param groupID
	 *            The ID of the group
	 * @param members
	 *            The membership list of the group
	 */
	public GroupMembersReplyCallbackArgs(UUID requestID, UUID groupID, HashMap<UUID, GroupMember> members) {
		this.m_RequestID = requestID;
		this.m_GroupID = groupID;
		this.m_Members = members;
	}
}