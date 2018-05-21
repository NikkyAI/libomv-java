package libomv.model.login;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.model.grid.GridInfo;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

/** The decoded data returned from the login server after a successful login */
public final class LoginResponseData {
	private static final Logger logger = Logger.getLogger(LoginResponseData.class);
	/**
	 * true, false, indeterminate [XmlRpcMember("login")]
	 */
	public String login;
	public boolean success;
	public String reason;
	/** Login message of the day */
	public String message;
	public UUID agentID;
	public UUID sessionID;
	public UUID secureSessionID;
	public String firstName;
	public String lastName;
	public String startLocation;
	/** M or PG, also agent_region_access and agent_access_max */
	public String agentAccessMax;
	public String agentAccessPref;
	public Vector3 lookAt;
	public long homeRegion;
	public Vector3 homePosition;
	public Vector3 homeLookAt;
	public int circuitCode;
	public long region;
	public Vector2 regionSize;
	public short simPort;
	public InetAddress simIP;
	public String seedCapability;
	public BuddyListEntry[] buddyList;
	public int secondsSinceEpoch;
	public String udpBlacklist;

	// #region Inventory
	public UUID inventoryRoot;
	public UUID libraryRoot;
	public InventoryFolder[] inventorySkeleton;
	public InventoryFolder[] librarySkeleton;
	public UUID libraryOwner;
	// #endregion

	public GridInfo grid;

	// #region Redirection
	public String nextMethod;
	public String nextUrl;
	public String[] nextOptions;
	public int nextDuration;
	// #endregion

	// These aren't currently being utilized by the library
	public boolean aoTransition;
	public String inventoryHost;
	public int maxAgentGroups;
	public String mapServerUrl;
	public String openIDUrl;
	public String agentAppearanceServiceURL;
	public int cofVersion;
	public String initialOutfit;
	public boolean firstLogin;
	public Map<UUID, UUID> gestures;

	// Unhandled:
	// reply.gestures
	// reply.event_categories
	// reply.classified_categories
	// reply.event_notifications
	// reply.ui_config
	// reply.login_flags
	// reply.global_textures

