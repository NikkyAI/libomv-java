package libomv.model.agent;

/* */
public class ScriptSensorTypeFlags {
	/* */
	public static final byte Agent = 1;
	/* */
	public static final byte Active = 2;
	/* */
	public static final byte Passive = 4;
	/* */
	public static final byte Scripted = 8;

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static byte getValue(byte value) {
		return (byte) (value & _mask);
	}

	private static final byte _mask = 0xF;
}