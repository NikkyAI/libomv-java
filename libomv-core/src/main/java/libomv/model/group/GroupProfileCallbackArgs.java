package libomv.model.group;

import libomv.model.Group;
import libomv.utils.CallbackArgs;

// Represents the profile of a group
public class GroupProfileCallbackArgs implements CallbackArgs {
	private final Group group;

	/**
	 * Construct a new instance of the GroupProfileCallbackArgs class
	 *
	 * @param group
	 *            The group profile
	 */
	public GroupProfileCallbackArgs(Group group) {
		this.group = group;
	}

	// Get the group profile
	public final Group getGroup() {
		return group;
	}

}