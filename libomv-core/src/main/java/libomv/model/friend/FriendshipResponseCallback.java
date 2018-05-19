package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendshipResponseCallback implements Callback<FriendshipResponseCallbackArgs> {
	@Override
	public abstract boolean callback(FriendshipResponseCallbackArgs params);
}