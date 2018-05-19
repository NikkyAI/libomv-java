package libomv.model.directory;

/* The For Sale flag in PlacesReplyData */
public enum PlacesFlags {
	// Parcel is not listed for sale
	NotForSale(0),
	// Parcel is For Sale
	ForSale(128);

	public static PlacesFlags setValue(int value) {
		for (PlacesFlags e : values())
			if (e._value == value)
				return e;
		return NotForSale;
	}

	public byte getValue() {
		return _value;
	}

	private final byte _value;

	PlacesFlags(int value) {
		this._value = (byte) value;
	}
}