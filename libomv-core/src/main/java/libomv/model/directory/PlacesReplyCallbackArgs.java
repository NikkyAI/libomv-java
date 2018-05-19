package libomv.model.directory;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/** Contains the "Event" list data returned from the data server */
public class PlacesReplyCallbackArgs implements CallbackArgs {
	private final UUID m_QueryID;

	// The ID returned by <see cref="DirectoryManager.StartPlacesSearch"/>
	public final UUID getQueryID() {
		return m_QueryID;
	}

	private final ArrayList<PlacesSearchData> m_MatchedPlaces;

	// A list of "Places" returned by the data server
	public final ArrayList<PlacesSearchData> getMatchedPlaces() {
		return m_MatchedPlaces;
	}

	/**
	 * Construct a new instance of PlacesReplyEventArgs class
	 *
	 * @param queryID
	 *            The ID of the query returned by the data server. This will
	 *            correlate to the ID returned by the
	 *            <see cref="StartPlacesSearch"/> method
	 * @param matchedPlaces
	 *            A list containing the "Places" returned by the data server query
	 */
	public PlacesReplyCallbackArgs(UUID queryID, ArrayList<PlacesSearchData> matchedPlaces) {
		this.m_QueryID = queryID;
		this.m_MatchedPlaces = matchedPlaces;
	}
}