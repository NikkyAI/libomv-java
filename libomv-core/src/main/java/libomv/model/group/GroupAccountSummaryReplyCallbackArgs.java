package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the summary data for a group
public class GroupAccountSummaryReplyCallbackArgs implements CallbackArgs {
	private final UUID groupID;
	private final GroupAccountSummary summary;

	/**
	 * Construct a new instance of the GroupAccountSummaryReplyCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 * @param summary
	 *            The summary data
	 */
	public GroupAccountSummaryReplyCallbackArgs(UUID groupID, GroupAccountSummary summary) {
		this.groupID = groupID;
		this.summary = summary;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

	// Get the summary data
	public final GroupAccountSummary getSummary() {
		return summary;
	}

}