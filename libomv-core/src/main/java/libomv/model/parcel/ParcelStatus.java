package libomv.model.parcel;

/** Parcel ownership status */
public enum ParcelStatus {
	// Placeholder
	None(-1),
	// Parcel is leased (owned) by an avatar or group
	Leased(0),
	// Parcel is in process of being leased (purchased) by an avatar or
	// group
	LeasePending(1),
	// Parcel has been abandoned back to Governor Linden
	Abandoned(2);

	public static ParcelStatus setValue(int value) {
		for (ParcelStatus e : values()) {
			if (e._value == value)
				return e;
		}
		return None;
	}

	public byte getValue() {
		return _value;
	}

	private byte _value;

	private ParcelStatus(int value) {
		this._value = (byte) value;
	}
}