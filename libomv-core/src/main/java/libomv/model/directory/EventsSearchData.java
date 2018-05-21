package libomv.model.directory;

import libomv.types.UUID;
import libomv.utils.Helpers;

/** An "Event" Listing summary */
public final class EventsSearchData {
	/** The ID of the event creator */
	public UUID owner;
	/** The name of the event */
	public String name;
	/** The events ID */
	public int id; /* TODO was uint */
	/** A string containing the short date/time the event will begin */
	public String date;
	/** The event start time in Unixtime (seconds since epoch) */
	public int time;
	/** The events maturity rating */
	public EventFlags flags;

	/**
	 * Print the struct data as a string
	 *
	 * @return A string containing the field name, and field value
	 */
	@Override
	public String toString() {
		return Helpers.structToString(this);
	}
}