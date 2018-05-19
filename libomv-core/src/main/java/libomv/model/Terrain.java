package libomv.model;

import libomv.utils.CallbackArgs;

public interface Terrain {

	public final class GroupHeader {
		public int stride;
		public int patchSize;
		public LayerType type;
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

	// #region EventHandling

	// Provides data for LandPatchReceived
	public class LandPatchReceivedCallbackArgs implements CallbackArgs {
		private Simulator m_Simulator;
		private int m_X;
		private int m_Y;
		private int m_PatchSize;
		private float[] m_HeightMap;

		// Simulator from that sent the data
		public Simulator getSimulator() {
			return m_Simulator;
		}

		// Sim coordinate of the patch
		public int getX() {
			return m_X;
		}

		// Sim coordinate of the patch
		public int getY() {
			return m_Y;
		}

		// Size of tha patch</summary>
		public int getPatchSize() {
			return m_PatchSize;
		}

		/// <summary>Heightmap for the patch</summary>
		public float[] getHeightMap() {
			return m_HeightMap;
		}

		public LandPatchReceivedCallbackArgs(Simulator simulator, int x, int y, int patchSize, float[] heightMap) {
			this.m_Simulator = simulator;
			this.m_X = x;
			this.m_Y = y;
			this.m_PatchSize = patchSize;
			this.m_HeightMap = heightMap;
		}
	}

}
