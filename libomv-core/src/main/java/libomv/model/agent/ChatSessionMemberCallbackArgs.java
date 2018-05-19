package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Data sent when an agent joins or leaves a chat session your agent is
// currently participating in
public class ChatSessionMemberCallbackArgs implements CallbackArgs {
	private final UUID sessionID;
	private final UUID agentID;
	private final boolean added;

	public ChatSessionMemberCallbackArgs(UUID sessionID, UUID agentID, boolean added) {
		this.sessionID = sessionID;
		this.agentID = agentID;
		this.added = added;
	}

	// Get the ID of the chat session
	public UUID getSessionID() {
		return sessionID;
	}

	// Get the ID of the agent that joined
	public UUID getAgentID() {
		return agentID;
	}

	public boolean getAdded() {
		return added;
	}

}