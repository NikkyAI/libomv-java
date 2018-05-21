package libomv.model.network;

import java.nio.ByteBuffer;

import libomv.model.Simulator;
import libomv.packets.PacketType;

/**
 * Holds a simulator reference and a serialized packet, these structs are put in
 * the packet outbox for sending
 */
public class OutgoingPacket {
	/** Reference to the simulator this packet is destined for */
	public final Simulator simulator;
	/** Packet that needs to be sent */
	public final ByteBuffer buffer;
	/** PacketType */
	public PacketType type;
	/** Sequence number of the wrapped packet */
	public int sequenceNumber;
	/** Number of times this packet has been resent */
	public int resendCount;
	/** Environment.TickCount when this packet was last sent over the wire */
	public long tickCount;

	public OutgoingPacket(Simulator simulator, PacketType type, ByteBuffer buffer) {
		this.simulator = simulator;
		this.type = type;
		this.buffer = buffer;
	}
}