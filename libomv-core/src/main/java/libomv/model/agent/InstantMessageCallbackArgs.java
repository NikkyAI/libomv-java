package libomv.model.agent;

import libomv.model.Simulator;

/* The date received from an ImprovedInstantMessage */
public class InstantMessageCallbackArgs {
	private final InstantMessage m_IM;
	private final Simulator m_Simulator;

	/* Get the InstantMessage object */
	public final InstantMessage getIM() {
		return m_IM;
	}

	/* Get the simulator where the InstantMessage origniated */
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	/**
	 * Construct a new instance of the InstantMessageEventArgs object
	 *
	 * @param im
	 *            the InstantMessage object
	 * @param simulator
	 *            the simulator where the InstantMessage origniated
	 */
	public InstantMessageCallbackArgs(InstantMessage im, Simulator simulator) {
		this.m_IM = im;
		this.m_Simulator = simulator;
	}
}