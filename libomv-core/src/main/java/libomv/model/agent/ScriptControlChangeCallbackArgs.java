package libomv.model.agent;

import libomv.utils.CallbackArgs;

// Data sent by a script requesting to take or release specified controls to
// your agent
public class ScriptControlChangeCallbackArgs implements CallbackArgs {
	private int controls;
	private boolean pass;
	private boolean take;

	/**
	 * Construct a new instance of the ScriptControlEventArgs class
	 *
	 * @param controls
	 *            The controls the script is attempting to take or release to the
	 *            agent
	 * @param pass
	 *            True if the script is passing controls back to the agent
	 * @param take
	 *            True if the script is requesting controls be released to the
	 *            script
	 */
	public ScriptControlChangeCallbackArgs(int controls, boolean pass, boolean take) {
		this.controls = controls;
		this.pass = pass;
		this.take = take;
	}

	// Get the controls the script is attempting to take or release to the agent
	public int getControl() {
		return controls;
	}

	// True if the script is passing controls back to the agent
	public boolean getPass() {
		return pass;
	}

	// True if the script is requesting controls be released to the script
	public boolean getTake() {
		return take;
	}

}