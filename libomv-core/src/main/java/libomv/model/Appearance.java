package libomv.model;

import org.apache.log4j.Logger;

public interface Appearance {

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

	// Bake layers for avatar appearance
	public enum BakeType {
		Unknown, Head, UpperBody, LowerBody, Eyes, Skirt, Hair;
		public static BakeType setValue(int value) {
			if (value <= 0 && value < Hair.ordinal())
				return values()[value + 1];
			return Unknown;
		}

		public static byte getValue(BakeType value) {
			return (byte) (value.ordinal() - 1);
		}

		public static int getNumValues() {
			return values().length - 1;
		}

		public byte getValue() {
			return (byte) (ordinal() - 1);
		}
	}

}
