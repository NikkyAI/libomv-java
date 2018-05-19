package libomv.model.asset;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.Helpers;

// TODO:FIXME Changing several fields to public, they need getters instead!
public class Transfer {
	public UUID itemID;
	public int size;
	public AssetType assetType;
	public byte[] assetData;

	public UUID transactionID; // protected
	public int transferred; // protected
	public int packetNum; // protected
	public boolean success;
	public long timeSinceLastPacket; // protected
	public HashMap<Integer, DelayedTransfer> delayed; // protected
	public String suffix;

	public Transfer() {
		assetData = Helpers.EmptyBytes;
		delayed = new HashMap<Integer, DelayedTransfer>();
	}
}