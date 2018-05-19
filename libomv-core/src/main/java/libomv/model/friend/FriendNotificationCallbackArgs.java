package libomv.model.friend;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Triggered whenever a friend comes online or goes offline
public class FriendNotificationCallbackArgs implements CallbackArgs {
	private final UUID[] agentIDs;
	private final boolean online;

	public UUID[] getAgentID() {
		return agentIDs;
	}

	public boolean getOnline() {
		return online;
	}

	public FriendNotificationCallbackArgs(UUID[] agentIDs, boolean online) {
		this.agentIDs = agentIDs;
		this.online = online;
	}
}