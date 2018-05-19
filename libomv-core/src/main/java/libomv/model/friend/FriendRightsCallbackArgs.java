package libomv.model.friend;

import libomv.utils.CallbackArgs;

// Triggered when a friends rights changed
public class FriendRightsCallbackArgs implements CallbackArgs {
	private final FriendInfo friendInfo;

	public FriendRightsCallbackArgs(FriendInfo friendInfo) {
		this.friendInfo = friendInfo;
	}

	public FriendInfo getFriendInfo() {
		return friendInfo;
	}

}