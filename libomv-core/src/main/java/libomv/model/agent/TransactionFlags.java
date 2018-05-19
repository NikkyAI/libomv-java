package libomv.model.agent;

/*  */
public class TransactionFlags {
	/* */
	public static final byte None = 0x0;
	/* */
	public static final byte SourceGroup = 0x1;
	/* */
	public static final byte DestGroup = 0x2;
	/* */
	public static final byte OwnerGroup = 0x4;
	/* */
	public static final byte SimultaneousContribution = 0x8;
	/* */
	public static final byte ContributionRemoval = 0x10;

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static byte getValue(byte value) {
		return (byte) (value & _mask);
	}

	private static final byte _mask = 0x1F;
}