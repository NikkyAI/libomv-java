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
	private final Simulator simulator;
	private final UUID soundID;
	private final UUID ownerID;
	private final UUID objectID;

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
		this.simulator = sim;
		this.soundID = soundID;
		this.ownerID = ownerID;
		this.objectID = objectID;
	}

	// Simulator where the event originated
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the sound asset id
	public final UUID getSoundID() {
		return soundID;
	}

	// Get the ID of the owner
	public final UUID getOwnerID() {
		return ownerID;
	}

	// Get the ID of the Object
	public final UUID getObjectID() {
		return objectID;
	}

}