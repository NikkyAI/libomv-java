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
	private final Simulator m_Simulator;
	private final UUID m_SoundID;
	private final UUID m_OwnerID;
	private final UUID m_ObjectID;
	private final UUID m_ParentID;
	private final float m_Gain;
	private final long m_RegionHandle;
	private final Vector3 m_Position;

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

	// Get the ID of the objects parent
	public final UUID getParentID() {
		return m_ParentID;
	}

	// Get the volume level
	public final float getGain() {
		return m_Gain;
	}

	// Get the regionhandle
	public final long getRegionHandle() {
		return m_RegionHandle;
	}

	// Get the source position
	public final Vector3 getPosition() {
		return m_Position;
	}

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
	public SoundTriggerCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, UUID parentID,
			float gain, long regionHandle, Vector3 position) {
		this.m_Simulator = sim;
		this.m_SoundID = soundID;
		this.m_OwnerID = ownerID;
		this.m_ObjectID = objectID;
		this.m_ParentID = parentID;
		this.m_Gain = gain;
		this.m_RegionHandle = regionHandle;
		this.m_Position = position;
	}
}