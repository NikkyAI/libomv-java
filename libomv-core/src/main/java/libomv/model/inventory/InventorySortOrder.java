package libomv.model.inventory;

// [Flags]
public class InventorySortOrder {
	/* Sort by name */
	public static final byte ByName = 0;
	/* Sort by date */
	public static final byte ByDate = 1;
	/*
	 * Sort folders by name, regardless of whether items are sorted by name or date
	 */
	public static final byte FoldersByName = 2;
	/* Place system folders at the top */
	public static final byte SystemFoldersToTop = 4;

	private static final byte _mask = 0x7;

	private InventorySortOrder() {
	}

	public static byte setValue(int value) {
		return (byte) (value & _mask);
	}

	public static int getValue(byte value) {
		return value & _mask;
	}

}