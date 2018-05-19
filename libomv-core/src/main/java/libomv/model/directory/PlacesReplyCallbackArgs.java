package libomv.model.directory;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/** Contains the "Event" list data returned from the data server */
public class PlacesReplyCallbackArgs implements CallbackArgs {
	private final UUID queryID;
	private final ArrayList<PlacesSearchData> matchedPlaces;

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
		this.queryID = queryID;
		this.matchedPlaces = matchedPlaces;
	}

	// The ID returned by <see cref="DirectoryManager.StartPlacesSearch"/>
	public final UUID getQueryID() {
		return queryID;
	}

	// A list of "Places" returned by the data server
	public final ArrayList<PlacesSearchData> getMatchedPlaces() {
		return matchedPlaces;
	}

}