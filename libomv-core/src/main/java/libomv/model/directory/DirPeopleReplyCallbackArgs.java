package libomv.model.directory;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/** Contains the people data returned from the data server */
public class DirPeopleReplyCallbackArgs implements CallbackArgs {
	private final UUID m_QueryID;

	// The ID returned by <see cref="DirectoryManager.StartPeopleSearch"/>
	public final UUID getQueryID() {
		return m_QueryID;
	}

	private final ArrayList<AgentSearchData> m_MatchedPeople;

	// A list containing People data returned by the data server
	public final ArrayList<AgentSearchData> getMatchedPeople() {
		return m_MatchedPeople;
	}

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
	public DirPeopleReplyCallbackArgs(UUID queryID, ArrayList<AgentSearchData> matchedPeople) {
		this.m_QueryID = queryID;
		this.m_MatchedPeople = matchedPeople;
	}
}