package libomv.model.directory;

import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.Helpers;

/** The details of an "Event" */
public final class EventInfo {
	/** The events ID */
	public int id; /* TODO was uint */
	/** The ID of the event creator */
	public UUID creator;
	/** The name of the event */
	public String name;
	/** The category */
	public EventCategories category;
	/** The events description */
	public String desc;
	/** The short date/time the event will begin */
	public String date;
	/** The event start time in Unixtime (seconds since epoch) UTC adjusted */
	public int dateUTC; /* TODO was uint */
	/** The length of the event in minutes */
	public int duration; /* TODO was uint */
	/** 0 if no cover charge applies */
	public int cover; /* TODO was uint */
	/** The cover charge amount in L$ if applicable */
	public int amount; /* TODO was uint */
	/** The name of the region where the event is being held */
	public String simName;
	/** The gridwide location of the event */
	public Vector3d globalPos;
	/** The maturity rating */
	public EventFlags flags;

	/**
	 * Get a SL URL for the parcel where the event is hosted
	 *
	 * @return A string, containing a standard SLURL
	 */
	public String toSLurl() {
		float[] values = new float[2];
		Helpers.globalPosToRegionHandle((float) this.globalPos.x, (float) this.globalPos.y, values);
		return "secondlife://" + this.simName + "/" + values[0] + "/" + values[1] + "/" + this.globalPos.z;
	}

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