package libomv.model.group;

import java.util.ArrayList;
import java.util.Map.Entry;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the Role to Member mappings for a group
public class GroupRolesMembersReplyCallbackArgs implements CallbackArgs {
	private final UUID requestID;
	private final UUID groupID;
	private final ArrayList<Entry<UUID, UUID>> rolesMembers;

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
	public GroupRolesMembersReplyCallbackArgs(UUID requestID, UUID groupID, ArrayList<Entry<UUID, UUID>> rolesMembers) {
		this.requestID = requestID;
		this.groupID = groupID;
		this.rolesMembers = rolesMembers;
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

	// Get the member to roles map
	public final ArrayList<Entry<UUID, UUID>> getRolesMembers() {
		return rolesMembers;
	}

}