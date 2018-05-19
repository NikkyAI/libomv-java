package libomv.model.parcel;

/** The tool size to use when changing terrain levels */
// [Flags]
public class TerraformBrushSize {
	static float LAND_BRUSH_SIZE[] = { 1.0f, 2.0f, 4.0f };

	// Small
	public static final byte Small = 1 << 0;
	// Medium
	public static final byte Medium = 1 << 1;
	// Large
	public static final byte Large = 1 << 2;

	public static byte getIndex(float value) {
		for (byte i = 2; i >= 0; i--) {
			if (value > LAND_BRUSH_SIZE[i]) {
				return i;
			}
		}
		return 0;
	}

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static int getValue(byte value) {
		return value & _mask;
	}

	private static final byte _mask = 0x7;
}