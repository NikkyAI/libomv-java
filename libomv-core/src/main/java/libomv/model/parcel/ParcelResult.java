package libomv.model.parcel;

/** The result of a request for parcel properties */
public enum ParcelResult {
	// No matches were found for the request
	NoData(-1),
	// Request matched a single parcel
	Single(0),
	// Request matched multiple parcels
	Multiple(1);

	public static ParcelResult setValue(int value) {
		for (ParcelResult e : values()) {
			if (e._value == value)
				return e;
		}
		return NoData;
	}

	public byte getValue() {
		return _value;
	}

	private byte _value;

	private ParcelResult(int value) {
		this._value = (byte) value;
	}
}