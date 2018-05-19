package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.Primitive;
import libomv.utils.CallbackArgs;

/**
 * Provides primitive data containing updated location, velocity, rotation,
 * textures for the <see cref="ObjectManager.TerseObjectUpdate"/> event
 * <p>
 * The <see cref="ObjectManager.TerseObjectUpdate"/> event occurs when the
 * simulator sends updated location, velocity, rotation, etc
 * </p>
 */
public class TerseObjectUpdateCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final Primitive prim;
	private final ObjectMovementUpdate update;
	private final short timeDilation;

	public TerseObjectUpdateCallbackArgs(Simulator simulator, Primitive prim, ObjectMovementUpdate update,
			short timeDilation) {
		this.simulator = simulator;
		this.prim = prim;
		this.update = update;
		this.timeDilation = timeDilation;
	}

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the primitive details
	public final Primitive getPrim() {
		return prim;
	}

	public final ObjectMovementUpdate getUpdate() {
		return update;
	}

	public final short getTimeDilation() {
		return timeDilation;
	}

}