package libomv.model.estate;

import libomv.types.UUID;
import libomv.types.Vector3;

/**
 * Describes tasks returned in LandStatReply
 */
public class EstateTask {
	public Vector3 position;
	public float score;
	public float monoScore;
	public UUID taskID;
	public int taskLocalID;
	public String taskName;
	public String ownerName;
}
