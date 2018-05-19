package libomv.model.group;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the titles for a group
public class GroupTitlesReplyCallbackArgs implements CallbackArgs {
	private final UUID m_RequestID;
	private final UUID m_GroupID;
	private final HashMap<UUID, GroupTitle> m_Titles;

	// Get the ID as returned by the request to correlate this result set
	// and the request
	public final UUID getRequestID() {
		return m_RequestID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// Get the titles
	public final HashMap<UUID, GroupTitle> getTitles() {
		return m_Titles;
	}

	/**
	 * Construct a new instance of the GroupTitlesReplyCallbackArgs class
	 *
	 * @param requestID
	 *            The ID as returned by the request to correlate this result set and
	 *            the request
	 * @param groupID
	 *            The ID of the group
	 * @param titles
	 *            The titles
	 */
	public GroupTitlesReplyCallbackArgs(UUID requestID, UUID groupID, HashMap<UUID, GroupTitle> titles) {
		this.m_RequestID = requestID;
		this.m_GroupID = groupID;
		this.m_Titles = titles;
	}
}