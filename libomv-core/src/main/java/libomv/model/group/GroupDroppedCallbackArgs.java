package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents your agent leaving a group
public class GroupDroppedCallbackArgs implements CallbackArgs {
	private final UUID m_GroupID;

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	/**
	 * Construct a new instance of the GroupDroppedCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 */
	public GroupDroppedCallbackArgs(UUID groupID) {
		m_GroupID = groupID;
	}
}