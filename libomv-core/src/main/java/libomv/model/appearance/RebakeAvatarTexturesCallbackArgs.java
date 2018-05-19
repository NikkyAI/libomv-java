package libomv.model.appearance;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Triggered when the simulator requests the agent rebake its appearance.
// <see cref="RebakeAvatarRequest"/>
public class RebakeAvatarTexturesCallbackArgs implements CallbackArgs {
	private final UUID m_textureID;

	// The ID of the Texture Layer to bake
	public UUID getTextureID() {
		return m_textureID;
	}

	/**
	 * Triggered when the simulator sends a request for this agent to rebake its
	 * appearance
	 *
	 * @param textureID
	 *            The ID of the Texture Layer to bake
	 */
	public RebakeAvatarTexturesCallbackArgs(UUID textureID) {
		this.m_textureID = textureID;
	}
}