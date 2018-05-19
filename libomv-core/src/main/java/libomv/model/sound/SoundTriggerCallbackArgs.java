package libomv.model.sound;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

/**
 * Provides data for the <see cref="SoundManager.SoundTrigger"/> event
 * <p>
 * The <see cref="SoundManager.SoundTrigger"/> event occurs when the simulator
 * forwards a request made by yourself or another agent to play either an asset
 * sound or a built in sound
 * </p>
 *
 * <p>
 * Requests to play sounds where the <see cref="SoundTriggerEventArgs.SoundID"/>
 * is not one of the built-in <see cref="Sounds"/> will require sending a
 * request to download the sound asset before it can be played
 * </p>
 */
public class SoundTriggerCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final UUID soundID;
	private final UUID ownerID;
	private final UUID objectID;
	private final UUID parentID;
	private final float gain;
	private final long regionHandle;
	private final Vector3 position;

	/**
	 * Construct a new instance of the SoundTriggerEventArgs class
	 *
	 * @param sim
	 *            Simulator where the event originated
	 * @param soundID
	 *            The sound asset id
	 * @param ownerID
	 *            The ID of the owner
	 * @param objectID
	 *            The ID of the object
	 * @param parentID
	 *            The ID of the objects parent
	 * @param gain
	 *            The volume level
	 * @param regionHandle
	 *            The regionhandle
	 * @param position
	 *            The source position
	 */
	public SoundTriggerCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, UUID parentID, float gain,
			long regionHandle, Vector3 position) {
		this.simulator = sim;
		this.soundID = soundID;
		this.ownerID = ownerID;
		this.objectID = objectID;
		this.parentID = parentID;
		this.gain = gain;
		this.regionHandle = regionHandle;
		this.position = position;
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

	// Get the ID of the objects parent
	public final UUID getParentID() {
		return parentID;
	}

	// Get the volume level
	public final float getGain() {
		return gain;
	}

	// Get the regionhandle
	public final long getRegionHandle() {
		return regionHandle;
	}

	// Get the source position
	public final Vector3 getPosition() {
		return position;
	}

}