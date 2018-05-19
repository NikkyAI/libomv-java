package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains the media command for a parcel the agent is currently on
public class ParcelMediaCommandCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final int m_Sequence;
	private final int m_ParcelFlags;
	private final ParcelMediaCommand m_MediaCommand;
	private final float m_Time;

	// Get the simulator the parcel media command was issued in
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public final int getSequence() {
		return m_Sequence;
	}

	public final int getParcelFlags() {
		return m_ParcelFlags;
	}

	// Get the media command that was sent
	public final ParcelMediaCommand getMediaCommand() {
		return m_MediaCommand;
	}

	public final float getTime() {
		return m_Time;
	}

	/**
	 * Construct a new instance of the ParcelMediaCommandCallbackArgs class
	 *
	 * @param simulator
	 *            The simulator the parcel media command was issued in
	 * @param sequence
	 * @param flags
	 * @param command
	 *            The media command that was sent
	 * @param time
	 */
	public ParcelMediaCommandCallbackArgs(Simulator simulator, int sequence, int flags, ParcelMediaCommand command,
			float time) {
		this.m_Simulator = simulator;
		this.m_Sequence = sequence;
		this.m_ParcelFlags = flags;
		this.m_MediaCommand = command;
		this.m_Time = time;
	}
}