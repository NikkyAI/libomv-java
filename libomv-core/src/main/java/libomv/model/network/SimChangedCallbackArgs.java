package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class SimChangedCallbackArgs implements CallbackArgs {
	private final Simulator simulator;

	public Simulator getSimulator() {
		return simulator;
	}

	public SimChangedCallbackArgs(Simulator simulator) {
		this.simulator = simulator;
	}
}