package libomv.model.group;

import java.util.ArrayList;
import java.util.Map.Entry;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the Role to Member mappings for a group
public class GroupRolesMembersReplyCallbackArgs implements CallbackArgs {
	private final UUID m_RequestID;
	private final UUID m_GroupID;
	private final ArrayList<Entry<UUID, UUID>> m_RolesMembers;

	// Get the ID as returned by the request to correlate this result set
	// and the request
	public final UUID getRequestID() {
		return m_RequestID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// Get the member to roles map
	public final ArrayList<Entry<UUID, UUID>> getRolesMembers() {
		return m_RolesMembers;
	}

	/**
	 * Construct a new instance of the GroupRolesMembersReplyCallbackArgs class
	 *
	 * @param requestID
	 *            The ID as returned by the request to correlate this result set and
	 *            the request
	 * @param groupID
	 *            The ID of the group
	 * @param rolesMembers
	 *            The member to roles map
	 */
	public GroupRolesMembersReplyCallbackArgs(UUID requestID, UUID groupID,
			ArrayList<Entry<UUID, UUID>> rolesMembers) {
		this.m_RequestID = requestID;
		this.m_GroupID = groupID;
		this.m_RolesMembers = rolesMembers;
	}
}