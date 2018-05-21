package libomv.model.grid;

import java.util.List;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class CoarseLocationUpdateCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final List<UUID> newEntries;
	private final List<UUID> removedEntries;

	public CoarseLocationUpdateCallbackArgs(Simulator simulator, List<UUID> newEntries, List<UUID> removedEntries) {
		this.simulator = simulator;
		this.newEntries = newEntries;
		this.removedEntries = removedEntries;
	}

	public final Simulator getSimulator() {
		return simulator;
	}

	public final List<UUID> getNewEntries() {
		return newEntries;
	}

	public final List<UUID> getRemovedEntries() {
		return removedEntries;
	}

}