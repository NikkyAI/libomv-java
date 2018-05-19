package libomv.model.grid;

import java.util.ArrayList;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class CoarseLocationUpdateCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final ArrayList<UUID> m_NewEntries;
	private final ArrayList<UUID> m_RemovedEntries;

	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public final ArrayList<UUID> getNewEntries() {
		return m_NewEntries;
	}

	public final ArrayList<UUID> getRemovedEntries() {
		return m_RemovedEntries;
	}

	public CoarseLocationUpdateCallbackArgs(Simulator simulator, ArrayList<UUID> newEntries,
			ArrayList<UUID> removedEntries) {
		this.m_Simulator = simulator;
		this.m_NewEntries = newEntries;
		this.m_RemovedEntries = removedEntries;
	}
}