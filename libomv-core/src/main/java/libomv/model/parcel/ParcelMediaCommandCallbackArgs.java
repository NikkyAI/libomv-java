package libomv.model.parcel;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

// Contains the media command for a parcel the agent is currently on
public class ParcelMediaCommandCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final int sequence;
	private final int parcelFlags;
	private final ParcelMediaCommand mediaCommand;
	private final float time;

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
		this.simulator = simulator;
		this.sequence = sequence;
		this.parcelFlags = flags;
		this.mediaCommand = command;
		this.time = time;
	}

	// Get the simulator the parcel media command was issued in
	public final Simulator getSimulator() {
		return simulator;
	}

	public final int getSequence() {
		return sequence;
	}

	public final int getParcelFlags() {
		return parcelFlags;
	}

	// Get the media command that was sent
	public final ParcelMediaCommand getMediaCommand() {
		return mediaCommand;
	}

	public final float getTime() {
		return time;
	}

}