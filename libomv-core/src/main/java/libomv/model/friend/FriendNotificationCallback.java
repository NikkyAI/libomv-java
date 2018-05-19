package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendNotificationCallback implements Callback<FriendNotificationCallbackArgs> {
	@Override
	public abstract boolean callback(FriendNotificationCallbackArgs params);
}