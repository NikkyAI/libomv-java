package libomv.capabilities;

import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.CapsEventType;

public interface IMessage
{
	public CapsEventType getType();

	public OSDMap Serialize();

	public void Deserialize(OSDMap map);
}
