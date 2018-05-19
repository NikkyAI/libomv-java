package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class EventQueueRunningCallbackArgs implements CallbackArgs {
	private final Simulator simulator;

	public EventQueueRunningCallbackArgs(Simulator simulator) {
		this.simulator = simulator;
	}

	public final Simulator getSimulator() {
		return simulator;
	}

}