package libomv.model.parcel;

/** The tool to use when modifying terrain levels */
public enum TerraformAction {
	// Level the terrain
	Level,
	// Raise the terrain
	Raise,
	// Lower the terrain
	Lower,
	// Smooth the terrain
	Smooth,
	// Add random noise to the terrain
	Noise,
	// Revert terrain to simulator default
	Revert;

	public static TerraformAction setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}