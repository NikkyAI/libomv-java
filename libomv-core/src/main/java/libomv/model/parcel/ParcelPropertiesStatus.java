package libomv.model.parcel;

/**
 * Sequence ID in ParcelPropertiesReply packets (sent when avatar tries to cross
 * a parcel border)
 */
public enum ParcelPropertiesStatus {
	None(0),
	// Parcel is currently selected
	ParcelSelected(-10000),
	// Parcel restricted to a group the avatar is not a member of
	CollisionNotInGroup(-20000),
	// Avatar is banned from the parcel
	CollisionBanned(-30000),
	// Parcel is restricted to an access list that the avatar is not on
	CollisionNotOnAccessList(-40000),
	// Response to hovering over a parcel
	HoveredOverParcel(-50000);

	public static ParcelPropertiesStatus setValue(int value) {
		for (ParcelPropertiesStatus e : values()) {
			if (e._value == value)
				return e;
		}
		return None;
	}

	public int getValue() {
		return _value;
	}

	private int _value;

	ParcelPropertiesStatus(int value) {
		this._value = value;
	}
}