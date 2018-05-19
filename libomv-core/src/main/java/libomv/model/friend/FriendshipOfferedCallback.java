package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendshipOfferedCallback implements Callback<FriendshipOfferedCallbackArgs> {
	@Override
	public abstract boolean callback(FriendshipOfferedCallbackArgs params);
}