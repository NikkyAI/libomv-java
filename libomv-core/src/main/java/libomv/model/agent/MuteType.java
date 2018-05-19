package libomv.model.agent;

/**
 * Type of mute entry
 */
public enum MuteType {
	// Object muted by name
	ByName,
	// Muted resident
	Resident,
	// Object muted by UUID
	Object,
	// Muted group
	Group,
	// Muted external entry
	External;

	public static MuteType setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}