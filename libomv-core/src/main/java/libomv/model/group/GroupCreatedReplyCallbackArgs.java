package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// A response to a group create request
public class GroupCreatedReplyCallbackArgs implements CallbackArgs {
	private final UUID m_GroupID;
	private final boolean m_Success;
	private final String m_Message;

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// true of the group was created successfully
	public final boolean getSuccess() {
		return m_Success;
	}

	// A string containing the message
	public final String getMessage() {
		return m_Message;
	}

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
		this.m_GroupID = groupID;
		this.m_Success = success;
		this.m_Message = messsage;
	}
}