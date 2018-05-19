package libomv.model.group;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents a list of active group notices
public class GroupNoticesListReplyCallbackArgs implements CallbackArgs {
	private final UUID groupID;
	private final ArrayList<GroupNoticesListEntry> notices;

	/**
	 * Construct a new instance of the GroupNoticesListReplyCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 * @param notices
	 *            The list containing active notices
	 */
	public GroupNoticesListReplyCallbackArgs(UUID groupID, ArrayList<GroupNoticesListEntry> notices) {
		this.groupID = groupID;
		this.notices = notices;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

	// Get the notices list
	public final ArrayList<GroupNoticesListEntry> getNotices() {
		return notices;
	}

}