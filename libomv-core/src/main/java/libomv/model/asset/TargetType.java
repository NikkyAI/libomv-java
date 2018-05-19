package libomv.model.asset;

public enum TargetType {
	Unknown, File, VFile;

	public static TargetType setValue(int value) {
		return values()[value];
	}

	public static byte getValue(TargetType value) {
		return (byte) value.ordinal();
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}