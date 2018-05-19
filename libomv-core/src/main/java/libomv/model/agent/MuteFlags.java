package libomv.model.agent;

/**
 * Flags of mute entry
 */
// [Flags]
public class MuteFlags {
	// No exceptions
	public static final byte Default = 0x0;
	// Don't mute text chat
	public static final byte TextChat = 0x1;
	// Don't mute voice chat
	public static final byte VoiceChat = 0x2;
	// Don't mute particles
	public static final byte Particles = 0x4;
	// Don't mute sounds
	public static final byte ObjectSounds = 0x8;
	// Don't mute
	public static final byte All = 0xf;

	private static final byte _mask = 0xf;

	private MuteFlags() {
	}

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static int getValue(byte value) {
		return value & _mask;
	}

}