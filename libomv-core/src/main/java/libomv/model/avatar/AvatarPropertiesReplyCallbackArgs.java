package libomv.model.avatar;

import libomv.utils.CallbackArgs;

public class AvatarPropertiesReplyCallbackArgs implements CallbackArgs {
	private libomv.primitives.Avatar avatar;

	public libomv.primitives.Avatar getAvatar() {
		return avatar;
	}

	public AvatarPropertiesReplyCallbackArgs(libomv.primitives.Avatar avatar) {
		this.avatar = avatar;
	}
}