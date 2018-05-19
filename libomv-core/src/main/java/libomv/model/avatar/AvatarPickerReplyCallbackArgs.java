package libomv.model.avatar;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarPickerReplyCallbackArgs implements CallbackArgs {
	private UUID queryID;
	private HashMap<UUID, String> avatars;

	public AvatarPickerReplyCallbackArgs(UUID queryID, HashMap<UUID, String> avatars) {
		this.queryID = queryID;
		this.avatars = avatars;
	}

	public UUID getQueryID() {
		return queryID;
	}

	public HashMap<UUID, String> getAvatars() {
		return avatars;
	}

}