package libomv.model.directory;

/* Classified Ad categories */
public enum ClassifiedCategories {
	// Classified is listed in the Any category
	Any,
	// Classified is shopping related
	Shopping,
	// Classified is
	LandRental,
	//
	PropertyRental,
	//
	SpecialAttraction,
	//
	NewProducts,
	//
	Employment,
	//
	Wanted,
	//
	Service,
	//
	Personal;

	public static ClassifiedCategories setValue(int value) {
		return values()[value];
	}

	public static byte getValue(ClassifiedCategories value) {
		return (byte) value.ordinal();
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}