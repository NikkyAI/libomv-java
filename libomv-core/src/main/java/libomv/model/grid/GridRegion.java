package libomv.model.grid;

import libomv.types.UUID;

/* Class for regions on the world map */
public class GridRegion {
	// Sim X position on World Map
	public int X;
	// Sim Y position on World Map
	public int Y;
	// Sim Name (NOTE: In lowercase!)
	public String Name;
	//
	public byte Access;
	// Various flags for the region (presumably things like PG/Mature)
	public int RegionFlags;
	// Sim's defined Water Height
	public byte WaterHeight;
	//
	public byte Agents;
	// UUID of the World Map image
	public UUID MapImageID;
	// Used for teleporting
	public long RegionHandle;

	// Constructor
	public GridRegion() {
	}

	public GridRegion(String name) {
		Name = name;
	}

	@Override
	public String toString() {
		return String.format("%s (%d/%d), Handle: %d, MapImage: %s, Access: %d, Flags: 0x%8x", Name, X, Y,
				RegionHandle, MapImageID.toString(), Access, RegionFlags);
	}

	@Override
	public int hashCode() {
		return ((Integer) X).hashCode() ^ ((Integer) Y).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GridRegion) {
			return equals((GridRegion) obj);
		}
		return false;
	}

	private boolean equals(GridRegion region) {
		return (this.X == region.X && this.Y == region.Y);
	}
}