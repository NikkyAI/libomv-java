package libomv.model.grid;

import libomv.types.UUID;

public final class GridLayer {
	public int bottom;
	public int left;
	public int top;
	public int right;
	public UUID imageID;

	public boolean containsRegion(int x, int y) {
		return x >= left && x <= right && y >= bottom && y <= top;
	}
}