package libomv.model.parcel;

/** Type of return to use when returning objects from a parcel */
// [Flags]
public class ObjectReturnType {
	//
	public static final byte None = 0;
	// Return objects owned by parcel owner
	public static final byte Owner = 1 << 1;
	// Return objects set to group
	public static final byte Group = 1 << 2;
	// Return objects not owned by parcel owner or set to group
	public static final byte Other = 1 << 3;
	// Return a specific list of objects on parcel
	public static final byte List = 1 << 4;
	// Return objects that are marked for-sale
	public static final byte Sell = 1 << 5;

	private static final byte _mask = 0x1F;

	private ObjectReturnType() {
	}

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static int getValue(byte value) {
		return value & _mask;
	}

}