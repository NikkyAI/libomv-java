package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.ObjectProperties;

/**
 * Provides additional primitive data, permissions and sale info for the
 * <see cref="ObjectManager.ObjectPropertiesFamily"/> event
 * <p>
 * The <see cref="ObjectManager.ObjectPropertiesFamily"/> event occurs when the
 * simulator sends an <see cref="ObjectPropertiesPacket"/> containing additional
 * details for a Primitive, Foliage data or Attachment. This includes
 * Permissions, Sale info, and other basic details on an object
 * </p>
 * <p>
 * The <see cref="ObjectManager.ObjectProperties"/> event is also raised when a
 * <see cref="ObjectManager.RequestObjectPropertiesFamily"/> request is made,
 * the viewer equivalent is hovering the mouse cursor over an object
 * </p>
 *
 */
public class ObjectPropertiesFamilyCallbackArgs extends ObjectPropertiesCallbackArgs {
	private final ReportType m_Type;

	//
	public final ReportType getType() {
		return m_Type;
	}

	public ObjectPropertiesFamilyCallbackArgs(Simulator simulator, ObjectProperties props, ReportType type) {
		super(simulator, props);
		this.m_Type = type;
	}
}