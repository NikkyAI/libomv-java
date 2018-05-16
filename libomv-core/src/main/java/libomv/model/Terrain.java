package libomv.model;

public interface Terrain {

	public final class GroupHeader {
		public int Stride;
		public int PatchSize;
		public LayerType Type;
	}

	public enum LayerType {
		Land(0x4C), // 'L'
		LandExtended(0x4D), // 'M'
		Water(0x57), // 'W'
		WaterExtended(0x58), // 'X'
		Wind(0x37), // '7'
		WindExtended(0x39), // '9'
		Cloud(0x38), // '8'
		CloudExtended(0x3A); // ':'

		public static LayerType setValue(int value) {
			for (LayerType e : values()) {
				if (e._value == value) {
					return e;
				}
			}
			return Land;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		private LayerType(int value) {
			_value = (byte) value;
		}
	}

	public class TerrainHeader {
		public float DCOffset;
		public int Range;
		public int QuantWBits;
		public int PatchIDs;
		public int WordBits;

		public int getX() {
			return PatchIDs >> 5;
		}

		public void setX(int value) {
			PatchIDs += (value << 5);
		}

		public int getY() {
			return PatchIDs & 0x1F;
		}

		public void setY(int value) {
			PatchIDs |= value & 0x1F;
		}
	}

	public class TerrainPatch {
		/* X position of this patch */
		public int X;
		/* Y position of this patch */
		public int Y;
		/* A 16x16 array of floats holding decompressed layer data */
		public float[] Data;
	}

}
