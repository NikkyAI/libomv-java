package libomv.model.sound;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Provides data for the <see cref="SoundManager.AttachedSoundGainChange"/>
 * event
 *
 * The <see cref="SoundManager.AttachedSoundGainChange"/> event occurs when an
 * attached sound changes its volume level
 */
public class AttachedSoundGainChangeCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final UUID objectID;
	private final float gain;

	/**
	 * Construct a new instance of the AttachedSoundGainChangedEventArgs class
	 *
	 * @param sim
	 *            Simulator where the event originated
	 * @param objectID
	 *            The ID of the Object
	 * @param gain
	 *            The new volume level
	 */
	public AttachedSoundGainChangeCallbackArgs(Simulator sim, UUID objectID, float gain) {
		this.simulator = sim;
		this.objectID = objectID;
		this.gain = gain;
	}

	// Simulator where the event originated
	// Tangible_doc_comment_end
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the ID of the Object
	// Tangible_doc_comment_end
	public final UUID getObjectID() {
		return objectID;
	}

	// Get the volume level
	// Tangible_doc_comment_end
	public final float getGain() {
		return gain;
	}

}