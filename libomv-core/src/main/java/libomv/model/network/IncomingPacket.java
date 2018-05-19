package libomv.model.network;

import libomv.capabilities.IMessage;
import libomv.model.Simulator;
import libomv.packets.Packet;

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