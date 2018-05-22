package libomv.model.estate;

// Used by EstateOwnerMessage packets
public enum EstateAccessDelta {
	None(0), BanUser(64), BanUserAllEstates(66), UnbanUser(128), UnbanUserAllEstates(130), AddManager(
			256), AddManagerAllEstates(257), RemoveManager(512), RemoveManagerAllEstates(513), AddUserAsAllowed(
					4), AddAllowedAllEstates(6), RemoveUserAsAllowed(8), RemoveUserAllowedAllEstates(
							10), AddGroupAsAllowed(16), AddGroupAllowedAllEstates(
									18), RemoveGroupAsAllowed(32), RemoveGroupAllowedAllEstates(34);

	private int val;

	private EstateAccessDelta(int value) {
		val = value;
	}

	public static EstateAccessDelta setValue(int value) {
		for (EstateAccessDelta e : values()) {
			if (e.val == value)
				return e;
		}
		return None;
	}

	public int getValue() {
		return val;
	}

}