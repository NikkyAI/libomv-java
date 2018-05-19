package libomv.model;

import java.util.ArrayList;
import java.util.HashMap;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;

public interface Estate {

	// #region Enums
	// Used in the ReportType field of a LandStatRequest
	public enum LandStatReportType {
		TopScripts, TopColliders;

		public static LandStatReportType setValue(int value) {
			return values()[value];
		}
	}

	// Used by EstateOwnerMessage packets
	public enum EstateAccessDelta {
		None(0), BanUser(64), BanUserAllEstates(66), UnbanUser(128), UnbanUserAllEstates(130), AddManager(
				256), AddManagerAllEstates(257), RemoveManager(512), RemoveManagerAllEstates(513), AddUserAsAllowed(
						4), AddAllowedAllEstates(6), RemoveUserAsAllowed(8), RemoveUserAllowedAllEstates(
								10), AddGroupAsAllowed(16), AddGroupAllowedAllEstates(
										18), RemoveGroupAsAllowed(32), RemoveGroupAllowedAllEstates(34);

		public static EstateAccessDelta setValue(int value) {
			for (EstateAccessDelta e : values()) {
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private EstateAccessDelta(int value) {
			val = value;
		}
	}

	// Used by EstateOwnerMessage packets
	public enum EstateAccessReplyDelta {
		None(0), AllowedUsers(17), AllowedGroups(18), EstateBans(20), EstateManagers(24);

		public static EstateAccessReplyDelta setValue(int value) {
			for (EstateAccessReplyDelta e : values()) {
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private EstateAccessReplyDelta(int value) {
			val = value;
		}
	}

	public enum EstateReturnFlags {
		/// <summary>No flags set</summary>
		None(2),
		/// <summary>Only return targets scripted objects</summary>
		ReturnScripted(6),
		/// <summary>Only return targets objects if on others land</summary>
		ReturnOnOthersLand(3),
		/// <summary>Returns target's scripted objects and objects on other
		/// parcels</summary>
		ReturnScriptedAndOnOthers(7);

		public static EstateReturnFlags setValue(int value) {
			for (EstateReturnFlags e : values()) {
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private EstateReturnFlags(int value) {
			val = value;
		}
	}
	// #endregion

	// #region Structs

	/**
	 * Ground texture settings for each corner of the region TODO: maybe move this
	 * class to the Simulator object and implement it there too
	 */
	public class GroundTextureSettings {
		public UUID Low;
		public UUID MidLow;
		public UUID MidHigh;
		public UUID High;
	}

	/**
	 * Used by GroundTextureHeightSettings
	 */
	public class GroundTextureHeight {
		public float Low;
		public float High;
	}

	/**
	 * The high and low texture thresholds for each corner of the sim
	 */
	public class GroundTextureHeightSettings {
		public GroundTextureHeight SW;
		public GroundTextureHeight NW;
		public GroundTextureHeight SE;
		public GroundTextureHeight NE;
	}

	/**
	 * Describes tasks returned in LandStatReply
	 */
	public class EstateTask {
		public Vector3 Position;
		public float Score;
		public float MonoScore;
		public UUID TaskID;
		public int TaskLocalID;
		public String TaskName;
		public String OwnerName;
	}
	// #endregion

	// #region EstateTools CallbackArgs Classes
	/**
	 * Raised on LandStatReply when the report type is for "top colliders"
	 */
	public class TopCollidersReplyCallbackArgs implements CallbackArgs {
		private final int m_objectCount;
		private final HashMap<UUID, EstateTask> m_Tasks;

		// The number of returned items in LandStatReply
		public int getObjectCount() {
			return m_objectCount;
		}

		// A Dictionary of Object UUIDs to tasks returned in LandStatReply
		public HashMap<UUID, EstateTask> getTasks() {
			return m_Tasks;
		}

		/**
		 * Construct a new instance of the TopCollidersReplyEventArgs class
		 *
		 * @param objectCount
		 *            The number of returned items in LandStatReply
		 * @param tasks
		 *            Dictionary of Object UUIDs to tasks returned in LandStatReply
		 */
		public TopCollidersReplyCallbackArgs(int objectCount, HashMap<UUID, EstateTask> tasks) {
			this.m_objectCount = objectCount;
			this.m_Tasks = tasks;
		}
	}

	/**
	 * Raised on LandStatReply when the report type is for "top Scripts"
	 */
	public class TopScriptsReplyCallbackArgs implements CallbackArgs {
		private final int m_objectCount;
		private final HashMap<UUID, EstateTask> m_Tasks;

		// The number of scripts returned in LandStatReply
		public int getObjectCount() {
			return m_objectCount;
		}

		// A Dictionary of Object UUIDs to tasks returned in LandStatReply
		public HashMap<UUID, EstateTask> getTasks() {
			return m_Tasks;
		}

		/**
		 * Construct a new instance of the TopScriptsReplyEventArgs class
		 *
		 * @param objectCount
		 *            The number of returned items in LandStatReply
		 * @param tasks
		 *            Dictionary of Object UUIDs to tasks returned in LandStatReply
		 */
		public TopScriptsReplyCallbackArgs(int objectCount, HashMap<UUID, EstateTask> tasks) {
			this.m_objectCount = objectCount;
			this.m_Tasks = tasks;
		}
	}

	/**
	 * Returned, along with other info, upon a successful .RequestInfo()
	 */
	public class EstateBansReplyCallbackArgs implements CallbackArgs {
		private final int m_estateID;
		private final int m_count;
		private final ArrayList<UUID> m_banned;

		// The identifier of the estate
		public int getEstateID() {
			return m_estateID;
		}

		// The number of returned items
		public int getCount() {
			return m_count;
		}

		// List of UUIDs of Banned Users
		public ArrayList<UUID> getBanned() {
			return m_banned;
		}

		/**
		 * Construct a new instance of the EstateBansReplyEventArgs class
		 *
		 * @param estateID
		 *            The estate's identifier on the grid
		 * @param count
		 *            The number of returned items in LandStatReply
		 * @param banned
		 *            User UUIDs banned
		 */
		public EstateBansReplyCallbackArgs(int estateID, int count, ArrayList<UUID> banned) {
			this.m_estateID = estateID;
			this.m_count = count;
			this.m_banned = banned;
		}
	}

	/**
	 * Returned, along with other info, upon a successful .RequestInfo()
	 */
	public class EstateUsersReplyCallbackArgs implements CallbackArgs {
		private final int m_estateID;
		private final int m_count;
		private final ArrayList<UUID> m_allowedUsers;

		// The identifier of the estate
		public int getEstateID() {
			return m_estateID;
		}

		// The number of returned items
		public int getCount() {
			return m_count;
		}

		// List of UUIDs of Allowed Users
		public ArrayList<UUID> getAllowedUsers() {
			return m_allowedUsers;
		}

		/**
		 * Construct a new instance of the EstateUsersReplyEventArgs class
		 *
		 * @param estateID
		 *            The estate's identifier on the grid
		 * @param count
		 *            The number of returned users in LandStatReply
		 * @param allowedUsers
		 *            Allowed users UUIDs
		 */
		public EstateUsersReplyCallbackArgs(int estateID, int count, ArrayList<UUID> allowedUsers) {
			this.m_estateID = estateID;
			this.m_count = count;
			this.m_allowedUsers = allowedUsers;
		}
	}

	/**
	 * Returned, along with other info, upon a successful .RequestInfo()
	 */
	public class EstateGroupsReplyCallbackArgs implements CallbackArgs {
		private final int m_estateID;
		private final int m_count;
		private final ArrayList<UUID> m_allowedGroups;

		// The identifier of the estate
		public int getEstateID() {
			return m_estateID;
		}

		// The number of returned items
		public int getCount() {
			return m_count;
		}

		// List of UUIDs of Allowed Groups
		public ArrayList<UUID> getAllowedGroups() {
			return m_allowedGroups;
		}

		/**
		 * Construct a new instance of the EstateGroupsReplyEventArgs class
		 *
		 * @param estateID
		 *            The estate's identifier on the grid
		 * @param count
		 *            The number of returned groups in LandStatReply
		 * @param allowedGroups
		 *            Allowed Groups UUIDs
		 */
		public EstateGroupsReplyCallbackArgs(int estateID, int count, ArrayList<UUID> allowedGroups) {
			this.m_estateID = estateID;
			this.m_count = count;
			this.m_allowedGroups = allowedGroups;
		}
	}

	/**
	 * Returned, along with other info, upon a successful .RequestInfo()</summary>
	 */
	public class EstateManagersReplyCallbackArgs implements CallbackArgs {
		private final int m_estateID;
		private final int m_count;
		private final ArrayList<UUID> m_Managers;

		// The identifier of the estate
		public int getEstateID() {
			return m_estateID;
		}

		// The number of returned items
		public int getCount() {
			return m_count;
		}

		// List of UUIDs of the Estate's Managers
		public ArrayList<UUID> getManagers() {
			return m_Managers;
		}

		/**
		 * Construct a new instance of the EstateManagersReplyEventArgs class
		 *
		 * @param estateID
		 *            The estate's identifier on the grid
		 * @param count
		 *            The number of returned managers in LandStatReply
		 * @param managers
		 *            Managers UUIDs
		 */
		public EstateManagersReplyCallbackArgs(int estateID, int count, ArrayList<UUID> managers) {
			this.m_estateID = estateID;
			this.m_count = count;
			this.m_Managers = managers;
		}
	}

	/**
	 * Returned, along with other info, upon a successful .RequestInfo()</summary>
	 */
	public class EstateCovenantReplyCallbackArgs implements CallbackArgs {
		private final UUID m_covenantID;
		private final long m_timestamp;
		private final String m_estateName;
		private final UUID m_estateOwnerID;

		// The Covenant
		public UUID getCovenantID() {
			return m_covenantID;
		}

		// The timestamp
		public long getTimestamp() {
			return m_timestamp;
		}

		// The Estate name
		public String getEstateName() {
			return m_estateName;
		}

		// The Estate Owner's ID (can be a GroupID)
		public UUID getEstateOwnerID() {
			return m_estateOwnerID;
		}

		/**
		 * Construct a new instance of the EstateCovenantReplyEventArgs class
		 *
		 * @param covenantID
		 *            The Covenant ID
		 * @param timestamp
		 *            The timestamp
		 * @param estateName
		 *            The estate's name
		 * @param estateOwnerID
		 *            The Estate Owner's ID (can be a GroupID)
		 */
		public EstateCovenantReplyCallbackArgs(UUID covenantID, long timestamp, String estateName, UUID estateOwnerID) {
			this.m_covenantID = covenantID;
			this.m_timestamp = timestamp;
			this.m_estateName = estateName;
			this.m_estateOwnerID = estateOwnerID;
		}
	}

	/**
	 * Returned, along with other info, upon a successful .RequestInfo()
	 */
	public class EstateUpdateInfoReplyCallbackArgs implements CallbackArgs {
		private final int m_estateID;
		private final boolean m_denyNoPaymentInfo;
		private final String m_estateName;
		private final UUID m_estateOwner;

		// The estate's name
		public String getEstateName() {
			return m_estateName;
		}

		// The Estate Owner's ID (can be a GroupID)
		public UUID getEstateOwner() {
			return m_estateOwner;
		}

		// The identifier of the estate on the grid
		public int getEstateID() {
			return m_estateID;
		}

		public boolean getDenyNoPaymentInfo() {
			return m_denyNoPaymentInfo;
		}

		/**
		 * Construct a new instance of the EstateUpdateInfoReplyEventArgs class
		 *
		 * @param estateName
		 *            The estate's name
		 * @param estateOwner
		 *            The Estate Owners ID (can be a GroupID)
		 * @param estateID
		 *            The estate's identifier on the grid
		 * @param denyNoPaymentInfo
		 */
		public EstateUpdateInfoReplyCallbackArgs(String estateName, UUID estateOwner, int estateID,
				boolean denyNoPaymentInfo) {
			this.m_estateName = estateName;
			this.m_estateOwner = estateOwner;
			this.m_estateID = estateID;
			this.m_denyNoPaymentInfo = denyNoPaymentInfo;

		}
	}

	// #endregion
}
