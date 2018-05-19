package libomv.model.parcel;

// TODO:FIXME
// Rename this to LandingType ;)
/** Type of teleport landing for a parcel */
public enum LandingTypeEnum {
	// Unset, simulator default
	None,
	// Specific landing point set for this parcel
	LandingPoint,
	// No landing point set, direct teleports enabled for this parcel
	Direct;

	public static LandingTypeEnum setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}