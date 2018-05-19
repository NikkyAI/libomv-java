package libomv.model.parcel;

import java.util.Date;

import libomv.types.UUID;

public final class ParcelAccessEntry {
	// Agents {@link T:OpenMetaverse.UUID}
	public UUID agentID;
	//
	public Date time;
	// Flags for specific entry in white/black lists
	public byte flags;
}