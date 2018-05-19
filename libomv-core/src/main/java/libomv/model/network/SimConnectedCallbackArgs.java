package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class SimConnectedCallbackArgs implements CallbackArgs {
	private final Simulator simulator;

	public Simulator getSimulator() {
		return simulator;
	}

	public SimConnectedCallbackArgs(Simulator simulator) {
		this.simulator = simulator;
	}
}