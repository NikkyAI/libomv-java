package libomv.model.directory;

import libomv.types.UUID;
import libomv.utils.Helpers;

/** An Avatar returned from the dataserver */
public final class AgentSearchData {
	/**
	 * Online status of agent This field appears to be obsolete and always returns
	 * false
	 */
	public boolean Online;
	/** The agents first name */
	public String FirstName;
	/** The agents last name */
	public String LastName;
	/** The agents <see cref="UUID"/> */
	public UUID AgentID;

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