package libomv.model.login;

import libomv.utils.CallbackArgs;

// An event for being logged out either through client request, server
// forced, or network error
public class LoginProgressCallbackArgs implements CallbackArgs {
	private final LoginStatus m_Status;
	private final String m_Message;
	private final String m_Reason;
	private LoginResponseData m_Reply;

	public final LoginStatus getStatus() {
		return m_Status;
	}

	public final String getMessage() {
		return m_Message;
	}

	public final String getReason() {
		return m_Reason;
	}

	public LoginResponseData getReply() {
		return m_Reply;
	}

	public LoginProgressCallbackArgs(LoginStatus login, String message, String reason, LoginResponseData reply) {
		this.m_Reply = reply;
		this.m_Status = login;
		this.m_Message = message;
		this.m_Reason = reason;
	}
}