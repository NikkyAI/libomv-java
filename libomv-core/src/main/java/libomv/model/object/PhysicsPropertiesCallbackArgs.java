package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.PhysicsProperties;
import libomv.utils.CallbackArgs;

//TODO:FIXME Why are these public?
public class PhysicsPropertiesCallbackArgs implements CallbackArgs {
	// Simulator where the message originated
	public Simulator simulator;
	// Updated physical properties
	public PhysicsProperties physicsProperties;

	/**
	 * Constructor
	 *
	 * @param sim
	 *            Simulator where the message originated
	 * @param props
	 *            Updated physical properties
	 */
	public PhysicsPropertiesCallbackArgs(Simulator sim, PhysicsProperties props) {
		this.simulator = sim;
		this.physicsProperties = props;
	}
}