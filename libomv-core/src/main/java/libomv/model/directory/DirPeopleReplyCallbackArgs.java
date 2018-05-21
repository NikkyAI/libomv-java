package libomv.model.directory;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/** Contains the people data returned from the data server */
public class DirPeopleReplyCallbackArgs implements CallbackArgs {
	private final UUID queryID;
	private final List<AgentSearchData> matchedPeople;

	/**
	 * Construct a new instance of the DirPeopleReplyEventArgs class
	 *
	 * @param queryID
	 *            The ID of the query returned by the data server. This will
	 *            correlate to the ID returned by the
	 *            <see cref="StartPeopleSearch"/> method
	 * @param matchedPeople
	 *            A list of people data returned by the data server
	 */
	public DirPeopleReplyCallbackArgs(UUID queryID, List<AgentSearchData> matchedPeople) {
		this.queryID = queryID;
		this.matchedPeople = matchedPeople;
	}

	// The ID returned by <see cref="DirectoryManager.StartPeopleSearch"/>
	public final UUID getQueryID() {
		return queryID;
	}

	// A list containing People data returned by the data server
	public final List<AgentSearchData> getMatchedPeople() {
		return matchedPeople;
	}

}