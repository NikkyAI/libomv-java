package libomv.model.directory;

/* Classified ad query options */
public class ClassifiedQueryFlags {
	// Include PG ads in results
	public static final byte PG = 1 << 2;
	// Include Mature ads in results
	public static final byte Mature = 1 << 3;
	// Include Adult ads in results
	public static final byte Adult = 1 << 6;
	// Include all ads in results
	public static final byte All = PG | Mature | Adult;

	private static final byte _mask = 0x4C;

	private ClassifiedQueryFlags() {
	}

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static byte getValue(byte value) {
		return (byte) (value & _mask);
	}

}