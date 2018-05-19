package libomv.model.friend;

public class FriendRights {
	/** The avatar has no rights */
	public static final byte None = 0;
	/** The avatar can see the online status of the target avatar */
	public static final byte CanSeeOnline = 1;
	/** The avatar can see the location of the target avatar on the map */
	public static final byte CanSeeOnMap = 2;
	/** The avatar can modify the ojects of the target avatar */
	public static final byte CanModifyObjects = 4;

	private static final String[] _names = new String[] { "None", "SeeOnline", "SeeOnMap", "ModifyObjects" };

	public static String toString(byte value) {
		if ((value & _mask) == 0)
			return _names[0];

		String rights = "";
		for (int i = 1; i < _names.length; i++) {
			if ((value & (1 << (i - 1))) != 0) {
				rights.concat(_names[i] + ", ");
			}
		}
		return rights.substring(0, rights.length() - 2);
	}

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static int getValue(int value) {
		return value;
	}

	private static final byte _mask = 0x7;
}