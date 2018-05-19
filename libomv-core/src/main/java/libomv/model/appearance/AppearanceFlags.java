package libomv.model.appearance;

import org.apache.log4j.Logger;

import libomv.model.Appearance;

// Appearance Flags, introdued with server side baking, currently unused
// [Flags]
public enum AppearanceFlags {
	None;

	public static AppearanceFlags setValue(int value) {
		if (value >= 0 && value < values().length)
			return values()[value];
		Logger.getLogger(AppearanceFlags.class).warn("Unknown Appearance flag value" + value);
		return None;
	}

	public static byte getValue(BakeType value) {
		return (byte) (value.ordinal());
	}
}