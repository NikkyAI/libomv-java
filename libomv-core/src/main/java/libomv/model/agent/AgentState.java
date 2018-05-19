package libomv.model.agent;

/* Action state of the avatar, which can currently be typing and editing */
public class AgentState {
	/* */
	public static final byte None = 0x00;
	/* */
	public static final byte Typing = 0x04;
	/* */
	public static final byte Editing = 0x10;

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static byte getValue(byte value) {
		return (byte) (value & _mask);
	}

	private static final byte _mask = 0x14;
}