package libomv.model.avatar;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AgentNamesCallbackArgs implements CallbackArgs {
	private HashMap<UUID, String> names;

	public HashMap<UUID, String> getNames() {
		return names;
	}

	public AgentNamesCallbackArgs(HashMap<UUID, String> names) {
		this.names = names;
	}
}