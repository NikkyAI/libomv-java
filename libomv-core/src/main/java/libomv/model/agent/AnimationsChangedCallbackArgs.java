package libomv.model.agent;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AnimationsChangedCallbackArgs implements CallbackArgs {
	private final HashMap<UUID, Integer> animations;

	public AnimationsChangedCallbackArgs(HashMap<UUID, Integer> animations) {
		this.animations = animations;
	}

	public HashMap<UUID, Integer> getAnimations() {
		return animations;
	}

}