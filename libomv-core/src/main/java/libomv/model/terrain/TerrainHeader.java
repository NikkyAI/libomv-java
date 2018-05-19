package libomv.model.terrain;

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