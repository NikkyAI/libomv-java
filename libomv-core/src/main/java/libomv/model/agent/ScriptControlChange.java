package libomv.model.agent;

/*
 * Flags sent when a script takes or releases a control
 *
 * NOTE: (need to verify) These might be a subset of the ControlFlags enum in
 * Movement,
 */
public class ScriptControlChange {
	/* No Flags set */
	public static final int None = 0;
	/* Forward (W or up Arrow) */
	public static final int Forward = 0x1;
	/* Back (S or down arrow) */
	public static final int Back = 0x2;
	/* Move left (shift+A or left arrow) */
	public static final int Left = 0x4;
	/* Move right (shift+D or right arrow) */
	public static final int Right = 0x8;
	/* Up (E or PgUp) */
	public static final int Up = 0x10;
	/* Down (C or PgDown) */
	public static final int Down = 0x20;
	/* Rotate left (A or left arrow) */
	public static final int RotateLeft = 0x100;
	/* Rotate right (D or right arrow) */
	public static final int RotateRight = 0x200;
	/* Left Mouse Button */
	public static final int LeftButton = 0x10000000;
	/* Left Mouse button in MouseLook */
	public static final int MouseLookLeftButton = 0x40000000;

	public static int setValue(int value) {
		return value & _mask;
	}

	public static int getValue(int value) {
		return value & _mask;
	}

	private static final int _mask = 0x5000033F;
}