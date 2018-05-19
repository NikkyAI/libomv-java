package libomv.model.agent;

import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

// Data containing script sensor requests which allow an agent to know the
// specific details
// of a primitive sending script sensor requests
public class ScriptSensorReplyCallbackArgs implements CallbackArgs {
	private final UUID requestorID;
	private final UUID groupID;
	private final String name;
	private final UUID objectID;
	private final UUID ownerID;
	private final Vector3 position;
	private final float range;
	private final Quaternion rotation;
	private final byte type;
	private final Vector3 velocity;

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
		this.requestorID = requestorID;
		this.groupID = groupID;
		this.name = name;
		this.objectID = objectID;
		this.ownerID = ownerID;
		this.position = position;
		this.range = range;
		this.rotation = rotation;
		this.type = type;
		this.velocity = velocity;
	}

	// Get the ID of the primitive sending the sensor
	public UUID getRequestorID() {
		return requestorID;
	}

	// Get the ID of the group associated with the primitive
	public UUID getGroupID() {
		return groupID;
	}

	// Get the name of the primitive sending the sensor
	public String getName() {
		return name;
	}

	// Get the ID of the primitive sending the sensor
	public UUID getObjectID() {
		return objectID;
	}

	// Get the ID of the owner of the primitive sending the sensor
	public UUID getOwnerID() {
		return ownerID;
	}

	// Get the position of the primitive sending the sensor
	public Vector3 getPosition() {
		return position;
	}

	// Get the range the primitive specified to scan
	public float getRange() {
		return range;
	}

	// Get the rotation of the primitive sending the sensor
	public Quaternion getRotation() {
		return rotation;
	}

	// Get the type of sensor the primitive sent
	public byte getType() {
		return type;
	}

	// Get the velocity of the primitive sending the sensor
	public Vector3 getVelocity() {
		return velocity;
	}

}