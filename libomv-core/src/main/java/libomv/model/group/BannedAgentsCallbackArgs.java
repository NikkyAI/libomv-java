package libomv.model.group;

import java.util.Date;
import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Result of the request for list of agents banned from a group
public class BannedAgentsCallbackArgs implements CallbackArgs {
	private final UUID mGroupID;
	private final boolean mSuccess;
	private final HashMap<UUID, Date> mBannedAgents;

	// Indicates if list of banned agents for a group was successfully retrieved
	public UUID getGroupID() {
		return mGroupID;
	}

	// Indicates if list of banned agents for a group was successfully retrieved
	public boolean getSuccess() {
		return mSuccess;
	}

	// Array containing a list of UUIDs of the agents banned from a group
	public HashMap<UUID, Date> getBannedAgents() {
		return mBannedAgents;
	}

	public BannedAgentsCallbackArgs(UUID groupID, boolean success, HashMap<UUID, Date> bannedAgents) {
		this.mGroupID = groupID;
		this.mSuccess = success;
		this.mBannedAgents = bannedAgents;
	}
}
// #endregion CallbackArgs