package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// An event for the connection to a simulator other than the currently
// occupied one disconnecting
public class SimDisconnectedCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final DisconnectType type;

	public Simulator getSimulator() {
		return simulator;
	}

	public DisconnectType getDisconnectType() {
		return type;
	}

	public SimDisconnectedCallbackArgs(Simulator simulator, DisconnectType type) {
		this.simulator = simulator;
		this.type = type;
	}
}