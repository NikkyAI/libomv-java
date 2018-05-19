package libomv.model.agent;

/*
 * The action an avatar is doing when looking at something, used in ViewerEffect
 * packets for the LookAt effect
 */
public enum LookAtType {
	/* */
	None,
	/* */
	Idle,
	/* */
	AutoListen,
	/* */
	FreeLook,
	/* */
	Respond,
	/* */
	Hover,
	/* Deprecated */
	Conversation,
	/* */
	Select,
	/* */
	Focus,
	/* */
	Mouselook,
	/* */
	Clear;

	public static LookAtType setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}