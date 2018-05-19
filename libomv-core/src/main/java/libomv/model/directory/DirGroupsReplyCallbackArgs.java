package libomv.model.directory;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/** Contains the group data returned from the data server */
public class DirGroupsReplyCallbackArgs implements CallbackArgs {
	private final UUID queryID;
	private final ArrayList<GroupSearchData> matchedGroups;

	/**
	 * Construct a new instance of the DirGroupsReplyEventArgs class
	 *
	 * @param queryID
	 *            The ID of the query returned by the data server. This will
	 *            correlate to the ID returned by the <see cref="StartGroupSearch"/>
	 *            method
	 * @param matchedGroups
	 *            A list of groups data returned by the data server
	 */
	public DirGroupsReplyCallbackArgs(UUID queryID, ArrayList<GroupSearchData> matchedGroups) {
		this.queryID = queryID;
		this.matchedGroups = matchedGroups;
	}

	// The ID returned by <see cref="DirectoryManager.StartGroupSearch"/>
	public final UUID getQueryID() {
		return queryID;
	}

	// A list containing Groups data returned by the data server
	public final ArrayList<GroupSearchData> getMatchedGroups() {
		return matchedGroups;
	}

}