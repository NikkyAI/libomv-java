package libomv.model.directory;

/* The content rating of the event */
public enum EventFlags {
	// Event is PG
	PG,
	// Event is Mature
	Mature,
	// Event is Adult
	Adult;

	public static EventFlags setValue(int value) {
		return values()[value];
	}

	public static byte getValue(EventFlags value) {
		return (byte) value.ordinal();
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}