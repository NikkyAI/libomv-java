package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AgentDataReplyCallbackArgs implements CallbackArgs {
	private final String m_FirstName;
	private final String m_LastName;
	private final UUID m_ActiveGroup;
	private final String m_GroupName;
	private final String m_GroupTitle;
	private final long m_ActiveGroupPowers;

	public String getFristName() {
		return m_FirstName;
	}

	public String getLastName() {
		return m_LastName;
	}

	public UUID getActiveGroup() {
		return m_ActiveGroup;
	}

	public String getGroupName() {
		return m_GroupName;
	}

	public String getGroupTitle() {
		return m_GroupTitle;
	}

	public long getActiveGroupPowers() {
		return m_ActiveGroupPowers;
	}

	public AgentDataReplyCallbackArgs(String firstName, String lastName, UUID activeGroup, String groupTitle,
			long activeGroupPowers, String groupName) {
		this.m_FirstName = firstName;
		this.m_LastName = lastName;
		this.m_ActiveGroup = activeGroup;
		this.m_GroupName = groupName;
		this.m_GroupTitle = groupTitle;
		this.m_ActiveGroupPowers = activeGroupPowers;
	}
}