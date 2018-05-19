package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendFoundReplyCallback implements Callback<FriendFoundReplyCallbackArgs> {
	@Override
	public abstract boolean callback(FriendFoundReplyCallbackArgs params);
}