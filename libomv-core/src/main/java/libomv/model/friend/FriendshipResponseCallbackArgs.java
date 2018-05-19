package libomv.model.friend;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/* Triggered when someone accepts or rejects a friendship request */
public class FriendshipResponseCallbackArgs implements CallbackArgs {
	private final UUID agentID;
	private final String name;
	private final boolean accepted;

	public FriendshipResponseCallbackArgs(UUID agentID, String name, boolean accepted) {
		this.agentID = agentID;
		this.name = name;
		this.accepted = accepted;
	}

	public UUID getAgentID() {
		return agentID;
	}

	public String getName() {
		return name;
	}

	public boolean getAccepted() {
		return accepted;
	}

}