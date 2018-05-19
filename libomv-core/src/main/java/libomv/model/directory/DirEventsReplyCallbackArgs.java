package libomv.model.directory;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// /#region callback handlers
/** Contains the "Event" detail data returned from the data server */
public class DirEventsReplyCallbackArgs implements CallbackArgs {
	private final UUID queryID;
	private final ArrayList<EventsSearchData> matchedEvents;

	/**
	 * Construct a new instance of the DirEventsReplyEventArgs class
	 *
	 * @param queryID
	 *            The ID of the query returned by the data server. This will
	 *            correlate to the ID returned by the
	 *            <see cref="StartEventsSearch"/> method
	 * @param matchedEvents
	 *            A list containing the "Events" returned by the search query
	 */
	public DirEventsReplyCallbackArgs(UUID queryID, ArrayList<EventsSearchData> matchedEvents) {
		this.queryID = queryID;
		this.matchedEvents = matchedEvents;
	}

	/** The ID returned by <see cref="DirectoryManager.StartEventsSearch"/> */
	public final UUID getQueryID() {
		return queryID;
	}

	/** A list of "Events" returned by the data server */
	public final ArrayList<EventsSearchData> getMatchedEvents() {
		return matchedEvents;
	}

}