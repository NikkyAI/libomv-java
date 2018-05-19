package libomv.model.avatar;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarAnimationCallbackArgs implements CallbackArgs {
	private UUID agentID;
	private ArrayList<Animation> animations;

	public AvatarAnimationCallbackArgs(UUID agentID, ArrayList<Animation> animations) {
		this.agentID = agentID;
		this.animations = animations;
	}

	public UUID getAgentID() {
		return agentID;
	}

	public ArrayList<Animation> getAnimations() {
		return animations;
	}

}