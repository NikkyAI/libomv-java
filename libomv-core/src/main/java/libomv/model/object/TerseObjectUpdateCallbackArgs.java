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
	private final Simulator m_Simulator;
	private final Primitive m_Prim;
	private final ObjectMovementUpdate m_Update;

	private final short m_TimeDilation;

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the primitive details
	public final Primitive getPrim() {
		return m_Prim;
	}

	public final ObjectMovementUpdate getUpdate() {
		return m_Update;
	}

	public final short getTimeDilation() {
		return m_TimeDilation;
	}

	public TerseObjectUpdateCallbackArgs(Simulator simulator, Primitive prim, ObjectMovementUpdate update,
			short timeDilation) {
		this.m_Simulator = simulator;
		this.m_Prim = prim;
		this.m_Update = update;
		this.m_TimeDilation = timeDilation;
	}
}