	/**
	 * Parse LLSD Login Reply Data
	 *
	 * @param reply
	 *            An {@link OSDMap} containing the login response data. XML-RPC
	 *            logins do not require this as XML-RPC.NET automatically populates
	 *            the struct properly using attributes
	 * @return this object pointer
	 */
	public LoginResponseData parseLoginReply(OSDMap reply) {
		if (reply.containsKey("login")) {
			login = reply.get("login").asString();
		}
		success = reply.get("login").asBoolean();
		message = reply.get("message").asString();
		if (!success) {
			if (login != null && login.equals("indeterminate")) {
				// Parse redirect options
				if (reply.containsKey("next_url"))
					nextUrl = reply.get("next_url").asString();
				if (reply.containsKey("next_method"))
					nextMethod = reply.get("next_method").asString();
				if (reply.containsKey("next_duration"))
					nextDuration = reply.get("next_duration").asUInteger();
				if (reply.containsKey("next_options")) {
					OSD osd = reply.get("next_options");
					if (osd.getType().equals(OSDType.Array))
						nextOptions = ((OSDArray) osd).toArray(nextOptions);
				}
			} else {
				// login failed
				// Reason can be: tos, critical, key, update, optional, presence
				reason = reply.get("reason").asString();
			}
			return this;
		}

		// UDP Blacklist
		if (reply.containsKey("udp_blacklist")) {
			udpBlacklist = reply.get("udp_blacklist").asString();
		}
		agentID = reply.get("agent_id").asUUID();
		sessionID = reply.get("session_id").asUUID();
		secureSessionID = reply.get("secure_session_id").asUUID();
		firstName = reply.get("first_name").asString();
		lastName = reply.get("last_name").asString();

		agentAccessMax = reply.get("agent_access_max").asString();
		if (agentAccessMax.isEmpty()) {
			// we're on an older sim version (probably an opensim)
			agentAccessMax = reply.get("agent_access").asString();
		}
		agentAccessPref = reply.get("agent_region_access").asString();
		aoTransition = reply.get("ao_transition").asInteger() == 1;
		startLocation = reply.get("start_location").asString();

		circuitCode = reply.get("circuit_code").asUInteger();
		region = Helpers.UIntsToLong(reply.get("region_x").asUInteger(), reply.get("region_y").asUInteger());
		simPort = (short) reply.get("sim_port").asUInteger();
		simIP = reply.get("sim_ip").asInetAddress();

		seedCapability = reply.get("seed_capability").asString();
		secondsSinceEpoch = reply.get("seconds_since_epoch").asUInteger();

		// Home
		homeRegion = 0;
		homePosition = Vector3.Zero;
		homeLookAt = Vector3.Zero;
		try {
			if (reply.containsKey("home")) {
				parseHome(reply.get("home").asString());
			}
			lookAt = parseVector3("look_at", reply);
		} catch (Exception ex) {
			// TODO:FIXME
			// This contains a log statement, but shouldn't this throw the exception?
			// Also, shouldn't this be catching the proper exceptions instead of this
			// catch-all?
			logger.warn("Login server returned (some) invalid data: " + ex.getMessage(), ex);
		}

		// TODO: add options parsing

		// Buddy list
		OSD buddyLLSD = reply.get("buddy-list");
		if (buddyLLSD != null && buddyLLSD.getType().equals(OSDType.Array)) {
			OSDArray buddyArray = (OSDArray) buddyLLSD;
			buddyList = new BuddyListEntry[buddyArray.size()];
			for (int i = 0; i < buddyArray.size(); i++) {
				if (buddyArray.get(i).getType().equals(OSDType.Map)) {
					BuddyListEntry bud = new BuddyListEntry();
					OSDMap buddy = (OSDMap) buddyArray.get(i);

					bud.buddyID = buddy.get("buddy_id").asString();
					bud.buddyRightsGiven = buddy.get("buddy_rights_given").asUInteger();
					bud.buddyRightsHas = buddy.get("buddy_rights_has").asUInteger();

					buddyList[i] = bud;
				}
			}
		}

		inventoryRoot = parseMappedUUID("inventory-root", "folder_id", reply);
		inventorySkeleton = parseInventorySkeleton("inventory-skeleton", reply);

		libraryOwner = parseMappedUUID("inventory-lib-owner", "agent_id", reply);
		libraryRoot = parseMappedUUID("inventory-lib-root", "folder_id", reply);
		librarySkeleton = parseInventorySkeleton("inventory-skel-lib", reply);

		grid = parseGridInfo(reply);

		if (reply.containsKey("max-agent-groups")) {
			maxAgentGroups = reply.get("max-agent-groups").asUInteger();
		} else {
			// OpenSIM
			if (reply.containsKey("max_groups"))
				maxAgentGroups = reply.get("max_groups").asUInteger();
			else
				maxAgentGroups = -1;
		}

		mapServerUrl = reply.get("map_server_url").asString();

		if (reply.containsKey("openid_url")) {
			openIDUrl = reply.get("openid_url").asString();
		}

		if (reply.containsKey("agent_appearance_service")) {
			agentAppearanceServiceURL = reply.get("agent_appearance_service").asString();
		}

		cofVersion = 0;
		if (reply.containsKey("cof_version")) {
			cofVersion = reply.get("cof_version").asUInteger();
		}

		initialOutfit = Helpers.EmptyString;
		OSD osd = reply.get("initial-outfit");
		if (osd != null && osd.getType() == OSDType.Array) {
			OSDArray array = (OSDArray) osd;
			for (int i = 0; i < array.size(); i++) {
				osd = array.get(i);
				if (osd.getType() == OSDType.Map) {
					OSDMap map = (OSDMap) osd;
					initialOutfit = map.get("folder_name").asString();
				}
			}
		}

		gestures = new HashMap<>();
		osd = reply.get("gestures");
		if (osd != null && osd.getType() == OSDType.Array) {
			OSDArray array = (OSDArray) osd;
			for (int i = 0; i < array.size(); i++) {
				osd = array.get(i);
				if (osd.getType() == OSDType.Map) {
					OSDMap map = (OSDMap) array.get(i);
					if (!map.containsKey("item_id") || !map.containsKey("asset_id")) {
						continue;
					}

					UUID itemId = null;
					RefObject<UUID> refItemId = new RefObject<>(itemId);
					if (!UUID.TryParse(map.get("item_id").toString(), refItemId)) {
						continue;
					}

					UUID assetId = null;
					RefObject<UUID> refAssetId = new RefObject<>(assetId);
					if (!UUID.TryParse(map.get("asset_id").toString(), refAssetId)) {
						continue;
					}

					gestures.put(itemId, assetId);
				}
			}
		}

		firstLogin = false;
		osd = reply.get("login-flags");
		if (osd != null && osd.getType() == OSDType.Array) {
			OSDArray array = (OSDArray) osd;
			for (int i = 0; i < array.size(); i++) {
				osd = array.get(i);
				if (osd.getType() == OSDType.Map) {
					OSDMap map = (OSDMap) osd;
					firstLogin = map.get("ever_logged_in").asString().equalsIgnoreCase("N");
				}
			}
		}
		return this;
	}

