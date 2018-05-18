package libomv.model;

import java.nio.ByteBuffer;

import libomv.capabilities.IMessage;
import libomv.packets.Packet;
import libomv.packets.PacketType;

public interface Network {

	/** Explains why a simulator or the grid disconnected from us */
	public enum DisconnectType {
		/** The client requested the logout or simulator disconnect */
		ClientInitiated,
		/** The server notified us that it is disconnecting */
		ServerInitiated,
		/** Either a socket was closed or network traffic timed out */
		NetworkTimeout,
		/** The last active simulator shut down */
		SimShutdown
	}

	/**
	 * Holds a simulator reference and a decoded packet, these structs are put in
	 * the packet inbox for event handling
	 */
	public final class IncomingPacket {
		/** Reference to the simulator that this packet came from */
		public Simulator simulator;
		/** Packet that needs to be processed */
		public Packet packet;
		/** CapsMessage that needs to be processed */
		public IMessage message;

		public IncomingPacket(Simulator simulator, Packet packet) {
			this.simulator = simulator;
			this.packet = packet;
		}

		public IncomingPacket(Simulator simulator, IMessage message) {
			this.simulator = simulator;
			this.message = message;
		}
	}

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
		public PacketType Type;
		/** Sequence number of the wrapped packet */
		public int sequenceNumber;
		/** Number of times this packet has been resent */
		public int resendCount;
		/** Environment.TickCount when this packet was last sent over the wire */
		public long tickCount;

		public OutgoingPacket(Simulator simulator, PacketType type, ByteBuffer buffer) {
			this.simulator = simulator;
			this.Type = type;
			this.buffer = buffer;
		}
	}

}
