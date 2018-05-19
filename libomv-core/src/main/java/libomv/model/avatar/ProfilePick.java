package libomv.model.avatar;

import libomv.types.UUID;
import libomv.types.Vector3d;

/**
 * Holds group information on an individual profile pick
 */
public class ProfilePick {
	public UUID pickID;
	public UUID creatorID;
	public boolean topPick;
	public UUID parcelID;
	public String name;
	public String desc;
	public UUID snapshotID;
	public String user;
	public String originalName;
	public String simName;
	public Vector3d posGlobal;
	public int sortOrder;
	public boolean enabled;
}