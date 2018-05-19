package libomv.model.friend;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/* Triggered when someone terminated friendship with us */
public class FriendshipTerminatedCallbackArgs implements CallbackArgs {
	private final UUID otherID;
	private final String name;

	public UUID getOtherID() {
		return otherID;
	}

	public String getName() {
		return name;
	}

	public FriendshipTerminatedCallbackArgs(UUID otherID, String name) {
		this.otherID = otherID;
		this.name = name;
	}
}