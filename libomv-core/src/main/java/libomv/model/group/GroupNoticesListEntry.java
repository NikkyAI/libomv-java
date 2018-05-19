package libomv.model.group;

import libomv.model.asset.AssetType;
import libomv.types.UUID;

// Struct representing a group notice list entry
public final class GroupNoticesListEntry {
	// Notice ID
	public UUID NoticeID;
	// Creation timestamp of notice
	// TODO: ORIGINAL LINE: public uint Timestamp;
	public int Timestamp;
	// Agent name who created notice
	public String FromName;
	// Notice subject
	public String Subject;
	// Is there an attachment?
	public boolean HasAttachment;
	// Attachment Type
	public AssetType AssetType;

}