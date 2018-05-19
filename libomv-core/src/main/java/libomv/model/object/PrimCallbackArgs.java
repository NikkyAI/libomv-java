package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.Primitive;
import libomv.utils.CallbackArgs;

/**
 * Provides data for the <see cref="ObjectManager.ObjectUpdate"/> event
 *
 * <p>
 * The <see cref="ObjectManager.ObjectUpdate"/> event occurs when the simulator
 * sends an <see cref="ObjectUpdatePacket"/> containing a Primitive, Foliage or
 * Attachment data
 * </p>
 * <p>
 * Note 1: The <see cref="ObjectManager.ObjectUpdate"/> event will not be raised
 * when the object is an Avatar
 * </p>
 * <p>
 * Note 2: It is possible for the <see cref="ObjectManager.ObjectUpdate"/> to be
 * raised twice for the same object if for example the primitive moved to a new
 * simulator, then returned to the current simulator or if an Avatar crosses the
 * border into a new simulator and returns to the current simulator
 * </p>
 *
 * <example> The following code example uses the
 * <see cref="PrimCallbackArgs.Prim"/>,
 * <see cref="PrimCallbackArgs.Simulator"/>, and
 * <see cref="PrimCallbackArgs.IsAttachment"/> properties to display new
 * Primitives and Attachments on the <see cref="Console"/> window. <code>
 *     // Subscribe to the event that gives us prim and foliage information
 *     _Client.Objects.OnObjectUpdate.add(new Objects_ObjectUpdate());
 *
 *
 *     private Objects_ObjectUpdate implements CallbackHandler<PrimCallbackArgs>
 *     {
 *         void callback(PrimCallbackArgs e)
 *         {
 *              Console.WriteLine("Primitive %s %s in %s is an attachment %s", e.getPrim().ID, e.getPrim().LocalID, e.getSimulator().Name, e.getIsAttachment());
 *         }
 *     }
 * </code> </example> {@link ObjectManager.OnObjectUpdate}
 * {@link ObjectManager.OnAvatarUpdate} {@link AvatarUpdateCallbackArgs}
 */
public class PrimCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final boolean isNew;
	private final Primitive prim;
	private final short timeDilation;

	/**
	 * Construct a new instance of the PrimEventArgs class
	 *
	 * @param simulator
	 *            The simulator the object originated from
	 * @param prim
	 *            The Primitive
	 * @param timeDilation
	 *            The simulator time dilation
	 * @param isNew
	 *            The prim was not in the dictionary before this update
	 */
	public PrimCallbackArgs(Simulator simulator, Primitive prim, short timeDilation, boolean isNew) {
		this.simulator = simulator;
		this.isNew = isNew;
		this.prim = prim;
		this.timeDilation = timeDilation;
	}

	// Get the simulator the <see cref="Primitive"/> originated from
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the <see cref="Primitive"/> details
	public final Primitive getPrim() {
		return prim;
	}

	// true if the <see cref="Primitive"/> did not exist in the dictionary
	// before this update (always true if object tracking has been disabled)
	public final boolean getIsNew() {
		return isNew;
	}

	// Get the simulator Time Dilation
	public final short getTimeDilation() {
		return timeDilation;
	}

}