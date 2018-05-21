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
		ret.id = map.get("id").asUUID();
		ret.userName = map.get("username").asString();
		ret.displayName = map.get("display_name").asString();
		ret.legacyFirstName = map.get("legacy_first_name").asString();
		ret.legacyLastName = map.get("legacy_last_name").asString();
		ret.isDefaultDisplayName = map.get("is_display_name_default").asBoolean();
		ret.nextUpdate = map.get("display_name_next_update").asDate();
		ret.updated = map.get("last_updated").asDate();
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

		map.put("id", OSD.fromUUID(id));
		map.put("username", OSD.fromString(userName));
		map.put("display_name", OSD.fromString(displayName));
		map.put("legacy_first_name", OSD.fromString(legacyFirstName));
		map.put("legacy_last_name", OSD.fromString(legacyLastName));
		map.put("is_display_name_default", OSD.fromBoolean(isDefaultDisplayName));
		map.put("display_name_next_update", OSD.fromDate(nextUpdate));
		map.put("last_updated", OSD.fromDate(updated));

		return map;
	}

	@Override
	public String toString() {
		return Helpers.structToString(this);
	}
}