package libomv.model.estate;

import libomv.types.UUID;
import libomv.types.Vector3;

/**
 * Describes tasks returned in LandStatReply
 */
public class EstateTask {
	public Vector3 Position;
	public float Score;
	public float MonoScore;
	public UUID TaskID;
	public int TaskLocalID;
	public String TaskName;
	public String OwnerName;
}
// #endregion