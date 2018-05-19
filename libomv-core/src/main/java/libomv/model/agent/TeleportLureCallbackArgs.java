package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class TeleportLureCallbackArgs implements CallbackArgs {
	private final UUID fromID;
	private final String fromName;
	private final UUID lureID;
	private final String message;
	private final LureLocation location; // if null, it's a godlike lure request

	public UUID getFromID() {
		return fromID;
	}

	public String getFromName() {
		return fromName;
	}

	public UUID getLureID() {
		return lureID;
	}

	public String getMessage() {
		return message;
	}

	public LureLocation getLocation() {
		return location;
	}

	public TeleportLureCallbackArgs(UUID fromID, String fromName, UUID lureID, String message,
			LureLocation location) {
		this.fromID = fromID;
		this.fromName = fromName;
		this.lureID = lureID;
		this.location = location;
		this.message = message;
	}
}