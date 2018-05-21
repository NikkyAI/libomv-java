package libomv.model.directory;

/* The For Sale flag in PlacesReplyData */
public enum PlacesFlags {
	// Parcel is not listed for sale
	NotForSale(0),
	// Parcel is For Sale
	ForSale(128);

	private final byte value;

	PlacesFlags(int value) {
		this.value = (byte) value;
	}

	public static PlacesFlags setValue(int value) {
		for (PlacesFlags e : values())
			if (e.value == value)
				return e;
		return NotForSale;
	}

	public byte getValue() {
		return value;
	}

}