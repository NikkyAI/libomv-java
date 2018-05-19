package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.ObjectProperties;
import libomv.primitives.Primitive;

/**
 * Provides additional primitive data for the
 * <see cref="ObjectManager.ObjectPropertiesUpdated"/> event
 * <p>
 * The <see cref="ObjectManager.ObjectPropertiesUpdated"/> event occurs when the
 * simulator sends an <see cref="ObjectPropertiesPacket"/> containing additional
 * details for a Primitive or Foliage data that is currently being tracked in
 * the <see cref="Simulator.ObjectsPrimitives"/> dictionary
 * </p>
 * <p>
 * The <see cref="ObjectManager.ObjectPropertiesUpdated"/> event is also raised
 * when a <see cref="ObjectManager.SelectObject"/> request is made and
 * <see cref="Settings.OBJECT_TRACKING"/> is enabled
 * </p>
 */
public class ObjectPropertiesUpdatedCallbackArgs extends ObjectPropertiesCallbackArgs {
	private final Primitive prim;

	/**
	 * Construct a new instance of the ObjectPropertiesUpdatedEvenrArgs class
	 *
	 * @param simulator
	 *            The simulator the object is located
	 * @param prim
	 *            The Primitive
	 * @param props
	 *            The primitive Properties
	 */
	public ObjectPropertiesUpdatedCallbackArgs(Simulator simulator, Primitive prim, ObjectProperties props) {
		super(simulator, props);
		this.prim = prim;
	}

	// Get the primitive details
	public final Primitive getPrim() {
		return prim;
	}

}