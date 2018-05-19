package libomv.model.parcel;

/**
 * Parcel overlay type. This is used primarily for highlighting and coloring
 * which is why it is a single integer instead of a set of flags
 *
 * These values seem to be poorly thought out. The first three bits represent a
 * single value, not flags. For example Auction (0x05) is not a combination of
 * OwnedByOther (0x01) and ForSale(0x04). However, the BorderWest and
 * BorderSouth values are bit flags that get attached to the value stored in the
 * first three bits. Bits four, five, and six are unused
 */
public enum ParcelOverlayType {
	// Public land
	Public(0),
	// Land is owned by another avatar
	OwnedByOther(1),
	// Land is owned by a group
	OwnedByGroup(2),
	// Land is owned by the current avatar
	OwnedBySelf(3),
	// Land is for sale
	ForSale(4),
	// Land is being auctioned
	Auction(5),
	// Land is private
	Private(32),
	// To the west of this area is a parcel border
	BorderWest(64),
	// To the south of this area is a parcel border
	BorderSouth(128);

	public static ParcelOverlayType setValue(int value) {
		for (ParcelOverlayType e : values()) {
			if (e._value == value)
				return e;
		}
		return Public;
	}

	public byte getValue() {
		return _value;
	}

	private byte _value;

	private ParcelOverlayType(int value) {
		this._value = (byte) value;
	}
}