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
	private final UUID sessionID;
	private final UUID groupID;
	private final UUID roleID;
	private final String fromName;
	private final Simulator simulator;
	private final int fee;
	private String message;

	public GroupInvitationCallbackArgs(Simulator simulator, UUID sessionID, UUID groupID, UUID roleID, String fromName,
			String message, int fee) {
		this.simulator = simulator;
		this.sessionID = sessionID;
		this.groupID = groupID;
		this.roleID = roleID;
		this.fromName = fromName;
		this.message = message;
		this.fee = fee;
	}

	// The ID of the Avatar sending the group invitation
	public final UUID getSessionID() {
		return sessionID;
	}

	// The ID of the Avatar sending the group invitation
	public final UUID getGroupID() {
		return groupID;
	}

	// The ID of the group role sending the group invitation
	public final UUID getRoleID() {
		return roleID;
	}

	// The name of the Avatar sending the group invitation
	public final String getFromName() {
		return fromName;
	}

	// The fee joining this group costs
	public final int getFee() {
		return fee;
	}

	// A message containing the request information which includes the name
	// of the group, the groups charter and the fee to join details
	public final String getMessage() {
		return message;
	}

	// The Simulator
	public final Simulator getSimulator() {
		return simulator;
	}

}