package libomv.model.object;

// Specific Flags for MultipleObjectUpdate requests
public class UpdateType {
	// None
	public static final byte None = 0x00;
	// Change position of prims
	public static final byte Position = 0x01;
	// Change rotation of prims
	public static final byte Rotation = 0x02;
	// Change size of prims
	public static final byte Scale = 0x04;
	// Perform operation on link set
	public static final byte Linked = 0x08;
	// Scale prims uniformly, same as selecing ctrl+shift in the
	// viewer. Used in conjunction with Scale
	public static final byte Uniform = 0x10;

	public static short setValue(byte value) {
		return (short) (value & _mask);
	}

	public static byte getValue(int value) {
		return (byte) (value & _mask);
	}

	private static final short _mask = 0x1F;
}