package libomv.model.group;

import java.io.IOException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.types.UUID;
import libomv.utils.Helpers;

// Struct representing a group notice
public final class GroupNotice {
	//
	public String Subject;
	//
	public String Message;
	//
	public UUID AttachmentID;
	//
	public UUID OwnerID;

	public byte[] SerializeAttachment() throws IOException {
		if (OwnerID.equals(UUID.Zero) || AttachmentID.equals(UUID.Zero)) {
			return Helpers.EmptyBytes;
		}

		OSDMap att = new OSDMap();
		att.put("item_id", OSD.FromUUID(AttachmentID));
		att.put("owner_id", OSD.FromUUID(OwnerID));

		return OSDParser.serializeToBytes(att, OSDFormat.Xml, true, Helpers.UTF8_ENCODING);
	}
}