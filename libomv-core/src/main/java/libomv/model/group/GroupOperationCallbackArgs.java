package libomv.model.group;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents a response to a request
public class GroupOperationCallbackArgs implements CallbackArgs {
	private final UUID m_GroupID;
	private final boolean m_Success;

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// true of the request was successful
	public final boolean getSuccess() {
		return m_Success;
	}

	/**
	 * Construct a new instance of the GroupOperationCallbackArgs class
	 *
	 * @param groupID
	 *            The ID of the group
	 * @param success
	 *            true of the request was successful
	 */
	public GroupOperationCallbackArgs(UUID groupID, boolean success) {
		this.m_GroupID = groupID;
		this.m_Success = success;
	}
}