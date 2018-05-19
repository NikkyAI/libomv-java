package libomv.model.avatar;

import libomv.primitives.Avatar;
import libomv.utils.CallbackArgs;

public class AvatarPropertiesReplyCallbackArgs implements CallbackArgs {
	private Avatar avatar;

	public AvatarPropertiesReplyCallbackArgs(Avatar avatar) {
		this.avatar = avatar;
	}

	public Avatar getAvatar() {
		return avatar;
	}

}