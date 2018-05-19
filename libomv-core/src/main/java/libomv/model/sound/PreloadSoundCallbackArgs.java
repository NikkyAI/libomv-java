package libomv.model.sound;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Provides data for the <see cref="SoundManager.PreloadSound"/> event
 *
 * The <see cref="SoundManager.PreloadSound"/> event occurs when an attached
 * sound changes its volume level
 */
public class PreloadSoundCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final UUID m_SoundID;
	private final UUID m_OwnerID;
	private final UUID m_ObjectID;

	// Simulator where the event originated
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the sound asset id
	public final UUID getSoundID() {
		return m_SoundID;
	}

	// Get the ID of the owner
	public final UUID getOwnerID() {
		return m_OwnerID;
	}

	// Get the ID of the Object
	public final UUID getObjectID() {
		return m_ObjectID;
	}

	/**
	 * Construct a new instance of the PreloadSoundEventArgs class
	 *
	 * @param sim
	 *            Simulator where the event originated
	 * @param soundID
	 *            The sound asset id
	 * @param ownerID
	 *            The ID of the owner
	 * @param objectID
	 *            The ID of the object
	 */
	public PreloadSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID) {
		this.m_Simulator = sim;
		this.m_SoundID = soundID;
		this.m_OwnerID = ownerID;
		this.m_ObjectID = objectID;
	}
}