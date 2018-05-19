package libomv.model.grid;

import java.util.Date;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

/** Map Items */
public class MapItem {
	private UUID ID;
	private String Name;
	private Vector3 GlobalPos;

	/* Represents an agent or group of agents location */
	public int AvatarCount;

	/* For adult and normal land for sale */
	public int Size;
	public int Price;

	public boolean isInfoHub;

	/* For evnts */
	public Date DateTime;

	public final UUID getUUID() {
		return ID;
	}

	public final String getName() {
		return Name;
	}

	/* Get the Local X position of the item */
	public final float getLocalX() {
		return GlobalPos.X % 256;
	}

	/* Get the Local Y position of the item */
	public final float getLocalY() {
		return GlobalPos.Y % 256;
	}

	public final Vector3 getGlobalPosition() {
		return GlobalPos;
	}

	public final void setEvelation(float z) {
		GlobalPos.Z = z;
	}

	/* Get the Handle of the region */
	public final long getRegionHandle() {
		return Helpers.IntsToLong((int) (GlobalPos.X - (GlobalPos.X % 256)),
				(int) (GlobalPos.Y - (GlobalPos.Y % 256)));
	}

	public MapItem(float x, float y, UUID id, String name) {
		GlobalPos = new Vector3(x, y, 40);
		ID = id;
		Name = name;
	}
}