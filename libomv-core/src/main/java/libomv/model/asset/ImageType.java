package libomv.model.asset;

// When requesting image download, type of the image requested
public enum ImageType {
	// Normal in-world object texture
	Normal,
	// Local baked avatar texture
	Baked,
	// Server baked avatar texture
	ServerBaked;

	public static ImageType setValue(int value) {
		return values()[value];
	}

	public static byte getValue(ImageType value) {
		return (byte) value.ordinal();
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}