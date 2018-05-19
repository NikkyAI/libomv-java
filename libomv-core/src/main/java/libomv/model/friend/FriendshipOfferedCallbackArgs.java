package libomv.model.friend;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/* Triggered when someone offers us friendship */
public class FriendshipOfferedCallbackArgs implements CallbackArgs {
	private final UUID friendID;
	private final String name;
	private final UUID sessionID;

	public UUID getFriendID() {
		return friendID;
	}

	public String getName() {
		return name;
	}

	public UUID getSessionID() {
		return sessionID;
	}

	public FriendshipOfferedCallbackArgs(UUID friendID, String name, UUID sessionID) {
		this.friendID = friendID;
		this.name = name;
		this.sessionID = sessionID;
	}
}