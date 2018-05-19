package libomv.model.terrain;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Provides data for LandPatchReceived
public class LandPatchReceivedCallbackArgs implements CallbackArgs {
	private Simulator simulator;
	private int x;
	private int y;
	private int patchSize;
	private float[] heightMap;

	public LandPatchReceivedCallbackArgs(Simulator simulator, int x, int y, int patchSize, float[] heightMap) {
		this.simulator = simulator;
		this.x = x;
		this.y = y;
		this.patchSize = patchSize;
		this.heightMap = heightMap;
	}

	// Simulator from that sent the data
	public Simulator getSimulator() {
		return simulator;
	}

	// Sim coordinate of the patch
	public int getX() {
		return x;
	}

	// Sim coordinate of the patch
	public int getY() {
		return y;
	}

	// Size of tha patch</summary>
	public int getPatchSize() {
		return patchSize;
	}

	/// <summary>Heightmap for the patch</summary>
	public float[] getHeightMap() {
		return heightMap;
	}

}