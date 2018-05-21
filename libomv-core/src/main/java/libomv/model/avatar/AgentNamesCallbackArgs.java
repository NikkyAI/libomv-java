package libomv.model.avatar;

import java.util.Map;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AgentNamesCallbackArgs implements CallbackArgs {
	private Map<UUID, String> names;

	public AgentNamesCallbackArgs(Map<UUID, String> names) {
		this.names = names;
	}

	public Map<UUID, String> getNames() {
		return names;
	}

}