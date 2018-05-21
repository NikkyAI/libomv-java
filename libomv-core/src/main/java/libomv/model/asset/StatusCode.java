package libomv.model.asset;

public enum StatusCode {
	OK(0),
	// Transfer completed
	Done(1), Skip(2), Abort(3),
	// Unknown error occurred
	Error(-1),
	// Equivalent to a 404 error
	UnknownSource(-2),
	// Client does not have permission for that resource
	InsufficientPermissions(-3),
	// Unknown status
	Unknown(-4);

	private final byte value;

	private StatusCode(int value) {
		this.value = (byte) value;
	}

	public static StatusCode setValue(int value) {
		for (StatusCode e : values()) {
			if (e.value == value)
				return e;
		}
		return Unknown;
	}

	public static byte getValue(StatusCode value) {
		for (StatusCode e : values()) {
			if (e == value)
				return e.value;
		}
		return Unknown.value;
	}

	public byte getValue() {
		return value;
	}

}