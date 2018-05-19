package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.PhysicsProperties;
import libomv.utils.CallbackArgs;

public class PhysicsPropertiesCallbackArgs implements CallbackArgs {
	// Simulator where the message originated
	public Simulator Simulator;
	// Updated physical properties
	public PhysicsProperties PhysicsProperties;

	/**
	 * Constructor
	 *
	 * @param sim
	 *            Simulator where the message originated
	 * @param props
	 *            Updated physical properties
	 */
	public PhysicsPropertiesCallbackArgs(Simulator sim, PhysicsProperties props) {
		Simulator = sim;
		PhysicsProperties = props;
	}
}