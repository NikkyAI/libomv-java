package libomv.model.grid;

import libomv.types.UUID;

public final class GridLayer {
	public int Bottom;
	public int Left;
	public int Top;
	public int Right;
	public UUID ImageID;

	public boolean ContainsRegion(int x, int y) {
		return (x >= Left && x <= Right && y >= Bottom && y <= Top);
	}
}