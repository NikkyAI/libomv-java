package libomv.model.avatar;

import libomv.utils.CallbackArgs;

public class AvatarInterestsReplyCallbackArgs implements CallbackArgs {
	private libomv.primitives.Avatar avatar;

	public libomv.primitives.Avatar getAvatar() {
		return avatar;
	}

	public AvatarInterestsReplyCallbackArgs(libomv.primitives.Avatar avatar) {
		this.avatar = avatar;
	}
}