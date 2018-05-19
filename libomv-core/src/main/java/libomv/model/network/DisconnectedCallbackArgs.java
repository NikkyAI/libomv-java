package libomv.model.network;

import libomv.utils.CallbackArgs;

// An event for being logged out either through client request, server
// forced, or network error
public class DisconnectedCallbackArgs implements CallbackArgs {
	private final DisconnectType type;
	private final String message;

	public DisconnectedCallbackArgs(DisconnectType type, String message) {
		this.type = type;
		this.message = message;
	}

	public DisconnectType getDisconnectType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

}