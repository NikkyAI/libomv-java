package libomv.model.agent;

import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

// Data containing script sensor requests which allow an agent to know the
// specific details
// of a primitive sending script sensor requests
public class ScriptSensorReplyCallbackArgs implements CallbackArgs {
	private final UUID m_RequestorID;
	private final UUID m_GroupID;
	private final String m_Name;
	private final UUID m_ObjectID;
	private final UUID m_OwnerID;
	private final Vector3 m_Position;
	private final float m_Range;
	private final Quaternion m_Rotation;
	private final byte m_Type;
	private final Vector3 m_Velocity;

	// Get the ID of the primitive sending the sensor
	public UUID getRequestorID() {
		return m_RequestorID;
	}

	// Get the ID of the group associated with the primitive
	public UUID getGroupID() {
		return m_GroupID;
	}

	// Get the name of the primitive sending the sensor
	public String getName() {
		return m_Name;
	}

	// Get the ID of the primitive sending the sensor
	public UUID getObjectID() {
		return m_ObjectID;
	}

	// Get the ID of the owner of the primitive sending the sensor
	public UUID getOwnerID() {
		return m_OwnerID;
	}

	// Get the position of the primitive sending the sensor
	public Vector3 getPosition() {
		return m_Position;
	}

	// Get the range the primitive specified to scan
	public float getRange() {
		return m_Range;
	}

	// Get the rotation of the primitive sending the sensor
	public Quaternion getRotation() {
		return m_Rotation;
	}

	// Get the type of sensor the primitive sent
	public byte getType() {
		return m_Type;
	}

	// Get the velocity of the primitive sending the sensor
	public Vector3 getVelocity() {
		return m_Velocity;
	}

	/**
	 * Construct a new instance of the ScriptSensorReplyEventArgs
	 *
	 * @param requestorID
	 *            The ID of the primitive sending the sensor
	 * @param groupID
	 *            The ID of the group associated with the primitive
	 * @param name
	 *            The name of the primitive sending the sensor
	 * @param objectID
	 *            The ID of the primitive sending the sensor
	 * @param ownerID
	 *            The ID of the owner of the primitive sending the sensor
	 * @param position
	 *            The position of the primitive sending the sensor
	 * @param range
	 *            The range the primitive specified to scan
	 * @param rotation
	 *            The rotation of the primitive sending the sensor
	 * @param type
	 *            The type of sensor the primitive sent
	 * @param velocity
	 *            The velocity of the primitive sending the sensor
	 */
	public ScriptSensorReplyCallbackArgs(UUID requestorID, UUID groupID, String name, UUID objectID, UUID ownerID,
			Vector3 position, float range, Quaternion rotation, byte type, Vector3 velocity) {
		this.m_RequestorID = requestorID;
		this.m_GroupID = groupID;
		this.m_Name = name;
		this.m_ObjectID = objectID;
		this.m_OwnerID = ownerID;
		this.m_Position = position;
		this.m_Range = range;
		this.m_Rotation = rotation;
		this.m_Type = type;
		this.m_Velocity = velocity;
	}
}