package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.Avatar;
import libomv.utils.CallbackArgs;

// Provides updates sit position data
public class AvatarSitChangedCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final Avatar m_Avatar;

	private final int m_SittingOn;

	private final int m_OldSeat;

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public final Avatar getAvatar() {
		return m_Avatar;
	}

	public final int getSittingOn() {
		return m_SittingOn;
	}

	public final int getOldSeat() {
		return m_OldSeat;
	}

	public AvatarSitChangedCallbackArgs(Simulator simulator, Avatar avatar, int sittingOn, int oldSeat) {
		this.m_Simulator = simulator;
		this.m_Avatar = avatar;
		this.m_SittingOn = sittingOn;
		this.m_OldSeat = oldSeat;
	}
}