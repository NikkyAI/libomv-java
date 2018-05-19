package libomv.model.agent;

import java.util.Date;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.utils.Helpers;

// Information about agents display name
public class AgentDisplayName {
	// Agent UUID
	public UUID id;
	// Username
	public String userName;
	// Display name
	public String displayName;
	// First name (legacy)
	public String legacyFirstName;
	// Last name (legacy)
	public String legacyLastName;
	// Full name (legacy)
	public String legacyFullName;
	// Is display name default display name </summary>
	public boolean isDefaultDisplayName;
	// Cache display name until
	public Date nextUpdate;
	// Last updated timestamp
	public Date updated;

	/**
	 * Creates AgentDisplayName object from OSD
	 *
	 * @param data
	 *            Incoming OSD data AgentDisplayName object
	 */
	public AgentDisplayName fromOSD(OSD data) {
		AgentDisplayName ret = new AgentDisplayName();

		OSDMap map = (OSDMap) data;
		ret.id = map.get("id").AsUUID();
		ret.userName = map.get("username").AsString();
		ret.displayName = map.get("display_name").AsString();
		ret.legacyFirstName = map.get("legacy_first_name").AsString();
		ret.legacyLastName = map.get("legacy_last_name").AsString();
		ret.isDefaultDisplayName = map.get("is_display_name_default").AsBoolean();
		ret.nextUpdate = map.get("display_name_next_update").AsDate();
		ret.updated = map.get("last_updated").AsDate();
		return ret;
	}

	public String getLegacyFullName() {
		return String.format("%s %s", legacyFirstName, legacyLastName);
	}

	/**
	 * Return object as OSD map
	 *
	 * @returns OSD containing agent's display name data
	 */
	public OSD toOSD() {
		OSDMap map = new OSDMap();

		map.put("id", OSD.FromUUID(id));
		map.put("username", OSD.FromString(userName));
		map.put("display_name", OSD.FromString(displayName));
		map.put("legacy_first_name", OSD.FromString(legacyFirstName));
		map.put("legacy_last_name", OSD.FromString(legacyLastName));
		map.put("is_display_name_default", OSD.FromBoolean(isDefaultDisplayName));
		map.put("display_name_next_update", OSD.FromDate(nextUpdate));
		map.put("last_updated", OSD.FromDate(updated));

		return map;
	}

	@Override
	public String toString() {
		return Helpers.StructToString(this);
	}
}