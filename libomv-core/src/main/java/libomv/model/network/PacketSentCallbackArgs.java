package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class PacketSentCallbackArgs implements CallbackArgs {
	private final byte[] data;
	private final int sentBytes;
	private final Simulator simulator;

	public PacketSentCallbackArgs(byte[] data, int bytesSent, Simulator simulator) {
		this.data = data;
		this.sentBytes = bytesSent;
		this.simulator = simulator;
	}

	public final byte[] getData() {
		return data;
	}

	public final int getSentBytes() {
		return sentBytes;
	}

	public final Simulator getSimulator() {
		return simulator;
	}

}