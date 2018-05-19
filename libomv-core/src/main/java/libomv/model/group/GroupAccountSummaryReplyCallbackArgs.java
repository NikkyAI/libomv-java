package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the summary data for a group
public class GroupAccountSummaryReplyCallbackArgs implements CallbackArgs {
	private final UUID m_GroupID;
	private final GroupAccountSummary m_Summary;

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// Get the summary data
	public final GroupAccountSummary getSummary() {
		return m_Summary;
	}

	/**
	 * Construct a new instance of the GroupAccountSummaryReplyCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 * @param summary
	 *            The summary data
	 */
	public GroupAccountSummaryReplyCallbackArgs(UUID groupID, GroupAccountSummary summary) {
		this.m_GroupID = groupID;
		this.m_Summary = summary;
	}
}