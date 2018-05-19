package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendRightsCallback implements Callback<FriendRightsCallbackArgs> {
	@Override
	public abstract boolean callback(FriendRightsCallbackArgs params);
}