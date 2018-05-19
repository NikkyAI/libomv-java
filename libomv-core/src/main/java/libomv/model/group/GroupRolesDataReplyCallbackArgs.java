package libomv.model.group;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the roles associated with a group
public class GroupRolesDataReplyCallbackArgs implements CallbackArgs {
	private final UUID requestID;
	private final UUID groupID;
	private final HashMap<UUID, GroupRole> roles;

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
		this.requestID = requestID;
		this.groupID = groupID;
		this.roles = roles;
	}

	// Get the ID as returned by the request to correlate this result set
	// and the request
	public final UUID getRequestID() {
		return requestID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

	// Get the dictionary containing the roles
	public final java.util.HashMap<UUID, GroupRole> getRoles() {
		return roles;
	}

}