package libomv.model.network;

import libomv.model.Simulator;
import libomv.utils.CallbackArgs;

public class PacketSentCallbackArgs implements CallbackArgs {
	private final byte[] m_Data;
	private final int m_SentBytes;
	private final Simulator m_Simulator;

	public final byte[] getData() {
		return m_Data;
	}

	public final int getSentBytes() {
		return m_SentBytes;
	}

	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public PacketSentCallbackArgs(byte[] data, int bytesSent, Simulator simulator) {
		this.m_Data = data;
		this.m_SentBytes = bytesSent;
		this.m_Simulator = simulator;
	}
}