package libomv.model.friend;

import libomv.utils.CallbackArgs;

public class FriendListChangedCallbackArgs implements CallbackArgs {
	private final FriendInfo info;
	private final boolean added;

	public FriendInfo getFriendInfo() {
		return info;
	}

	public boolean getIsAdded() {
		return added;
	}

	public FriendListChangedCallbackArgs(FriendInfo info, boolean added) {
		this.info = info;
		this.added = added;
	}
}