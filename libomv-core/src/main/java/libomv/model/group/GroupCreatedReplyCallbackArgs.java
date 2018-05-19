package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// A response to a group create request
public class GroupCreatedReplyCallbackArgs implements CallbackArgs {
	private final UUID groupID;
	private final boolean success;
	private final String message;

	/**
	 * Construct a new instance of the GroupCreatedReplyCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 * @param success
	 *            the success or failure of the request
	 * @param messsage
	 *            A string containing additional information
	 */
	public GroupCreatedReplyCallbackArgs(UUID groupID, boolean success, String messsage) {
		this.groupID = groupID;
		this.success = success;
		this.message = messsage;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

	// true of the group was created successfully
	public final boolean getSuccess() {
		return success;
	}

	// A string containing the message
	public final String getMessage() {
		return message;
	}

}