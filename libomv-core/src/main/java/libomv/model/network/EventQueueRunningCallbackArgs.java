package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class EventQueueRunningCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;

	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public EventQueueRunningCallbackArgs(Simulator simulator) {
		this.m_Simulator = simulator;
	}
}