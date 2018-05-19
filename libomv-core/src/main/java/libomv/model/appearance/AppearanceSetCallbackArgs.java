package libomv.model.appearance;

import libomv.utils.CallbackArgs;

// Raised when appearance data is sent to the simulator, also indicates the main
// appearance thread is finished.
// <see cref="RequestAgentSetAppearance"/> request.
public class AppearanceSetCallbackArgs implements CallbackArgs {
	private final boolean m_success;

	// Indicates whether appearance setting was successful
	public boolean getSuccess() {
		return m_success;
	}

	/**
	 * Triggered when appearance data is sent to the sim and the main appearance
	 * thread is done.
	 *
	 * @param success
	 *            Indicates whether appearance setting was successful
	 */
	public AppearanceSetCallbackArgs(boolean success) {
		this.m_success = success;
	}
}