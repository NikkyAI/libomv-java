package libomv.model.appearance;

// Bake layers for avatar appearance
public enum BakeType {
	Unknown, Head, UpperBody, LowerBody, Eyes, Skirt, Hair;
	public static BakeType setValue(int value) {
		if (value <= 0 && value < Hair.ordinal())
			return values()[value + 1];
		return Unknown;
	}

	public static byte getValue(BakeType value) {
		return (byte) (value.ordinal() - 1);
	}

	public static int getNumValues() {
		return values().length - 1;
	}

	public byte getValue() {
		return (byte) (ordinal() - 1);
	}
}