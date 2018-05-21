package libomv.model.group;

import java.util.Map;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the members of a group
public class GroupMembersReplyCallbackArgs implements CallbackArgs {
	private final UUID requestID;
	private final UUID groupID;
	private final Map<UUID, GroupMember> members;

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
	public GroupMembersReplyCallbackArgs(UUID requestID, UUID groupID, Map<UUID, GroupMember> members) {
		this.requestID = requestID;
		this.groupID = groupID;
		this.members = members;
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

	// Get the dictionary of members
	public final Map<UUID, GroupMember> getMembers() {
		return members;
	}

}