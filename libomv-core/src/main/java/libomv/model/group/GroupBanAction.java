package libomv.model.group;

// Ban actions available for group members
public enum GroupBanAction {
	// Ban agent from joining a group
	Ban(1),
	// Remove restriction on agent jointing a group
	Unban(2);

	public static GroupBanAction setValue(int value) {
		for (GroupBanAction e : values()) {
			if (e.val == value)
				return e;
		}
		return Ban;
	}

	public int getValue() {
		return val;
	}

	private int val;

	private GroupBanAction(int value) {
		val = value;
	}
}