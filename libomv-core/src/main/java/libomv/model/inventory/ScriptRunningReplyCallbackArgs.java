package libomv.model.inventory;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class ScriptRunningReplyCallbackArgs implements CallbackArgs {
	private final UUID m_ObjectID;
	private final UUID m_ScriptID;
	private final boolean m_IsMono;
	private final boolean m_IsRunning;

	public final UUID getObjectID() {
		return m_ObjectID;
	}

	public final UUID getScriptID() {
		return m_ScriptID;
	}

	public final boolean getIsMono() {
		return m_IsMono;
	}

	public final boolean getIsRunning() {
		return m_IsRunning;
	}

	public ScriptRunningReplyCallbackArgs(UUID objectID, UUID sctriptID, boolean isMono, boolean isRunning) {
		this.m_ObjectID = objectID;
		this.m_ScriptID = sctriptID;
		this.m_IsMono = isMono;
		this.m_IsRunning = isRunning;
	}
}