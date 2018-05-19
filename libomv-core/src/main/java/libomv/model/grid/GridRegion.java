package libomv.model.grid;

import libomv.types.UUID;

/* Class for regions on the world map */
public class GridRegion {
	// Sim X position on World Map
	public int x;
	// Sim Y position on World Map
	public int y;
	// Sim Name (NOTE: In lowercase!)
	public String name;
	//
	public byte access;
	// Various flags for the region (presumably things like PG/Mature)
	public int regionFlags;
	// Sim's defined Water Height
	public byte waterHeight;
	//
	public byte agents;
	// UUID of the World Map image
	public UUID mapImageID;
	// Used for teleporting
	public long regionHandle;

	// Constructor
	public GridRegion() {
	}

	public GridRegion(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s (%d/%d), Handle: %d, MapImage: %s, Access: %d, Flags: 0x%8x", name, x, y, regionHandle,
				mapImageID.toString(), access, regionFlags);
	}

	@Override
	public int hashCode() {
		return ((Integer) x).hashCode() ^ ((Integer) y).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GridRegion) {
			return equals((GridRegion) obj);
		}
		return false;
	}

	private boolean equals(GridRegion region) {
		return (this.x == region.x && this.y == region.y);
	}
}