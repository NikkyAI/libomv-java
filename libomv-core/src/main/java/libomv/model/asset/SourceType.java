package libomv.model.asset;

public enum SourceType {
	//
	Unknown,
	//
	Unused,
	// Asset from the asset server
	Asset,
	// Inventory item
	SimInventoryItem,
	// Estate asset, such as an estate covenant
	SimEstate;

	public static SourceType setValue(int value) {
		return values()[value];
	}

	public static byte getValue(SourceType value) {
		return (byte) value.ordinal();
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}