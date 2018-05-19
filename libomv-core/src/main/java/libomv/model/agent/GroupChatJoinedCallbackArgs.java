package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class GroupChatJoinedCallbackArgs implements CallbackArgs {
	private final UUID m_SessionID;
	private final String m_SessionName;
	private final UUID m_tmpSessionID;
	private final boolean m_Success;

	// Get the ID of the chat session
	public UUID getSessionID() {
		return m_SessionID;
	}

	// Get the name of the chat session
	public String getSessionName() {
		return m_SessionName;
	}

	// Get the ID of the agent that joined
	public UUID getTempSessionID() {
		return m_tmpSessionID;
	}

	public boolean getSucess() {
		return m_Success;
	}

	public GroupChatJoinedCallbackArgs(UUID sessionID, String name, UUID tmpSessionID, boolean success) {
		m_SessionID = sessionID;
		m_SessionName = name;
		m_tmpSessionID = tmpSessionID;
		m_Success = success;
	}
}