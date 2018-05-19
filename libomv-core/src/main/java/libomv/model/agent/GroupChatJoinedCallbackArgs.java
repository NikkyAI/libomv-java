package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class GroupChatJoinedCallbackArgs implements CallbackArgs {
	private final UUID sessionID;
	private final String sessionName;
	private final UUID tmpSessionID;
	private final boolean success;

	public GroupChatJoinedCallbackArgs(UUID sessionID, String name, UUID tmpSessionID, boolean success) {
		this.sessionID = sessionID;
		this.sessionName = name;
		this.tmpSessionID = tmpSessionID;
		this.success = success;
	}

	// Get the ID of the chat session
	public UUID getSessionID() {
		return sessionID;
	}

	// Get the name of the chat session
	public String getSessionName() {
		return sessionName;
	}

	// Get the ID of the agent that joined
	public UUID getTempSessionID() {
		return tmpSessionID;
	}

	public boolean getSucess() {
		return success;
	}

}