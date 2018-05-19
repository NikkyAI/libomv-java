package libomv.model.group;

import java.util.HashMap;

import libomv.model.Group;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Contains the current groups your agent is a member of
public class CurrentGroupsCallbackArgs implements CallbackArgs {
	private final HashMap<UUID, Group> groups;

	/**
	 * Construct a new instance of the CurrentGroupsCallbackArgs class
	 *
	 * @param groups
	 *            The current groups your agent is a member of
	 */
	public CurrentGroupsCallbackArgs(HashMap<UUID, Group> groups) {
		this.groups = groups;
	}

	// Get the current groups your agent is a member of
	public final HashMap<UUID, Group> getGroups() {
		return groups;
	}

}