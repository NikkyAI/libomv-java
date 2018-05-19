package libomv.model.agent;

import libomv.utils.CallbackArgs;

public class TeleportCallbackArgs implements CallbackArgs {
	private final String message;
	private final TeleportStatus status;
	private final int flags;

	public String getMessage() {
		return message;
	}

	public TeleportStatus getStatus() {
		return status;
	}

	public int getFlags() {
		return flags;
	}

	public TeleportCallbackArgs(String message, TeleportStatus status, int flags) {
		this.message = message;
		this.status = status;
		this.flags = flags;
	}
}