package libomv.model.object;

// Special values in PayPriceReply. If the price is not one of these
// literal value of the price should be use
public enum PayPriceType {
	// Indicates that this pay option should be hidden
	Hide(-1),

	// Indicates that this pay option should have the default value
	Default(-2);

	private final byte value;

	private PayPriceType(int value) {
		this.value = (byte) value;
	}

	public static PayPriceType setValue(int value) {
		if (value >= 0 && value < values().length)
			return values()[value];
		return null;
	}

	public byte getValue() {
		return value;
	}

}