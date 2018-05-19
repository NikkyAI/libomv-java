package libomv.model.friend;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

// Triggered when a map request for a friend is answered
public class FriendFoundReplyCallbackArgs implements CallbackArgs {
	private final UUID agentID;
	private final long regionHandle;
	private final Vector3 location;

	public FriendFoundReplyCallbackArgs(UUID agentID, long regionHandle, Vector3 location) {
		this.agentID = agentID;
		this.regionHandle = regionHandle;
		this.location = location;
	}

	public UUID getAgentID() {
		return agentID;
	}

	public long getRegionHandle() {
		return regionHandle;
	}

	public Vector3 getLocation() {
		return location;
	}

}