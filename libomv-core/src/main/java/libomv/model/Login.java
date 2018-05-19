package libomv.model;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.model.Grid.GridInfo;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public interface Login {

	// #region Enums
	public enum LoginStatus {
		None, Failed, ConnectingToLogin, ReadingResponse, Redirecting, ConnectingToSim, Success;
	}

	// Status of the last application run.
	// Used for error reporting to the grid login service for statistical purposes.
	public enum LastExecStatus {
		// Application exited normally
		Normal,
		// Application froze
		Froze,
		// Application detected error and exited abnormally
		ForcedCrash,
		// Other crash
		OtherCrash,
		// Application froze during logout
		LogoutFroze,
		// Application crashed during logout
		LogoutCrash;
	}
	// #endregion Enums

	// #region Structs
	public final class BuddyListEntry {
		public int buddy_rights_given;
		public String buddy_id;
		public int buddy_rights_has;
	}

	/** The decoded data returned from the login server after a successful login */
	public final class LoginResponseData {
		private static final Logger logger = Logger.getLogger(LoginResponseData.class);
		/**
		 * true, false, indeterminate [XmlRpcMember("login")]
		 */
		public String Login;
		public boolean Success;
		public String Reason;
		/** Login message of the day */
		public String Message;
		public UUID AgentID;
		public UUID SessionID;
		public UUID SecureSessionID;
		public String FirstName;
		public String LastName;
		public String StartLocation;
		/** M or PG, also agent_region_access and agent_access_max */
		public String AgentAccessMax;
		public String AgentAccessPref;
		public Vector3 LookAt;
		public long HomeRegion;
		public Vector3 HomePosition;
		public Vector3 HomeLookAt;
		public int CircuitCode;
		public long Region;
		public Vector2 RegionSize;
		public short SimPort;
		public InetAddress SimIP;
		public String SeedCapability;
		public BuddyListEntry[] BuddyList;
		public int SecondsSinceEpoch;
		public String UDPBlacklist;

		// #region Inventory
		public UUID InventoryRoot;
		public UUID LibraryRoot;
		public InventoryFolder[] InventorySkeleton;
		public InventoryFolder[] LibrarySkeleton;
		public UUID LibraryOwner;
		// #endregion

		public GridInfo Grid;

		// #region Redirection
		public String NextMethod;
		public String NextUrl;
		public String[] NextOptions;
		public int NextDuration;
		// #endregion

		// These aren't currently being utilized by the library
		public boolean AOTransition;
		public String InventoryHost;
		public int MaxAgentGroups;
		public String MapServerUrl;
		public String OpenIDUrl;
		public String AgentAppearanceServiceURL;
		public int COFVersion;
		public String InitialOutfit;
		public boolean FirstLogin;
		public Map<UUID, UUID> Gestures;

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
		public LoginResponseData ParseLoginReply(OSDMap reply) {
			if (reply.containsKey("login")) {
				Login = reply.get("login").AsString();
			}
			Success = reply.get("login").AsBoolean();
			Message = reply.get("message").AsString();
			if (!Success) {
				if (Login != null && Login.equals("indeterminate")) {
					// Parse redirect options
					if (reply.containsKey("next_url"))
						NextUrl = reply.get("next_url").AsString();
					if (reply.containsKey("next_method"))
						NextMethod = reply.get("next_method").AsString();
					if (reply.containsKey("next_duration"))
						NextDuration = reply.get("next_duration").AsUInteger();
					if (reply.containsKey("next_options")) {
						OSD osd = reply.get("next_options");
						if (osd.getType().equals(OSDType.Array))
							NextOptions = ((OSDArray) osd).toArray(NextOptions);
					}
				} else {
					// login failed
					// Reason can be: tos, critical, key, update, optional, presence
					Reason = reply.get("reason").AsString();
				}
				return this;
			}

			// UDP Blacklist
			if (reply.containsKey("udp_blacklist")) {
				UDPBlacklist = reply.get("udp_blacklist").AsString();
			}
			AgentID = reply.get("agent_id").AsUUID();
			SessionID = reply.get("session_id").AsUUID();
			SecureSessionID = reply.get("secure_session_id").AsUUID();
			FirstName = reply.get("first_name").AsString();
			LastName = reply.get("last_name").AsString();

			AgentAccessMax = reply.get("agent_access_max").AsString();
			if (AgentAccessMax.isEmpty()) {
				// we're on an older sim version (probably an opensim)
				AgentAccessMax = reply.get("agent_access").AsString();
			}
			AgentAccessPref = reply.get("agent_region_access").AsString();
			AOTransition = reply.get("ao_transition").AsInteger() == 1;
			StartLocation = reply.get("start_location").AsString();

			CircuitCode = reply.get("circuit_code").AsUInteger();
			Region = Helpers.UIntsToLong(reply.get("region_x").AsUInteger(), reply.get("region_y").AsUInteger());
			SimPort = (short) reply.get("sim_port").AsUInteger();
			SimIP = reply.get("sim_ip").AsInetAddress();

			SeedCapability = reply.get("seed_capability").AsString();
			SecondsSinceEpoch = reply.get("seconds_since_epoch").AsUInteger();

			// Home
			HomeRegion = 0;
			HomePosition = Vector3.Zero;
			HomeLookAt = Vector3.Zero;
			try {
				if (reply.containsKey("home")) {
					ParseHome(reply.get("home").AsString());
				}
				LookAt = ParseVector3("look_at", reply);
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
				BuddyList = new BuddyListEntry[buddyArray.size()];
				for (int i = 0; i < buddyArray.size(); i++) {
					if (buddyArray.get(i).getType().equals(OSDType.Map)) {
						BuddyListEntry bud = new BuddyListEntry();
						OSDMap buddy = (OSDMap) buddyArray.get(i);

						bud.buddy_id = buddy.get("buddy_id").AsString();
						bud.buddy_rights_given = buddy.get("buddy_rights_given").AsUInteger();
						bud.buddy_rights_has = buddy.get("buddy_rights_has").AsUInteger();

						BuddyList[i] = bud;
					}
				}
			}

			InventoryRoot = ParseMappedUUID("inventory-root", "folder_id", reply);
			InventorySkeleton = ParseInventorySkeleton("inventory-skeleton", reply);

			LibraryOwner = ParseMappedUUID("inventory-lib-owner", "agent_id", reply);
			LibraryRoot = ParseMappedUUID("inventory-lib-root", "folder_id", reply);
			LibrarySkeleton = ParseInventorySkeleton("inventory-skel-lib", reply);

			Grid = ParseGridInfo(reply);

			if (reply.containsKey("max-agent-groups")) {
				MaxAgentGroups = reply.get("max-agent-groups").AsUInteger();
			} else {
				// OpenSIM
				if (reply.containsKey("max_groups"))
					MaxAgentGroups = reply.get("max_groups").AsUInteger();
				else
					MaxAgentGroups = -1;
			}

			MapServerUrl = reply.get("map_server_url").AsString();

			if (reply.containsKey("openid_url")) {
				OpenIDUrl = reply.get("openid_url").AsString();
			}

			if (reply.containsKey("agent_appearance_service")) {
				AgentAppearanceServiceURL = reply.get("agent_appearance_service").AsString();
			}

			COFVersion = 0;
			if (reply.containsKey("cof_version")) {
				COFVersion = reply.get("cof_version").AsUInteger();
			}

			InitialOutfit = Helpers.EmptyString;
			OSD osd = reply.get("initial-outfit");
			if (osd != null && osd.getType() == OSDType.Array) {
				OSDArray array = (OSDArray) osd;
				for (int i = 0; i < array.size(); i++) {
					osd = array.get(i);
					if (osd.getType() == OSDType.Map) {
						OSDMap map = (OSDMap) osd;
						InitialOutfit = map.get("folder_name").AsString();
					}
				}
			}

			Gestures = new HashMap<UUID, UUID>();
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
						RefObject<UUID> refItemId = new RefObject<UUID>(itemId);
						if (!UUID.TryParse(map.get("item_id").toString(), refItemId)) {
							continue;
						}

						UUID assetId = null;
						RefObject<UUID> refAssetId = new RefObject<UUID>(assetId);
						if (!UUID.TryParse(map.get("asset_id").toString(), refAssetId)) {
							continue;
						}

						Gestures.put(itemId, assetId);
					}
				}
			}

			FirstLogin = false;
			osd = reply.get("login-flags");
			if (osd != null && osd.getType() == OSDType.Array) {
				OSDArray array = (OSDArray) osd;
				for (int i = 0; i < array.size(); i++) {
					osd = array.get(i);
					if (osd.getType() == OSDType.Map) {
						OSDMap map = (OSDMap) osd;
						FirstLogin = map.get("ever_logged_in").AsString().equalsIgnoreCase("N");
					}
				}
			}
			return this;
		}

		private void ParseHome(String value) throws ParseException, IOException {
			OSD osdHome = OSDParser.deserialize(value, OSDFormat.Notation);
			if (osdHome != null && osdHome.getType().equals(OSDType.Map)) {
				OSDMap home = (OSDMap) osdHome;
				OSD homeRegion = home.get("region_handle");
				if (homeRegion != null && homeRegion.getType().equals(OSDType.Array)) {
					OSDArray homeArray = (OSDArray) homeRegion;
					if (homeArray.size() == 2) {
						HomeRegion = Helpers.UIntsToLong(homeArray.get(0).AsInteger(), homeArray.get(1).AsInteger());
					}
				}
				HomePosition = ParseVector3("position", home);
				HomeLookAt = ParseVector3("look_at", home);
			}
		}

		private GridInfo ParseGridInfo(OSDMap reply) {
			GridInfo grid = new GridInfo();
			boolean update = false;
			if (reply.containsKey("gridname")) {
				grid.gridname = reply.get("gridname").AsString();
				update = true;
			}
			if (reply.containsKey("loginuri")) {
				grid.loginuri = reply.get("loginuri").AsString();
				update = true;
			}
			if (reply.containsKey("welcome")) {
				grid.loginpage = reply.get("welcome").AsString();
				update = true;
			}
			if (reply.containsKey("loginpage")) {
				grid.loginpage = reply.get("loginpage").AsString();
				update = true;
			}
			if (reply.containsKey("economy")) {
				grid.helperuri = reply.get("economy").AsString();
				update = true;
			}
			if (reply.containsKey("helperuri")) {
				grid.helperuri = reply.get("helperuri").AsString();
				update = true;
			}
			if (reply.containsKey("about")) {
				grid.website = reply.get("about").AsString();
				update = true;
			}
			if (reply.containsKey("website")) {
				grid.website = reply.get("website").AsString();
				update = true;
			}
			if (reply.containsKey("help")) {
				grid.support = reply.get("help").AsString();
				update = true;
			}
			if (reply.containsKey("support")) {
				grid.support = reply.get("support").AsString();
				update = true;
			}
			if (reply.containsKey("register")) {
				grid.register = reply.get("register").AsString();
				update = true;
			}
			if (reply.containsKey("account")) {
				grid.register = reply.get("account").AsString();
				update = true;
			}
			if (reply.containsKey("password")) {
				grid.passworduri = reply.get("password").AsString();
				update = true;
			}
			if (reply.containsKey("search")) {
				grid.searchurl = reply.get("search").AsString();
				update = true;
			}
			if (reply.containsKey("currency")) {
				grid.currencySym = reply.get("currency").AsString();
				update = true;
			}
			if (reply.containsKey("real_currency")) {
				grid.realCurrencySym = reply.get("real_currency").AsString();
				update = true;
			}
			if (reply.containsKey("directory_fee")) {
				grid.directoryFee = reply.get("directory_fee").AsString();
				update = true;
			}
			if (update)
				return grid;
			return null;
		}

		private InventoryFolder[] ParseInventorySkeleton(String key, OSDMap reply) {
			UUID ownerID;
			if (key.equals("inventory-skel-lib")) {
				ownerID = LibraryOwner;
			} else {
				ownerID = AgentID;
			}

			OSD skeleton = reply.get(key);
			if (skeleton != null && skeleton.getType().equals(OSDType.Array)) {
				OSDArray array = (OSDArray) skeleton;
				InventoryFolder[] folders = new InventoryFolder[array.size()];
				for (int i = 0; i < array.size(); i++) {
					if (array.get(i).getType().equals(OSDType.Map)) {
						OSDMap map = (OSDMap) array.get(i);
						folders[i] = new InventoryFolder(map.get("folder_id").AsUUID(), map.get("parent_id").AsUUID(),
								ownerID);
						folders[i].name = map.get("name").AsString();
						folders[i].preferredType = FolderType.setValue(map.get("type_default").AsInteger());
						folders[i].version = map.get("version").AsInteger();
					}
				}
				return folders;
			}
			return null;
		}

		private Vector3 ParseVector3(String key, OSDMap reply) throws ParseException, IOException {
			if (reply.containsKey(key)) {
				return reply.get(key).AsVector3();
			}
			return Vector3.Zero;
		}

		private UUID ParseMappedUUID(String key, String key2, OSDMap reply) {
			OSD folderOSD = reply.get(key);
			if (folderOSD != null && folderOSD.getType().equals(OSDType.Array)) {
				OSDArray array = (OSDArray) folderOSD;
				if (array.size() == 1 && array.get(0).getType().equals(OSDType.Map)) {
					OSDMap map = (OSDMap) array.get(0);
					OSD folder = map.get(key2);
					if (folder != null) {
						return folder.AsUUID();
					}
				}
			}
			return UUID.Zero;
		}
	}
	// #endregion Structs

	// #region Callback handlers

	// An event for being logged out either through client request, server
	// forced, or network error
	public class LoginProgressCallbackArgs implements CallbackArgs {
		private final LoginStatus m_Status;
		private final String m_Message;
		private final String m_Reason;
		private LoginResponseData m_Reply;

		public final LoginStatus getStatus() {
			return m_Status;
		}

		public final String getMessage() {
			return m_Message;
		}

		public final String getReason() {
			return m_Reason;
		}

		public LoginResponseData getReply() {
			return m_Reply;
		}

		public LoginProgressCallbackArgs(LoginStatus login, String message, String reason, LoginResponseData reply) {
			this.m_Reply = reply;
			this.m_Status = login;
			this.m_Message = message;
			this.m_Reason = reason;
		}
	}

}
