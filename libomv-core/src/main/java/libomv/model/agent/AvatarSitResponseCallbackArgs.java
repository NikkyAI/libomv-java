package libomv.model.agent;

import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

// Contains the response data returned from the simulator in response to a <see
// cref="RequestSit"/>
public class AvatarSitResponseCallbackArgs implements CallbackArgs {
	private final UUID objectID;
	private final boolean autopilot;
	private final Vector3 cameraAtOffset;
	private final Vector3 cameraEyeOffset;
	private final boolean forceMouselook;
	private final Vector3 sitPosition;
	private final Quaternion sitRotation;

	// Construct a new instance of the AvatarSitResponseEventArgs object
	public AvatarSitResponseCallbackArgs(UUID objectID, boolean autoPilot, Vector3 cameraAtOffset,
			Vector3 cameraEyeOffset, boolean forceMouselook, Vector3 sitPosition, Quaternion sitRotation) {
		this.objectID = objectID;
		this.autopilot = autoPilot;
		this.cameraAtOffset = cameraAtOffset;
		this.cameraEyeOffset = cameraEyeOffset;
		this.forceMouselook = forceMouselook;
		this.sitPosition = sitPosition;
		this.sitRotation = sitRotation;
	}

	/// <summary>Get the ID of the primitive the agent will be sitting on</summary>
	public UUID getObjectID() {
		return objectID;
	}

	/// <summary>True if the simulator Autopilot functions were involved</summary>
	public boolean getAutopilot() {
		return autopilot;
	}

	/// <summary>Get the camera offset of the agent when seated</summary>
	public Vector3 getCameraAtOffset() {
		return cameraAtOffset;
	}

	/// <summary>Get the camera eye offset of the agent when seated</summary>
	public Vector3 getCameraEyeOffset() {
		return cameraEyeOffset;
	}

	/// <summary>True of the agent will be in mouselook mode when seated</summary>
	public boolean getForceMouselook() {
		return forceMouselook;
	}

	/// <summary>Get the position of the agent when seated</summary>
	public Vector3 getSitPosition() {
		return sitPosition;
	}

	/// <summary>Get the rotation of the agent when seated</summary>
	public Quaternion getSitRotation() {
		return sitRotation;
	}

}