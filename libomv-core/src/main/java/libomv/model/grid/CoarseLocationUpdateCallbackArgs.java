package libomv.model.grid;

import java.util.ArrayList;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class CoarseLocationUpdateCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final ArrayList<UUID> newEntries;
	private final ArrayList<UUID> removedEntries;

	public CoarseLocationUpdateCallbackArgs(Simulator simulator, ArrayList<UUID> newEntries,
			ArrayList<UUID> removedEntries) {
		this.simulator = simulator;
		this.newEntries = newEntries;
		this.removedEntries = removedEntries;
	}

	public final Simulator getSimulator() {
		return simulator;
	}

	public final ArrayList<UUID> getNewEntries() {
		return newEntries;
	}

	public final ArrayList<UUID> getRemovedEntries() {
		return removedEntries;
	}

}