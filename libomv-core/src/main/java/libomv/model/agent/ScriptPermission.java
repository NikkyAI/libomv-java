package libomv.model.agent;

/** Permission request flags, asked when a script wants to control an Avatar */
public class ScriptPermission {
	private ScriptPermission() {
	}

	/* Placeholder for empty values, shouldn't ever see this */
	public static final int None = 0;
	/* Script wants ability to take money from you */
	public static final int Debit = 1 << 1;
	/* Script wants to take camera controls for you */
	public static final int TakeControls = 1 << 2;
	/* Script wants to remap avatars controls */
	public static final int RemapControls = 1 << 3;
	/*
	 * Script wants to trigger avatar animations This function is not implemented on
	 * the grid
	 */
	public static final int TriggerAnimation = 1 << 4;
	/* Script wants to attach or detach the prim or primset to your avatar */
	public static final int Attach = 1 << 5;
	/*
	 * Script wants permission to release ownership This function is not implemented
	 * on the grid The concept of "public" objects does not exist anymore.
	 */
	public static final int ReleaseOwnership = 1 << 6;
	/* Script wants ability to link/delink with other prims */
	public static final int ChangeLinks = 1 << 7;
	/*
	 * Script wants permission to change joints This function is not implemented on
	 * the grid
	 */
	public static final int ChangeJoints = 1 << 8;
	/*
	 * Script wants permissions to change permissions This function is not
	 * implemented on the grid
	 */
	public static final int ChangePermissions = 1 << 9;
	/* Script wants to track avatars camera position and rotation */
	public static final int TrackCamera = 1 << 10;
	/* Script wants to control your camera */
	public static final int ControlCamera = 1 << 11;

	// Script wants the ability to teleport you
	public static final int Teleport = 1 << 12;

	public static int setValue(int value) {
		return (value & _mask);
	}

	public static int getValue(int value) {
		return (value & _mask);
	}

	private static final int _mask = 0xFFF;
}