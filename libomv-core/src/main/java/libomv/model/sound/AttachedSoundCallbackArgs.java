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
	private final Simulator simulator;
	private final UUID soundID;
	private final UUID ownerID;
	private final UUID objectID;
	private final float gain;
	private final byte flags;

	public AttachedSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, float gain, byte flags) {
		this.simulator = sim;
		this.soundID = soundID;
		this.ownerID = ownerID;
		this.objectID = objectID;
		this.gain = gain;
		this.flags = flags;
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

	// Get the volume level
	public final float getGain() {
		return gain;
	}

	// Get the <see cref="SoundFlags"/>
	public final byte getFlags() {
		return flags;
	}

}