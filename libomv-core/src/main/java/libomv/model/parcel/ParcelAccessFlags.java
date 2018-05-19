package libomv.model.parcel;

/** Blacklist/Whitelist flags used in parcels Access List */
public enum ParcelAccessFlags {
	// Agent is denied access
	NoAccess,
	// Agent is granted access
	Access;

	public static ParcelAccessFlags setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}