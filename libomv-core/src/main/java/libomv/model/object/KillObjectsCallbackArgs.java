package libomv.model.object;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Provides notification when an Avatar, Object or Attachment is DeRezzed or
// moves out of the avatars view for the
// <see cref="ObjectManager.KillObject"/> event
public class KillObjectsCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final int[] objectLocalIDs;

	public KillObjectsCallbackArgs(Simulator simulator, int[] objectIDs) {
		this.simulator = simulator;
		this.objectLocalIDs = objectIDs;
	}

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return simulator;
	}

	// The LocalID of the object
	public final int[] getObjectLocalIDs() {
		return objectLocalIDs;
	}

}