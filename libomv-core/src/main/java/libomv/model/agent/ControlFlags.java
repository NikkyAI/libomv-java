package libomv.model.agent;

/* Used to specify movement actions for your agent */
public class ControlFlags {
	private static final int CONTROL_AT_POS_INDEX = 0;
	private static final int CONTROL_AT_NEG_INDEX = 1;
	private static final int CONTROL_LEFT_POS_INDEX = 2;
	private static final int CONTROL_LEFT_NEG_INDEX = 3;
	private static final int CONTROL_UP_POS_INDEX = 4;
	private static final int CONTROL_UP_NEG_INDEX = 5;
	private static final int CONTROL_PITCH_POS_INDEX = 6;
	private static final int CONTROL_PITCH_NEG_INDEX = 7;
	private static final int CONTROL_YAW_POS_INDEX = 8;
	private static final int CONTROL_YAW_NEG_INDEX = 9;
	private static final int CONTROL_FAST_AT_INDEX = 10;
	private static final int CONTROL_FAST_LEFT_INDEX = 11;
	private static final int CONTROL_FAST_UP_INDEX = 12;
	private static final int CONTROL_FLY_INDEX = 13;
	private static final int CONTROL_STOP_INDEX = 14;
	private static final int CONTROL_FINISH_ANIM_INDEX = 15;
	private static final int CONTROL_STAND_UP_INDEX = 16;
	private static final int CONTROL_SIT_ON_GROUND_INDEX = 17;
	private static final int CONTROL_MOUSELOOK_INDEX = 18;
	private static final int CONTROL_NUDGE_AT_POS_INDEX = 19;
	private static final int CONTROL_NUDGE_AT_NEG_INDEX = 20;
	private static final int CONTROL_NUDGE_LEFT_POS_INDEX = 21;
	private static final int CONTROL_NUDGE_LEFT_NEG_INDEX = 22;
	private static final int CONTROL_NUDGE_UP_POS_INDEX = 23;
	private static final int CONTROL_NUDGE_UP_NEG_INDEX = 24;
	private static final int CONTROL_TURN_LEFT_INDEX = 25;
	private static final int CONTROL_TURN_RIGHT_INDEX = 26;
	private static final int CONTROL_AWAY_INDEX = 27;
	private static final int CONTROL_LBUTTON_DOWN_INDEX = 28;
	private static final int CONTROL_LBUTTON_UP_INDEX = 29;
	private static final int CONTROL_ML_LBUTTON_DOWN_INDEX = 30;
	private static final int CONTROL_ML_LBUTTON_UP_INDEX = 31;

