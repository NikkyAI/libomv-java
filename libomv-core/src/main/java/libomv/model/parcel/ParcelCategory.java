package libomv.model.parcel;

/** Category parcel is listed in under search */
public enum ParcelCategory {
	// No assigned category
	None(0),
	// Linden Infohub or public area
	Linden(1),
	// Adult themed area
	Adult(2),
	// Arts and Culture
	Arts(3),
	// Business
	Business(4),
	// Educational
	Educational(5),
	// Gaming
	Gaming(6),
	// Hangout or Club
	Hangout(7),
	// Newcomer friendly
	Newcomer(8),
	// Parks and Nature
	Park(9),
	// Residential
	Residential(10),
	// Shopping
	Shopping(11),
	// Not Used?
	Stage(12),
	// Other
	Other(13),
	// Not an actual category, only used for queries
	Any(-1);

	private byte value;

	private ParcelCategory(int value) {
		this.value = (byte) value;
	}

	public static ParcelCategory setValue(int value) {
		for (ParcelCategory e : values()) {
			if (e.value == value)
				return e;
		}
		return None;
	}

	public byte getValue() {
		return value;
	}

}