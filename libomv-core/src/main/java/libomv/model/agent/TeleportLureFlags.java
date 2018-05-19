package libomv.model.agent;

/* */
public enum TeleportLureFlags {
	/* */
	NormalLure,
	/* */
	GodlikeLure,
	/* */
	GodlikePursuit;

	public static TeleportLureFlags setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}