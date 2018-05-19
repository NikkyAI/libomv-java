package libomv.model;

public class LLObject {
	private LLObject() {
	}

	/** Item Sale Status */
	public enum SaleType {
		/** Not for sale */
		Not,
		/** The original is for sale */
		Original,
		/** Copies are for sale */
		Copy,
		/** The contents of the object are for sale */
		Contents;

		private static final String[] _SaleTypeNames = new String[] { "not", "orig", "copy", "cntn" };

		/**
		 * Translate a string name of an SaleType into the proper Type
		 *
		 * @param type
		 *            A string containing the SaleType name
		 * @return The SaleType which matches the string name, or SaleType.Unknown if no
		 *         match was found
		 */
		public static SaleType setValue(String value) {
			for (int i = 0; i < _SaleTypeNames.length; i++) {
				if (value.compareToIgnoreCase(_SaleTypeNames[i]) == 0) {
					return values()[i];
				}
			}
			return Not;
		}

		public static SaleType setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}

		@Override
		public String toString() {
			return _SaleTypeNames[ordinal()];
		}
	}

}
