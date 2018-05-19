package libomv.model.agent;

import libomv.model.Simulator;

/* The date received from an ImprovedInstantMessage */
public class InstantMessageCallbackArgs {
	private final InstantMessage im;
	private final Simulator simulator;

	/**
	 * Construct a new instance of the InstantMessageEventArgs object
	 *
	 * @param im
	 *            the InstantMessage object
	 * @param simulator
	 *            the simulator where the InstantMessage origniated
	 */
	public InstantMessageCallbackArgs(InstantMessage im, Simulator simulator) {
		this.im = im;
		this.simulator = simulator;
	}

	/* Get the InstantMessage object */
	public final InstantMessage getIM() {
		return im;
	}

	/* Get the simulator where the InstantMessage origniated */
	public final Simulator getSimulator() {
		return simulator;
	}

}