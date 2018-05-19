package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.ObjectProperties;
import libomv.utils.CallbackArgs;

/**
 * Provides additional primitive data for the
 * <see cref="ObjectManager.ObjectProperties"/> event
 * <p>
 * The <see cref="ObjectManager.ObjectProperties"/> event occurs when the
 * simulator sends an <see cref="ObjectPropertiesPacket"/> containing additional
 * details for a Primitive, Foliage data or Attachment data
 * </p>
 * <p>
 * The <see cref="ObjectManager.ObjectProperties"/> event is also raised when a
 * <see cref="ObjectManager.SelectObject"/> request is made.
 * </p>
 *
 * <example> The following code example uses the
 * <see cref="PrimEventArgs.Prim"/>, <see cref="PrimEventArgs.Simulator"/> and
 * <see cref="ObjectPropertiesEventArgs.Properties"/> properties to display new
 * attachments and send a request for additional properties containing the name
 * of the attachment then display it on the <see cref="Console"/> window. <code>
 *     // Subscribe to the event that provides additional primitive details
 *     _Client.Objects.ObjectProperties += Objects_ObjectProperties;
 *
 *     // handle the properties data that arrives
 *     private void Objects_ObjectProperties(object sender, ObjectPropertiesEventArgs e)
 *     {
 *         Console.WriteLine("Primitive Properties: %s, Name is %s", e.Properties.ObjectID, e.Properties.Name);
 *     }
 * </code> </example>
 */
public class ObjectPropertiesCallbackArgs implements CallbackArgs {
	protected final Simulator m_Simulator;
	protected final ObjectProperties m_Properties;

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the primitive properties
	public final ObjectProperties getProperties() {
		return m_Properties;
	}

	/**
	 * Construct a new instance of the ObjectPropertiesEventArgs class
	 *
	 * @param simulator
	 *            The simulator the object is located
	 * @param props
	 *            The primitive Properties
	 */
	public ObjectPropertiesCallbackArgs(Simulator simulator, ObjectProperties props) {
		this.m_Simulator = simulator;
		this.m_Properties = props;
	}
}