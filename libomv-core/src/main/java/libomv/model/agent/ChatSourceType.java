package libomv.model.agent;

/* Identifies the source of a chat message */
public enum ChatSourceType {
	/* Chat from the grid or simulator */
	System,
	/* Chat from another avatar */
	Agent,
	/* Chat from an object */
	Object;

	public static ChatSourceType setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}