	private void parseHome(String value) throws ParseException, IOException {
		OSD osdHome = OSDParser.deserialize(value, OSDFormat.Notation);
		if (osdHome != null && osdHome.getType().equals(OSDType.Map)) {
			OSDMap home = (OSDMap) osdHome;
			OSD homeRegionOSD = home.get("region_handle");
			if (homeRegionOSD != null && homeRegionOSD.getType().equals(OSDType.Array)) {
				OSDArray homeArray = (OSDArray) homeRegionOSD;
				if (homeArray.size() == 2) {
					homeRegion = Helpers.UIntsToLong(homeArray.get(0).asInteger(), homeArray.get(1).asInteger());
				}
			}
			homePosition = parseVector3("position", home);
			homeLookAt = parseVector3("look_at", home);
		}
	}

	private GridInfo parseGridInfo(OSDMap reply) {
		GridInfo result = new GridInfo();
		boolean update = false;
		if (reply.containsKey("gridname")) {
			result.gridname = reply.get("gridname").asString();
			update = true;
		}
		if (reply.containsKey("loginuri")) {
			result.loginuri = reply.get("loginuri").asString();
			update = true;
		}
		if (reply.containsKey("welcome")) {
			result.loginpage = reply.get("welcome").asString();
			update = true;
		}
		if (reply.containsKey("loginpage")) {
			result.loginpage = reply.get("loginpage").asString();
			update = true;
		}
		if (reply.containsKey("economy")) {
			result.helperuri = reply.get("economy").asString();
			update = true;
		}
		if (reply.containsKey("helperuri")) {
			result.helperuri = reply.get("helperuri").asString();
			update = true;
		}
		if (reply.containsKey("about")) {
			result.website = reply.get("about").asString();
			update = true;
		}
		if (reply.containsKey("website")) {
			result.website = reply.get("website").asString();
			update = true;
		}
		if (reply.containsKey("help")) {
			result.support = reply.get("help").asString();
			update = true;
		}
		if (reply.containsKey("support")) {
			result.support = reply.get("support").asString();
			update = true;
		}
		if (reply.containsKey("register")) {
			result.register = reply.get("register").asString();
			update = true;
		}
		if (reply.containsKey("account")) {
			result.register = reply.get("account").asString();
			update = true;
		}
		if (reply.containsKey("password")) {
			result.passworduri = reply.get("password").asString();
			update = true;
		}
		if (reply.containsKey("search")) {
			result.searchurl = reply.get("search").asString();
			update = true;
		}
		if (reply.containsKey("currency")) {
			result.currencySym = reply.get("currency").asString();
			update = true;
		}
		if (reply.containsKey("real_currency")) {
			result.realCurrencySym = reply.get("real_currency").asString();
			update = true;
		}
		if (reply.containsKey("directory_fee")) {
			result.directoryFee = reply.get("directory_fee").asString();
			update = true;
		}
		if (update)
			return result;
		return null;
	}

	private InventoryFolder[] parseInventorySkeleton(String key, OSDMap reply) {
		UUID ownerID;
		if (key.equals("inventory-skel-lib")) {
			ownerID = libraryOwner;
		} else {
			ownerID = agentID;
		}

		OSD skeleton = reply.get(key);
		if (skeleton != null && skeleton.getType().equals(OSDType.Array)) {
			OSDArray array = (OSDArray) skeleton;
			InventoryFolder[] folders = new InventoryFolder[array.size()];
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i).getType().equals(OSDType.Map)) {
					OSDMap map = (OSDMap) array.get(i);
					folders[i] = new InventoryFolder(map.get("folder_id").asUUID(), map.get("parent_id").asUUID(),
							ownerID);
					folders[i].name = map.get("name").asString();
					folders[i].preferredType = FolderType.setValue(map.get("type_default").asInteger());
					folders[i].version = map.get("version").asInteger();
				}
			}
			return folders;
		}
		return null;
	}

	private Vector3 parseVector3(String key, OSDMap reply) throws ParseException, IOException {
		if (reply.containsKey(key)) {
			return reply.get(key).asVector3();
		}
		return Vector3.Zero;
	}

	private UUID parseMappedUUID(String key, String key2, OSDMap reply) {
		OSD folderOSD = reply.get(key);
		if (folderOSD != null && folderOSD.getType().equals(OSDType.Array)) {
			OSDArray array = (OSDArray) folderOSD;
			if (array.size() == 1 && array.get(0).getType().equals(OSDType.Map)) {
				OSDMap map = (OSDMap) array.get(0);
				OSD folder = map.get(key2);
				if (folder != null) {
					return folder.asUUID();
				}
			}
		}
		return UUID.Zero;
	}
}
