package libomv.model.avatar;

import libomv.primitives.Avatar;
import libomv.utils.CallbackArgs;

public class AvatarInterestsReplyCallbackArgs implements CallbackArgs {
	private Avatar avatar;

	public AvatarInterestsReplyCallbackArgs(Avatar avatar) {
		this.avatar = avatar;
	}

	public Avatar getAvatar() {
		return avatar;
	}

}