	// Empty flag
	public static final int NONE = 0;
	// Move Forward (SL Keybinding: W/Up Arrow)
	public static final int AGENT_CONTROL_AT_POS = 0x1 << CONTROL_AT_POS_INDEX;
	// t Move Backward (SL Keybinding: S/Down Arrow)
	public static final int AGENT_CONTROL_AT_NEG = 0x1 << CONTROL_AT_NEG_INDEX;
	// Move Left (SL Keybinding: Shift-(A/Left Arrow))
	public static final int AGENT_CONTROL_LEFT_POS = 0x1 << CONTROL_LEFT_POS_INDEX;
	// Move Right (SL Keybinding: Shift-(D/Right Arrow))
	public static final int AGENT_CONTROL_LEFT_NEG = 0x1 << CONTROL_LEFT_NEG_INDEX;
	// Not Flying: Jump/Flying: Move Up (SL Keybinding: E)
	public static final int AGENT_CONTROL_UP_POS = 0x1 << CONTROL_UP_POS_INDEX;
	// Not Flying: Croutch/Flying: Move Down (SL Keybinding: C)
	public static final int AGENT_CONTROL_UP_NEG = 0x1 << CONTROL_UP_NEG_INDEX;
	// Unused
	public static final int AGENT_CONTROL_PITCH_POS = 0x1 << CONTROL_PITCH_POS_INDEX;
	// Unused
	public static final int AGENT_CONTROL_PITCH_NEG = 0x1 << CONTROL_PITCH_NEG_INDEX;
	// Unused
	public static final int AGENT_CONTROL_YAW_POS = 0x1 << CONTROL_YAW_POS_INDEX;
	// Unused
	public static final int AGENT_CONTROL_YAW_NEG = 0x1 << CONTROL_YAW_NEG_INDEX;
	// ORed with AGENT_CONTROL_AT_* if the keyboard is being used
	public static final int AGENT_CONTROL_FAST_AT = 0x1 << CONTROL_FAST_AT_INDEX;
	// ORed with AGENT_CONTROL_LEFT_* if the keyboard is being used
	public static final int AGENT_CONTROL_FAST_LEFT = 0x1 << CONTROL_FAST_LEFT_INDEX;
	// ORed with AGENT_CONTROL_UP_* if the keyboard is being used
	public static final int AGENT_CONTROL_FAST_UP = 0x1 << CONTROL_FAST_UP_INDEX;
	// Fly
	public static final int AGENT_CONTROL_FLY = 0x1 << CONTROL_FLY_INDEX;
	//
	public static final int AGENT_CONTROL_STOP = 0x1 << CONTROL_STOP_INDEX;
	// Finish our current animation
	public static final int AGENT_CONTROL_FINISH_ANIM = 0x1 << CONTROL_FINISH_ANIM_INDEX;
	// Stand up from the ground or a prim seat
	public static final int AGENT_CONTROL_STAND_UP = 0x1 << CONTROL_STAND_UP_INDEX;
	// Sit on the ground at our current location
	public static final int AGENT_CONTROL_SIT_ON_GROUND = 0x1 << CONTROL_SIT_ON_GROUND_INDEX;
	// Whether mouselook is currently enabled
	public static final int AGENT_CONTROL_MOUSELOOK = 0x1 << CONTROL_MOUSELOOK_INDEX;
	// Legacy, used if a key was pressed for less than a certain amount of
	// time
	public static final int AGENT_CONTROL_NUDGE_AT_POS = 0x1 << CONTROL_NUDGE_AT_POS_INDEX;
	// Legacy, used if a key was pressed for less than a certain amount of
	// time
	public static final int AGENT_CONTROL_NUDGE_AT_NEG = 0x1 << CONTROL_NUDGE_AT_NEG_INDEX;
	// Legacy, used if a key was pressed for less than a certain amount of
	// time
	public static final int AGENT_CONTROL_NUDGE_LEFT_POS = 0x1 << CONTROL_NUDGE_LEFT_POS_INDEX;
	// Legacy, used if a key was pressed for less than a certain amount of
	// time
	public static final int AGENT_CONTROL_NUDGE_LEFT_NEG = 0x1 << CONTROL_NUDGE_LEFT_NEG_INDEX;
	// Legacy, used if a key was pressed for less than a certain amount of
	// time
	public static final int AGENT_CONTROL_NUDGE_UP_POS = 0x1 << CONTROL_NUDGE_UP_POS_INDEX;
	// Legacy, used if a key was pressed for less than a certain amount of
	// time
	public static final int AGENT_CONTROL_NUDGE_UP_NEG = 0x1 << CONTROL_NUDGE_UP_NEG_INDEX;
	//
	public static final int AGENT_CONTROL_TURN_LEFT = 0x1 << CONTROL_TURN_LEFT_INDEX;
	//
	public static final int AGENT_CONTROL_TURN_RIGHT = 0x1 << CONTROL_TURN_RIGHT_INDEX;
	// Set when the avatar is idled or set to away. Note that the away
	// animation is
	// activated separately from setting this flag
	public static final int AGENT_CONTROL_AWAY = 0x1 << CONTROL_AWAY_INDEX;
	//
	public static final int AGENT_CONTROL_LBUTTON_DOWN = 0x1 << CONTROL_LBUTTON_DOWN_INDEX;
	//
	public static final int AGENT_CONTROL_LBUTTON_UP = 0x1 << CONTROL_LBUTTON_UP_INDEX;
	//
	public static final int AGENT_CONTROL_ML_LBUTTON_DOWN = 0x1 << CONTROL_ML_LBUTTON_DOWN_INDEX;
	//
	public static final int AGENT_CONTROL_ML_LBUTTON_UP = 0x1 << CONTROL_ML_LBUTTON_UP_INDEX;

	private ControlFlags() {
	}

	public static int setValue(int value) {
		return value;
	}

	public static int getValue(int value) {
		return value;
	}
}