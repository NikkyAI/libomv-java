package libomv.model.sound;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Provides data for the <see cref="SoundManager.AttachedSound"/> event
 *
 * The <see cref="SoundManager.AttachedSound"/> event occurs when the simulator
 * sends the sound data which emits from an agents attachment
 */
public class AttachedSoundCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final UUID m_SoundID;
	private final UUID m_OwnerID;
	private final UUID m_ObjectID;
	private final float m_Gain;
	private final byte m_Flags;

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

	// Get the volume level
	public final float getGain() {
		return m_Gain;
	}

	// Get the <see cref="SoundFlags"/>
	public final byte getFlags() {
		return m_Flags;
	}

	public AttachedSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, float gain,
			byte flags) {
		this.m_Simulator = sim;
		this.m_SoundID = soundID;
		this.m_OwnerID = ownerID;
		this.m_ObjectID = objectID;
		this.m_Gain = gain;
		this.m_Flags = flags;
	}
}