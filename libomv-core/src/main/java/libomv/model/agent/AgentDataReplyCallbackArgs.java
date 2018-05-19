package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AgentDataReplyCallbackArgs implements CallbackArgs {
	private final String firstName;
	private final String lastName;
	private final UUID activeGroup;
	private final String groupName;
	private final String groupTitle;
	private final long activeGroupPowers;

	public AgentDataReplyCallbackArgs(String firstName, String lastName, UUID activeGroup, String groupTitle,
			long activeGroupPowers, String groupName) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.activeGroup = activeGroup;
		this.groupName = groupName;
		this.groupTitle = groupTitle;
		this.activeGroupPowers = activeGroupPowers;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public UUID getActiveGroup() {
		return activeGroup;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public long getActiveGroupPowers() {
		return activeGroupPowers;
	}

}