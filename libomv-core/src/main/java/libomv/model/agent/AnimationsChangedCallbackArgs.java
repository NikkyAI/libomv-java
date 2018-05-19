package libomv.model.agent;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AnimationsChangedCallbackArgs implements CallbackArgs {
	private final HashMap<UUID, Integer> m_Animations;

	public HashMap<UUID, Integer> getAnimations() {
		return m_Animations;
	}

	public AnimationsChangedCallbackArgs(HashMap<UUID, Integer> animations) {
		m_Animations = animations;
	}
}