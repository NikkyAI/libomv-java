package libomv.model.avatar;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarGroupsReplyCallbackArgs implements CallbackArgs {
	private UUID avatarID;
	private ArrayList<AvatarGroup> avatarGroups;

	public UUID getAvatarID() {
		return avatarID;
	}

	public ArrayList<AvatarGroup> getAvatarGroups() {
		return avatarGroups;
	}

	public AvatarGroupsReplyCallbackArgs(UUID avatarID, ArrayList<AvatarGroup> avatarGroups) {
		this.avatarID = avatarID;
		this.avatarGroups = avatarGroups;
	}
}