package libomv.model.network;

import java.net.InetSocketAddress;

import libomv.utils.CallbackArgs;

/** Callback arguments classes */
public class SimConnectingCallbackArgs implements CallbackArgs {
	private InetSocketAddress endPoint;
	private boolean cancel = false;

	public InetSocketAddress getEndPoint() {
		return endPoint;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public boolean getCancel() {
		return cancel;
	}

	public SimConnectingCallbackArgs(InetSocketAddress endPoint) {
		this.endPoint = endPoint;
	}
}