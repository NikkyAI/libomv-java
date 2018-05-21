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
	public String subject;
	//
	public String message;
	//
	public UUID attachmentID;
	//
	public UUID ownerID;

	public byte[] serializeAttachment() throws IOException {
		if (ownerID.equals(UUID.ZERO) || attachmentID.equals(UUID.ZERO)) {
			return Helpers.EmptyBytes;
		}

		OSDMap att = new OSDMap();
		att.put("item_id", OSD.fromUUID(attachmentID));
		att.put("owner_id", OSD.fromUUID(ownerID));

		return OSDParser.serializeToBytes(att, OSDFormat.Xml, true, Helpers.UTF8_ENCODING);
	}
}