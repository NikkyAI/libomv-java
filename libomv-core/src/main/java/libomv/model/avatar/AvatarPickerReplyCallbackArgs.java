package libomv.model.avatar;

import java.util.Map;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarPickerReplyCallbackArgs implements CallbackArgs {
	private UUID queryID;
	private Map<UUID, String> avatars;

	public AvatarPickerReplyCallbackArgs(UUID queryID, Map<UUID, String> avatars) {
		this.queryID = queryID;
		this.avatars = avatars;
	}

	public UUID getQueryID() {
		return queryID;
	}

	public Map<UUID, String> getAvatars() {
		return avatars;
	}

}