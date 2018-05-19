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
	private final Simulator m_Simulator;
	private final UUID m_ObjectID;
	private final float m_Gain;

	// Simulator where the event originated
	// Tangible_doc_comment_end
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the ID of the Object
	// Tangible_doc_comment_end
	public final UUID getObjectID() {
		return m_ObjectID;
	}

	// Get the volume level
	// Tangible_doc_comment_end
	public final float getGain() {
		return m_Gain;
	}

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
		this.m_Simulator = sim;
		this.m_ObjectID = objectID;
		this.m_Gain = gain;
	}
}