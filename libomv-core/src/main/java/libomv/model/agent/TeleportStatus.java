package libomv.model.agent;

/* Current teleport status */
public enum TeleportStatus {
	/* Unknown status */
	None,
	/* Teleport initialized */
	Start,
	/* Teleport in progress */
	Progress,
	/* Teleport failed */
	Failed,
	/* Teleport completed */
	Finished,
	/* Teleport cancelled */
	Cancelled;

	public static TeleportStatus setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}