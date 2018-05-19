package libomv.model.directory;

import libomv.utils.CallbackArgs;

/**
 * Contains the Event data returned from the data server from an
 * EventInfoRequest
 */
public class EventInfoReplyCallbackArgs implements CallbackArgs {
	private final EventInfo matchedEvent;

	/**
	 * Construct a new instance of the EventInfoReplyEventArgs class
	 *
	 * @param matchedEvent
	 *            A single EventInfo object containing the details of an event
	 */
	public EventInfoReplyCallbackArgs(EventInfo matchedEvent) {
		this.matchedEvent = matchedEvent;
	}

	/** A single EventInfo object containing the details of an event */
	public final EventInfo getMatchedEvent() {
		return matchedEvent;
	}

}