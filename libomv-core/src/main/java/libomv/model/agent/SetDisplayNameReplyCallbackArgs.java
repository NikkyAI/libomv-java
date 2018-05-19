package libomv.model.agent;

import libomv.utils.CallbackArgs;

// Event arguments with the result of setting display name
// operation
public class SetDisplayNameReplyCallbackArgs implements CallbackArgs {
	private final int m_Status;
	private final String m_Reason;
	private final AgentDisplayName m_DisplayName;

	// Status code, 200 indicates setting display name was successful
	public int getStatus() {
		return m_Status;
	}

	// Textual description of the status
	public String getReason() {
		return m_Reason;
	}

	// Details of the newly set display name
	public AgentDisplayName getDisplayName() {
		return m_DisplayName;
	}

	public SetDisplayNameReplyCallbackArgs(int status, String reason, AgentDisplayName displayName) {
		m_Status = status;
		m_Reason = reason;
		m_DisplayName = displayName;
	}
}