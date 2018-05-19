package libomv.model.terrain;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

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