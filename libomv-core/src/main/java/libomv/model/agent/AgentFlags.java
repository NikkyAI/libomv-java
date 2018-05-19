package libomv.model.agent;

/* Currently only used to hide your group title */
public enum AgentFlags {
	/* No flags set */
	None,
	/* Hide your group title */
	HideTitle;

	public static AgentFlags setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}