package libomv.model.directory;

import libomv.types.UUID;
import libomv.utils.Helpers;

/** Response to a "Groups" Search */
public final class GroupSearchData {
	/** The Group ID */
	public UUID GroupID;
	/** The name of the group */
	public String GroupName;
	/** The current number of members */
	public int Members;

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