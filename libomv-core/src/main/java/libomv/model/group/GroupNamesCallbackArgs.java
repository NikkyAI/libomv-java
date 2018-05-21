package libomv.model.group;

import java.util.Map;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// A Dictionary of group names, where the Key is the groups ID and the value
// is the groups name
public class GroupNamesCallbackArgs implements CallbackArgs {
	private final Map<UUID, String> groupNames;

	/**
	 * Construct a new instance of the GroupNamesCallbackArgs class
	 *
	 * @param groupNames
	 *            The Group names dictionary
	 */
	public GroupNamesCallbackArgs(Map<UUID, String> groupNames) {
		this.groupNames = groupNames;
	}

	// Get the Group Names dictionary
	public final Map<UUID, String> getGroupNames() {
		return groupNames;
	}

}