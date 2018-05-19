package libomv.model;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Vector;

import libomv.capabilities.IMessage;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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

	/** Callback arguments classes */
	public class SimConnectingCallbackArgs implements CallbackArgs {
		private InetSocketAddress endPoint;
		private boolean cancel = false;

		public InetSocketAddress getEndPoint() {
			return endPoint;
		}

		public void setCancel(boolean cancel) {
			this.cancel = cancel;
		}

		public boolean getCancel() {
			return cancel;
		}

		public SimConnectingCallbackArgs(InetSocketAddress endPoint) {
			this.endPoint = endPoint;
		}
	}

	public class SimConnectedCallbackArgs implements CallbackArgs {
		private final Simulator simulator;

		public Simulator getSimulator() {
			return simulator;
		}

		public SimConnectedCallbackArgs(Simulator simulator) {
			this.simulator = simulator;
		}
	}

	// An event for the connection to a simulator other than the currently
	// occupied one disconnecting
	public class SimDisconnectedCallbackArgs implements CallbackArgs {
		private final Simulator simulator;
		private final DisconnectType type;

		public Simulator getSimulator() {
			return simulator;
		}

		public DisconnectType getDisconnectType() {
			return type;
		}

		public SimDisconnectedCallbackArgs(Simulator simulator, DisconnectType type) {
			this.simulator = simulator;
			this.type = type;
		}
	}

	// An event for being logged out either through client request, server
	// forced, or network error
	public class DisconnectedCallbackArgs implements CallbackArgs {
		private final DisconnectType type;
		private final String message;

		public DisconnectType getDisconnectType() {
			return type;
		}

		public String getMessage() {
			return message;
		}

		public DisconnectedCallbackArgs(DisconnectType type, String message) {
			this.type = type;
			this.message = message;
		}
	}

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

	public class EventQueueRunningCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;

		public final Simulator getSimulator() {
			return m_Simulator;
		}

		public EventQueueRunningCallbackArgs(Simulator simulator) {
			this.m_Simulator = simulator;
		}
	}

	/**
	 * An event triggered when the logout is confirmed
	 *
	 * An empty itemIDs list indicates a abortion of the logout procedure after the
	 * logout timout has expired without receiving any confirmation from the server
	 */
	public class LoggedOutCallbackArgs implements CallbackArgs {
		private final Vector<UUID> itemIDs;

		public Vector<UUID> getItemIDs() {
			return itemIDs;
		}

		public LoggedOutCallbackArgs(Vector<UUID> itemIDs) {
			this.itemIDs = itemIDs;
		}
	}

	public class SimChangedCallbackArgs implements CallbackArgs {
		private final Simulator simulator;

		public Simulator getSimulator() {
			return simulator;
		}

		public SimChangedCallbackArgs(Simulator simulator) {
			this.simulator = simulator;
		}
	}

}
