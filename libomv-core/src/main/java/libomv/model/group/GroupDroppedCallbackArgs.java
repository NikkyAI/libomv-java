package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents your agent leaving a group
public class GroupDroppedCallbackArgs implements CallbackArgs {
	private final UUID groupID;

	/**
	 * Construct a new instance of the GroupDroppedCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 */
	public GroupDroppedCallbackArgs(UUID groupID) {
		this.groupID = groupID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

}