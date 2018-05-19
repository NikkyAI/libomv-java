package libomv.model.directory;

/*
 * Classified Ad Options
 *
 * There appear to be two formats the flags are packed in. This set of flags is
 * for the newer style
 */
public class ClassifiedFlags {
	//
	public static final byte None = 1 << 0;
	//
	public static final byte Mature = 1 << 1;
	//
	public static final byte Enabled = 1 << 2;
	// Deprecated
	// public static final byte HasPrice = 1 << 3;
	//
	public static final byte UpdateTime = 1 << 4;
	//
	public static final byte AutoRenew = 1 << 5;

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static byte getValue(byte value) {
		return (byte) (value & _mask);
	}

	private static final byte _mask = 0x37;
}