package libomv.model.agent;

import libomv.utils.CallbackArgs;

// Event arguments with the result of setting display name
// operation
public class SetDisplayNameReplyCallbackArgs implements CallbackArgs {
	private final int status;
	private final String reason;
	private final AgentDisplayName displayName;

	public SetDisplayNameReplyCallbackArgs(int status, String reason, AgentDisplayName displayName) {
		this.status = status;
		this.reason = reason;
		this.displayName = displayName;
	}

	// Status code, 200 indicates setting display name was successful
	public int getStatus() {
		return status;
	}

	// Textual description of the status
	public String getReason() {
		return reason;
	}

	// Details of the newly set display name
	public AgentDisplayName getDisplayName() {
		return displayName;
	}

}