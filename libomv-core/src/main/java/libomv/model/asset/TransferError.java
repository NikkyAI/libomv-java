package libomv.model.asset;

public enum TransferError {
	None(0),
	Failed(-1),
	AssetNotFound(-3),
	AssetNotFoundInDatabase(-4),
	InsufficientPermissions(-5),
	EOF(-39),
	CannotOpenFile(-42),
	FileNotFound(-43),
	FileIsEmpty(-44),
	TCPTimeout(-23016),
	CircuitGone(-23017);

	private final byte value;

	private TransferError(int value) {
		this.value = (byte) value;
	}

	public static TransferError setValue(int value) {
		for (TransferError e : values()) {
			if (e.value == value)
				return e;
		}
		return Failed;
	}

	public static byte getValue(TransferError value) {
		for (TransferError e : values()) {
			if (e == value)
				return e.value;
		}
		return Failed.value;
	}

	public byte getValue() {
		return value;
	}

}