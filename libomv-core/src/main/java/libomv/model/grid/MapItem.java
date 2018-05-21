package libomv.model.grid;

import java.util.Date;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

/** Map Items */
public class MapItem {
	private UUID id;
	private String name;
	private Vector3 globalPos;

	/* Represents an agent or group of agents location */
	public int avatarCount;

	/* For adult and normal land for sale */
	public int size;
	public int price;

	public boolean isInfoHub;

	/* For evnts */
	public Date dateTime;

	public MapItem(float x, float y, UUID id, String name) {
		globalPos = new Vector3(x, y, 40);
		this.id = id;
		this.name = name;
	}

	public final UUID getUUID() {
		return id;
	}

	public final String getName() {
		return name;
	}

	/* Get the Local X position of the item */
	public final float getLocalX() {
		return globalPos.x % 256;
	}

	/* Get the Local Y position of the item */
	public final float getLocalY() {
		return globalPos.y % 256;
	}

	public final Vector3 getGlobalPosition() {
		return globalPos;
	}

	public final void setEvelation(float z) {
		globalPos.z = z;
	}

	/* Get the Handle of the region */
	public final long getRegionHandle() {
		return Helpers.intsToLong((int) (globalPos.x - (globalPos.x % 256)), (int) (globalPos.y - (globalPos.y % 256)));
	}

}