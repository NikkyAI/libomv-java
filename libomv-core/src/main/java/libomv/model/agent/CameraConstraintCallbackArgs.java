package libomv.model.agent;

import libomv.types.Vector4;
import libomv.utils.CallbackArgs;

public class CameraConstraintCallbackArgs implements CallbackArgs {
	private final Vector4 constraints;

	public CameraConstraintCallbackArgs(Vector4 constraints) {
		this.constraints = constraints;
	}

	public Vector4 getConstraints() {
		return constraints;
	}

}