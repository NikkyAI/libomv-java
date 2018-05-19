package libomv.model.asset;

public enum ChannelType {
	Unknown,
	// Unknown
	Misc,
	// Virtually all asset transfers use this channel
	Asset;

	public static ChannelType setValue(int value) {
		return values()[value];
	}

	public static byte getValue(ChannelType value) {
		return (byte) value.ordinal();
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}