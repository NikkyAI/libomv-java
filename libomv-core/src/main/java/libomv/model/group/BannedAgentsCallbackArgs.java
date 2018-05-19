package libomv.model.group;

import java.util.Date;
import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Result of the request for list of agents banned from a group
public class BannedAgentsCallbackArgs implements CallbackArgs {
	private final UUID groupID;
	private final boolean success;
	private final HashMap<UUID, Date> bannedAgents;

	public BannedAgentsCallbackArgs(UUID groupID, boolean success, HashMap<UUID, Date> bannedAgents) {
		this.groupID = groupID;
		this.success = success;
		this.bannedAgents = bannedAgents;
	}

	// Indicates if list of banned agents for a group was successfully retrieved
	public UUID getGroupID() {
		return groupID;
	}

	// Indicates if list of banned agents for a group was successfully retrieved
	public boolean getSuccess() {
		return success;
	}

	// Array containing a list of UUIDs of the agents banned from a group
	public HashMap<UUID, Date> getBannedAgents() {
		return bannedAgents;
	}

}
