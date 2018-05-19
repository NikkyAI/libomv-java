package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents a response to a request
public class GroupOperationCallbackArgs implements CallbackArgs {
	private final UUID groupID;
	private final boolean success;

	/**
	 * Construct a new instance of the GroupOperationCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 * @param success
	 *            true of the request was successful
	 */
	public GroupOperationCallbackArgs(UUID groupID, boolean success) {
		this.groupID = groupID;
		this.success = success;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

	// true of the request was successful
	public final boolean getSuccess() {
		return success;
	}

}