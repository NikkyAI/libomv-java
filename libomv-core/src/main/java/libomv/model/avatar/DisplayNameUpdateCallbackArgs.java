package libomv.model.avatar;

import libomv.model.agent.AgentDisplayName;
import libomv.utils.CallbackArgs;

/**
 * Event args class for display name notification messages
 */
public class DisplayNameUpdateCallbackArgs implements CallbackArgs {
	private String oldDisplayName;
	private AgentDisplayName displayName;

	public DisplayNameUpdateCallbackArgs(String oldDisplayName, AgentDisplayName displayName) {
		this.oldDisplayName = oldDisplayName;
		this.displayName = displayName;
	}

	public String getOldDisplayName() {
		return oldDisplayName;
	}

	public AgentDisplayName getDisplayName() {
		return displayName;
	}

}