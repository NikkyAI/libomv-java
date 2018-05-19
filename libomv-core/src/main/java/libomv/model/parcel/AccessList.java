package libomv.model.parcel;

/**
 * Flags used in the ParcelAccessListRequest packet to specify whether we want
 * the access list (whitelist), ban list (blacklist), or both
 */
// [Flags]
public class AccessList {
	// Request the access list
	public static final byte Access = 0x1;
	// Request the ban list
	public static final byte Ban = 0x2;
	// Request both White and Black lists
	public static final byte Both = 0x3;

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static int getValue(byte value) {
		return value & _mask;
	}

	private static final byte _mask = 0x3;
}