package libomv.model.avatar;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarAnimationCallbackArgs implements CallbackArgs {
	private UUID agentID;
	private List<Animation> animations;

	public AvatarAnimationCallbackArgs(UUID agentID, List<Animation> animations) {
		this.agentID = agentID;
		this.animations = animations;
	}

	public UUID getAgentID() {
		return agentID;
	}

	public List<Animation> getAnimations() {
		return animations;
	}

}