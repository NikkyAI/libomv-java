package libomv.model.directory;

import libomv.types.UUID;
import libomv.utils.Helpers;

/** An "Event" Listing summary */
public final class EventsSearchData {
	/** The ID of the event creator */
	public UUID Owner;
	/** The name of the event */
	public String Name;
	/** The events ID */
	public int ID; /* TODO was uint */
	/** A string containing the short date/time the event will begin */
	public String Date;
	/** The event start time in Unixtime (seconds since epoch) */
	public int Time;
	/** The events maturity rating */
	public EventFlags Flags;

	/**
	 * Print the struct data as a string
	 *
	 * @return A string containing the field name, and field value
	 */
	@Override
	public String toString() {
		return Helpers.StructToString(this);
	}
}