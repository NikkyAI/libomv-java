package libomv.model.agent;

/*  */
public enum ChatAudibleLevel {
	/*  */
	Not(-1),
	/*  */
	Barely(0),
	/*  */
	Fully(1);

	private int val;

	private ChatAudibleLevel(int value) {
		val = value;
	}

	public static ChatAudibleLevel setValue(byte value) {
		for (ChatAudibleLevel e : values()) {
			if (e.val == value)
				return e;
		}
		return Barely;
	}

	public int getValue() {
		return val;
	}

}