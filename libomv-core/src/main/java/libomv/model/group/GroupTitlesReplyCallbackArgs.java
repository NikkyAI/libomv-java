package libomv.model.group;

import java.util.Map;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Represents the titles for a group
public class GroupTitlesReplyCallbackArgs implements CallbackArgs {
	private final UUID requestID;
	private final UUID groupID;
	private final Map<UUID, GroupTitle> titles;

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
	public GroupTitlesReplyCallbackArgs(UUID requestID, UUID groupID, Map<UUID, GroupTitle> titles) {
		this.requestID = requestID;
		this.groupID = groupID;
		this.titles = titles;
	}

	// Get the ID as returned by the request to correlate this result set
	// and the request
	public final UUID getRequestID() {
		return requestID;
	}

	// Get the ID of the group
	public final UUID getGroupID() {
		return groupID;
	}

	// Get the titles
	public final Map<UUID, GroupTitle> getTitles() {
		return titles;
	}

}