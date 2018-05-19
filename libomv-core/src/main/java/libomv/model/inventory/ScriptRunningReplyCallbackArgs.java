package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class ScriptRunningReplyCallbackArgs implements CallbackArgs {
	private final UUID objectID;
	private final UUID scriptID;
	private final boolean isMono;
	private final boolean isRunning;

	public ScriptRunningReplyCallbackArgs(UUID objectID, UUID sctriptID, boolean isMono, boolean isRunning) {
		this.objectID = objectID;
		this.scriptID = sctriptID;
		this.isMono = isMono;
		this.isRunning = isRunning;
	}

	public final UUID getObjectID() {
		return objectID;
	}

	public final UUID getScriptID() {
		return scriptID;
	}

	public final boolean getIsMono() {
		return isMono;
	}

	public final boolean getIsRunning() {
		return isRunning;
	}

}