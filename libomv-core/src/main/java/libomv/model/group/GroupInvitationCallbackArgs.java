package libomv.model.group;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Provides notification of a group invitation request sent by another Avatar
 *
 * The <see cref="GroupInvitation"/> invitation is raised when another avatar
 * makes an offer for our avatar to join a group.
 */
public class GroupInvitationCallbackArgs implements CallbackArgs {
	private final UUID m_SessionID;
	private final UUID m_GroupID;
	private final UUID m_RoleID;
	private final String m_FromName;
	private final Simulator m_Simulator;
	private final int m_Fee;
	private String m_Message;

	// The ID of the Avatar sending the group invitation
	public final UUID getSessionID() {
		return m_SessionID;
	}

	// The ID of the Avatar sending the group invitation
	public final UUID getGroupID() {
		return m_GroupID;
	}

	// The ID of the group role sending the group invitation
	public final UUID getRoleID() {
		return m_RoleID;
	}

	// The name of the Avatar sending the group invitation
	public final String getFromName() {
		return m_FromName;
	}

	// The fee joining this group costs
	public final int getFee() {
		return m_Fee;
	}

	// A message containing the request information which includes the name
	// of the group, the groups charter and the fee to join details
	public final String getMessage() {
		return m_Message;
	}

	// The Simulator
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public GroupInvitationCallbackArgs(Simulator simulator, UUID sessionID, UUID groupID, UUID roleID,
			String fromName, String message, int fee) {
		this.m_Simulator = simulator;
		this.m_SessionID = sessionID;
		this.m_GroupID = groupID;
		this.m_RoleID = roleID;
		this.m_FromName = fromName;
		this.m_Message = message;
		this.m_Fee = fee;
	}
}