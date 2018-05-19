package libomv.model.avatar;

import libomv.model.agent.AgentDisplayName;
import libomv.types.UUID;

public class DisplayNamesCallbackArgs {
	private boolean success;
	private AgentDisplayName[] names;
	private UUID[] badIDs;

	public DisplayNamesCallbackArgs(boolean success, AgentDisplayName[] names, UUID[] badIDs) {
		this.success = success;
		this.names = names;
		this.badIDs = badIDs;
	}

	public boolean getSuccess() {
		return success;
	}

	public AgentDisplayName[] getNames() {
		return names;
	}

	public UUID[] getBadIDs() {
		return badIDs;
	}

}