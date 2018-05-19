package libomv.model.login;

import libomv.utils.CallbackArgs;

// An event for being logged out either through client request, server
// forced, or network error
public class LoginProgressCallbackArgs implements CallbackArgs {
	private final LoginStatus status;
	private final String message;
	private final String reason;
	private LoginResponseData reply;

	public LoginProgressCallbackArgs(LoginStatus login, String message, String reason, LoginResponseData reply) {
		this.reply = reply;
		this.status = login;
		this.message = message;
		this.reason = reason;
	}

	public final LoginStatus getStatus() {
		return status;
	}

	public final String getMessage() {
		return message;
	}

	public final String getReason() {
		return reason;
	}

	public LoginResponseData getReply() {
		return reply;
	}

}