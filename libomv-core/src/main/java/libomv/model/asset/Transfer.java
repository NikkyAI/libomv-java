package libomv.model.asset;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.Helpers;

// TODO:FIXME Changing several fields to public, they need getters instead!
public class Transfer {
	public UUID ItemID;
	public int Size;
	public AssetType AssetType;
	public byte[] AssetData;

	public UUID TransactionID; // protected
	public int Transferred; // protected
	public int PacketNum; // protected
	public boolean Success;
	public long TimeSinceLastPacket; // protected
	public HashMap<Integer, DelayedTransfer> delayed; // protected
	public String suffix;

	public Transfer() {
		AssetData = Helpers.EmptyBytes;
		delayed = new HashMap<Integer, DelayedTransfer>();
	}
}