package libomv.model;

import java.util.Map;

import libomv.model.Network.OutgoingPacket;
import libomv.packets.Packet;
import libomv.primitives.Avatar;
import libomv.types.UUID;

public interface Simulator extends Runnable {

	/* Access level for a simulator */
	public static enum SimAccess {
		/* Minimum access level, no additional checks */
		Min(0),
		/* Trial accounts allowed */
		Trial(7), // 4 + 2 + 1
		/* PG rating */
		PG(13), // 8 + 4 + 1
		/* Mature rating */
		Mature(21), // 16 + 4 + 1
		/* Adult rating */
		Adult(42), // 32 + 8 + 4
		/* Simulator is offline */
		Down(0xFE),
		/* Simulator does not exist */
		NonExistent(0xFF);

		public static SimAccess setValue(int value) {
			for (SimAccess e : values()) {
				if (e._value == value)
					return e;
			}
			return Min;
		}

		public byte getValue() {
			return _value;
		}

		private byte _value;

		private SimAccess(int value) {
			_value = (byte) value;
		}
	}

	public Avatar findAvatar(UUID uuid);

	public long getHandle();

	public Map<Integer, Parcel> getParcels();

	public void sendPacket(Packet packet) throws Exception;

	public void sendPacketFinal(OutgoingPacket outgoingPacket);
}
