package libomv.model.agent;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

public class ChatCallbackArgs implements CallbackArgs {
	private ChatAudibleLevel audible;
	private ChatType type;
	private ChatSourceType sourcetype;
	private String message;
	private String fromName;
	private UUID sourceId;
	private UUID ownerId;
	private Vector3 position;

	public ChatCallbackArgs(ChatAudibleLevel audible, ChatType type, ChatSourceType sourcetype, String fromName,
			String message, UUID sourceId, UUID ownerId, Vector3 position) {
		this.message = message;
		this.fromName = fromName;
		this.audible = audible;
		this.type = type;
		this.sourcetype = sourcetype;
		this.sourceId = sourceId;
		this.ownerId = ownerId;
		this.position = position;
	}

	public String getMessage() {
		return message;
	}

	public String getFromName() {
		return fromName;
	}

	public ChatAudibleLevel getAudible() {
		return audible;
	}

	public ChatType getType() {
		return type;
	}

	public ChatSourceType getSourceType() {
		return sourcetype;
	}

	public UUID getSourceID() {
		return sourceId;
	}

	public UUID getOwnerID() {
		return ownerId;
	}

	public Vector3 getPosition() {
		return position;
	}

}