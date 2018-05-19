package libomv.model.terrain;

public class TerrainHeader {
	public float dcOffset;
	public int range;
	public int quantWBits;
	public int patchIDs;
	public int wordBits;

	public int getX() {
		return patchIDs >> 5;
	}

	public void setX(int value) {
		patchIDs += (value << 5);
	}

	public int getY() {
		return patchIDs & 0x1F;
	}

	public void setY(int value) {
		patchIDs |= value & 0x1F;
	}
}