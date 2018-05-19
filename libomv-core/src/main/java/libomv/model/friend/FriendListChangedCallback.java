package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendListChangedCallback implements Callback<FriendListChangedCallbackArgs> {
	@Override
	public abstract boolean callback(FriendListChangedCallbackArgs params);
}