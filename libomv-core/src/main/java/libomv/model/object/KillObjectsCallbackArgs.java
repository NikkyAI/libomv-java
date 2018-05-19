package libomv.model.object;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Provides notification when an Avatar, Object or Attachment is DeRezzed or
// moves out of the avatars view for the
// <see cref="ObjectManager.KillObject"/> event
public class KillObjectsCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;

	private final int[] m_ObjectLocalIDs;

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// The LocalID of the object
	public final int[] getObjectLocalIDs() {
		return m_ObjectLocalIDs;
	}

	public KillObjectsCallbackArgs(Simulator simulator, int[] objectIDs) {
		this.m_Simulator = simulator;
		this.m_ObjectLocalIDs = objectIDs;
	}
}