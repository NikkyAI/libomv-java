package libomv.model;

import java.util.Map;

import libomv.model.network.OutgoingPacket;
import libomv.packets.Packet;
import libomv.primitives.Avatar;
import libomv.types.UUID;

public interface Simulator extends Runnable {

	public Avatar findAvatar(UUID uuid);

	public long getHandle();

	public Map<Integer, Parcel> getParcels();

	public String getSimName();

	public void sendPacket(Packet packet) throws Exception;

	public void sendPacketFinal(OutgoingPacket outgoingPacket);
}
