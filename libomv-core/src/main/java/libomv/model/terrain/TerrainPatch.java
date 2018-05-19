package libomv.model.terrain;

public class TerrainPatch {
	/* X position of this patch */
	public int x;
	/* Y position of this patch */
	public int y;
	/* A 16x16 array of floats holding decompressed layer data */
	public float[] data;
}