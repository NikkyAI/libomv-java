package libomv.model.agent;

import java.util.Map;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AnimationsChangedCallbackArgs implements CallbackArgs {
	private final Map<UUID, Integer> animations;

	public AnimationsChangedCallbackArgs(Map<UUID, Integer> animations) {
		this.animations = animations;
	}

	public Map<UUID, Integer> getAnimations() {
		return animations;
	}

}