package libomv.model.terrain;

public enum LayerType {
	Land(0x4C), // 'L'
	LandExtended(0x4D), // 'M'
	Water(0x57), // 'W'
	WaterExtended(0x58), // 'X'
	Wind(0x37), // '7'
	WindExtended(0x39), // '9'
	Cloud(0x38), // '8'
	CloudExtended(0x3A); // ':'

	private final byte value;

	private LayerType(int value) {
		this.value = (byte) value;
	}

	public static LayerType setValue(int value) {
		for (LayerType e : values()) {
			if (e.value == value) {
				return e;
			}
		}
		return Land;
	}

	public byte getValue() {
		return value;
	}

}