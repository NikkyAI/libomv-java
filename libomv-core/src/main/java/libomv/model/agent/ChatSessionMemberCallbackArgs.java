package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Data sent when an agent joins or leaves a chat session your agent is
// currently participating in
public class ChatSessionMemberCallbackArgs implements CallbackArgs {
	private final UUID m_SessionID;
	private final UUID m_AgentID;
	private final boolean m_added;

	// Get the ID of the chat session
	public UUID getSessionID() {
		return m_SessionID;
	}

	// Get the ID of the agent that joined
	public UUID getAgentID() {
		return m_AgentID;
	}

	public boolean getAdded() {
		return m_added;
	}

	public ChatSessionMemberCallbackArgs(UUID sessionID, UUID agentID, boolean added) {
		this.m_SessionID = sessionID;
		this.m_AgentID = agentID;
		this.m_added = added;
	}
}