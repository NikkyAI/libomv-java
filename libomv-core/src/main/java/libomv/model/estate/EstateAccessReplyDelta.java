package libomv.model.estate;

// Used by EstateOwnerMessage packets
public enum EstateAccessReplyDelta {
	None(0), AllowedUsers(17), AllowedGroups(18), EstateBans(20), EstateManagers(24);

	private int val;

	private EstateAccessReplyDelta(int value) {
		val = value;
	}

	public static EstateAccessReplyDelta setValue(int value) {
		for (EstateAccessReplyDelta e : values()) {
			if (e.val == value)
				return e;
		}
		return None;
	}

	public int getValue() {
		return val;
	}

}