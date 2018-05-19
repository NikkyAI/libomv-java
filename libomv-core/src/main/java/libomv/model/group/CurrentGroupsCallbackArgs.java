package libomv.model.group;

import libomv.model.Group;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Contains the current groups your agent is a member of
public class CurrentGroupsCallbackArgs implements CallbackArgs {
	private final java.util.HashMap<UUID, Group> m_Groups;

	// Get the current groups your agent is a member of
	public final java.util.HashMap<UUID, Group> getGroups() {
		return m_Groups;
	}

	/**
	 * Construct a new instance of the CurrentGroupsCallbackArgs class
	 *
	 * @param groups
	 *            The current groups your agent is a member of
	 */
	public CurrentGroupsCallbackArgs(java.util.HashMap<UUID, Group> groups) {
		this.m_Groups = groups;
	}
}