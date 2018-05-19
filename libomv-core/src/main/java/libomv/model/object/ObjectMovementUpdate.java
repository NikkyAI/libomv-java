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
	public boolean avatar;
	public Vector4 collisionPlane;
	public byte state;
	public int localID; // uint
	public Vector3 position;
	public Vector3 velocity;
	public Vector3 acceleration;
	public Quaternion rotation;
	public Vector3 angularVelocity;
	public TextureEntry textures;
}