package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.Avatar;
import libomv.utils.CallbackArgs;

// Provides updates sit position data
public class AvatarSitChangedCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final Avatar avatar;
	private final int sittingOn;
	private final int oldSeat;

	public AvatarSitChangedCallbackArgs(Simulator simulator, Avatar avatar, int sittingOn, int oldSeat) {
		this.simulator = simulator;
		this.avatar = avatar;
		this.sittingOn = sittingOn;
		this.oldSeat = oldSeat;
	}

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return simulator;
	}

	public final Avatar getAvatar() {
		return avatar;
	}

	public final int getSittingOn() {
		return sittingOn;
	}

	public final int getOldSeat() {
		return oldSeat;
	}

}