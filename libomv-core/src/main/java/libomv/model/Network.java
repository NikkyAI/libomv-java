package libomv.model;

import java.nio.ByteBuffer;

import libomv.capabilities.IMessage;
import libomv.packets.Packet;
import libomv.packets.PacketType;

public interface Network {

	/** Explains why a simulator or the grid disconnected from us */
	public enum DisconnectType
	{
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
	 * Holds a simulator reference and a decoded packet, these structs are put
	 * in the packet inbox for event handling
	 */
	public final class IncomingPacket
	{
		/** Reference to the simulator that this packet came from */
		public Simulator Simulator;
		/** Packet that needs to be processed */
		public Packet Packet;
		/** CapsMessage that needs to be processed */
		public IMessage Message;

		public IncomingPacket(Simulator simulator, Packet packet)
		{
			Simulator = simulator;
			Packet = packet;
		}

		public IncomingPacket(Simulator simulator, IMessage message)
		{
			Simulator = simulator;
			Message = message;
		}
	}

	/**
	 * Holds a simulator reference and a serialized packet, these structs are
	 * put in the packet outbox for sending
	 */
	public class OutgoingPacket
	{
		/** Reference to the simulator this packet is destined for */
		public final Simulator Simulator;
		/** Packet that needs to be sent */
		public final ByteBuffer Buffer;
		/** PacketType */
		public PacketType Type;
		/** Sequence number of the wrapped packet */
		public int SequenceNumber;
		/** Number of times this packet has been resent */
		public int ResendCount;
		/** Environment.TickCount when this packet was last sent over the wire */
		public long TickCount;

		public OutgoingPacket(Simulator simulator, PacketType type, ByteBuffer buffer)
		{
			Simulator = simulator;
			Type = type;
			Buffer = buffer;
		}
	}

}
