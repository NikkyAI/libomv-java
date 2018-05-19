package libomv.model.asset;

// #region Enums
public enum EstateAssetType {
	None, Covenant;

	public static EstateAssetType setValue(int value) {
		return values()[value + 1];
	}

	public static byte getValue(EstateAssetType value) {
		return (byte) (value.ordinal() - 1);
	}

	public byte getValue() {
		return (byte) (ordinal() - 1);
	}
}