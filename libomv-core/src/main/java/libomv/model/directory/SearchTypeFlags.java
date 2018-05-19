package libomv.model.directory;

/* Land types to search dataserver for */
public class SearchTypeFlags {
	// Search Auction, Mainland and Estate
	public static final byte Any = -1;
	// Land which is currently up for auction
	public static final byte Auction = 1 << 1;
	// Land available to new landowners (formerly the FirstLand program)
	// [Obsolete]
	// public static final byte Newbie = 1 << 2;
	// Parcels which are on the mainland (Linden owned) continents
	public static final byte Mainland = 1 << 3;
	// Parcels which are on privately owned simulators
	public static final byte Estate = 1 << 4;

	private static final byte _mask = 0x1B;

	private SearchTypeFlags() {
	}

	public static final byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static final int getValue(byte value) {
		return (value & _mask);
	}

}