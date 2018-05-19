package libomv.model.object;

import libomv.primitives.TextureEntry;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.types.Vector4;

/**
 * Contains the variables sent in an object update packet for objects. Used to
 * track position and movement of prims and avatars
 */
public final class ObjectMovementUpdate {
	public boolean Avatar;
	public Vector4 CollisionPlane;
	public byte State;
	public int LocalID; // uint
	public Vector3 Position;
	public Vector3 Velocity;
	public Vector3 Acceleration;
	public Quaternion Rotation;
	public Vector3 AngularVelocity;
	public TextureEntry Textures;
}