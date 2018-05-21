package libomv.model.parcel;

/** The result of a request for parcel properties */
public enum ParcelResult {
	// No matches were found for the request
	NoData(-1),
	// Request matched a single parcel
	Single(0),
	// Request matched multiple parcels
	Multiple(1);

	private byte value;

	private ParcelResult(int value) {
		this.value = (byte) value;
	}

	public static ParcelResult setValue(int value) {
		for (ParcelResult e : values()) {
			if (e.value == value)
				return e;
		}
		return NoData;
	}

	public byte getValue() {
		return value;
	}

}