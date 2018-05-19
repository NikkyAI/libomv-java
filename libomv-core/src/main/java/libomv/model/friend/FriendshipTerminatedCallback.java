package libomv.model.friend;

import libomv.utils.Callback;

public abstract class FriendshipTerminatedCallback implements Callback<FriendshipTerminatedCallbackArgs> {
	@Override
	public abstract boolean callback(FriendshipTerminatedCallbackArgs params);
}