package libomv.model.directory;

import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.Helpers;

/** The details of an "Event" */
public final class EventInfo {
	/** The events ID */
	public int ID; /* TODO was uint */
	/** The ID of the event creator */
	public UUID Creator;
	/** The name of the event */
	public String Name;
	/** The category */
	public EventCategories Category;
	/** The events description */
	public String Desc;
	/** The short date/time the event will begin */
	public String Date;
	/** The event start time in Unixtime (seconds since epoch) UTC adjusted */
	public int DateUTC; /* TODO was uint */
	/** The length of the event in minutes */
	public int Duration; /* TODO was uint */
	/** 0 if no cover charge applies */
	public int Cover; /* TODO was uint */
	/** The cover charge amount in L$ if applicable */
	public int Amount; /* TODO was uint */
	/** The name of the region where the event is being held */
	public String SimName;
	/** The gridwide location of the event */
	public Vector3d GlobalPos;
	/** The maturity rating */
	public EventFlags Flags;

	/**
	 * Get a SL URL for the parcel where the event is hosted
	 *
	 * @return A string, containing a standard SLURL
	 */
	public String toSLurl() {
		float[] values = new float[2];
		Helpers.GlobalPosToRegionHandle((float) this.GlobalPos.X, (float) this.GlobalPos.Y, values);
		return "secondlife://" + this.SimName + "/" + values[0] + "/" + values[1] + "/" + this.GlobalPos.Z;
	}

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