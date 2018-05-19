package libomv.model.parcel;

/** Reasons agent is denied access to a parcel on the simulator */
public enum AccessDeniedReason {
	// Agent is not denied, access is granted
	NotDenied,
	// Agent is not a member of the group set for the parcel, or which owns
	// the parcel
	NotInGroup,
	// Agent is not on the parcels specific allow list
	NotOnAllowList,
	// Agent is on the parcels ban list
	BannedFromParcel,
	// Unknown
	NoAccess,
	// Agent is not age verified and parcel settings deny access to non age
	// verified avatars
	NotAgeVerified;

	public static AccessDeniedReason setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}