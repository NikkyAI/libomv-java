package libomv.model.group;

import libomv.model.asset.AssetType;
import libomv.types.UUID;

// Struct representing a group notice list entry
public final class GroupNoticesListEntry {
	// Notice ID
	public UUID noticeID;
	// Creation timestamp of notice
	// TODO: ORIGINAL LINE: public uint Timestamp;
	public int timestamp;
	// Agent name who created notice
	public String fromName;
	// Notice subject
	public String subject;
	// Is there an attachment?
	public boolean hasAttachment;
	// Attachment Type
	public AssetType assetType;

}