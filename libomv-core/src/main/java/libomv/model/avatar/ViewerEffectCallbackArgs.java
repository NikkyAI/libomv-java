package libomv.model.avatar;

import libomv.model.Simulator;
import libomv.model.agent.EffectType;
import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.CallbackArgs;

public class ViewerEffectCallbackArgs implements CallbackArgs {
	private EffectType type;
	private Simulator simulator;
	private UUID sourceAvatar;
	private UUID targetObject;
	private Vector3d targetPos;
	private byte target;
	private float duration;
	private UUID dataID;

	public EffectType getType() {
		return type;
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public UUID getSourceAvatar() {
		return sourceAvatar;
	}

	public UUID getTargetObject() {
		return targetObject;
	}

	public Vector3d getTargetPos() {
		return targetPos;
	}

	public byte getTarget() {
		return target;
	}

	public float getDuration() {
		return duration;
	}

	public UUID getDataID() {
		return dataID;
	}

	public ViewerEffectCallbackArgs(EffectType type, Simulator simulator, UUID sourceAvatar, UUID targetObject,
			Vector3d targetPos, byte target, float duration, UUID dataID) {
		this.type = type;
		this.simulator = simulator;
		this.sourceAvatar = sourceAvatar;
		this.targetObject = targetObject;
		this.targetPos = targetPos;
		this.target = target;
		this.duration = duration;
		this.dataID = dataID;
	}
}