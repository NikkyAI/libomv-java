package libomv.model.avatar;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarGroupsReplyCallbackArgs implements CallbackArgs {
	private UUID avatarID;
	private List<AvatarGroup> avatarGroups;

	public AvatarGroupsReplyCallbackArgs(UUID avatarID, List<AvatarGroup> avatarGroups) {
		this.avatarID = avatarID;
		this.avatarGroups = avatarGroups;
	}

	public UUID getAvatarID() {
		return avatarID;
	}

	public List<AvatarGroup> getAvatarGroups() {
		return avatarGroups;
	}

}