package libomv.model.estate;

public enum EstateReturnFlags {
	/// <summary>No flags set</summary>
	None(2),
	/// <summary>Only return targets scripted objects</summary>
	ReturnScripted(6),
	/// <summary>Only return targets objects if on others land</summary>
	ReturnOnOthersLand(3),
	/// <summary>Returns target's scripted objects and objects on other
	/// parcels</summary>
	ReturnScriptedAndOnOthers(7);

	public static EstateReturnFlags setValue(int value) {
		for (EstateReturnFlags e : values()) {
			if (e.val == value)
				return e;
		}
		return None;
	}

	public int getValue() {
		return val;
	}

	private int val;

	private EstateReturnFlags(int value) {
		val = value;
	}
}
// #endregion