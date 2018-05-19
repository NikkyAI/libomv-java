package libomv.model.agent;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class RegionCrossedCallbackArgs implements CallbackArgs {
	private final Simulator oldSim, newSim;

	public Simulator getOldSim() {
		return oldSim;
	}

	public Simulator getNewSim() {
		return newSim;
	}

	public RegionCrossedCallbackArgs(Simulator oldSim, Simulator newSim) {
		this.oldSim = oldSim;
		this.newSim = newSim;
	}
}