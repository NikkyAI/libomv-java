package libomv.model.group;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the roles associated with a group
public class GroupRolesDataReplyCallbackArgs implements CallbackArgs {
	private final UUID m_RequestID;
	private final UUID m_GroupID;
	private final HashMap<UUID, GroupRole> m_Roles;

	// Get the ID as returned by the request to correlate this result set
	// and the request
	public final UUID getRequestID() {
		return m_RequestID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// Get the dictionary containing the roles
	public final java.util.HashMap<UUID, GroupRole> getRoles() {
		return m_Roles;
	}

	/**
	 * Construct a new instance of the GroupRolesDataReplyCallbackArgs class
	 *
	 * @param requestID
	 *            The ID as returned by the request to correlate this result set and
	 *            the request
	 * @param groupID
	 *            The ID of the group
	 * @param roles
	 *            The dictionary containing the roles
	 */
	public GroupRolesDataReplyCallbackArgs(UUID requestID, UUID groupID, java.util.HashMap<UUID, GroupRole> roles) {
		this.m_RequestID = requestID;
		this.m_GroupID = groupID;
		this.m_Roles = roles;
	}
}