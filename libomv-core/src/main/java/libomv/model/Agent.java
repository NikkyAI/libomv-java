package libomv.model;

import java.util.Date;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.utils.Helpers;

public interface Agent {

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

		public String getLegacyFullName() {
			return String.format("%s %s", legacyFirstName, legacyLastName);
		}

		/**
		 * Creates AgentDisplayName object from OSD
		 * 
		 * @param data
		 *            Incoming OSD data AgentDisplayName object
		 */
		public AgentDisplayName FromOSD(OSD data) {
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

		/**
		 * Return object as OSD map
		 * 
		 * @returns OSD containing agent's display name data
		 */
		public OSD GetOSD() {
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

	/** Special commands used in Instant Messages */
	public enum InstantMessageDialog {
		/*
		 * Indicates a regular IM from another agent, ID is meaningless, nothing in the
		 * binary bucket.
		 */
		MessageFromAgent, // 0
		/* Simple notification box with an OK button */
		MessageBox, // 1
		/* Used to show a countdown notification with an OK button, deprecated now */
		Deprecated_MessageBoxCountdown, // 2
		/*
		 * You've been invited to join a group. ID is the group id. The binary bucket
		 * contains a null terminated string representation of the officer/member status
		 * and join cost for the invitee. The format is 1 byte for officer/member (O for
		 * officer, M for member), and as many bytes as necessary for cost.
		 */
		GroupInvitation, // 3
		/*
		 * Inventory offer, ID is the transaction id, binary bucket is a list of
		 * inventory uuid and type.
		 */
		InventoryOffered, // 4
		/* Accepted inventory offer */
		InventoryAccepted, // 5
		/* Declined inventory offer */
		InventoryDeclined, // 6
		/*
		 * Group vote, Name is name of person who called vote, ID is vote ID used for
		 * internal tracking
		 */
		GroupVote, // 7
		/* A message to everyone in the agent's group, no longer used */
		Deprecated_GroupMessage, // 8
		/*
		 * An object is offering its inventory, ID is the transaction id, Binary bucket
		 * is a (mostly) complete packed inventory item
		 */
		TaskInventoryOffered, // 9
		/* Accept an inventory offer from an object */
		TaskInventoryAccepted, // 10
		/* Decline an inventory offer from an object */
		TaskInventoryDeclined, // 11
		/* Unknown */
		NewUserDefault, // 12
		/* Start a session, or add users to a session */
		SessionAdd, // 13
		/* Start a session, but don't prune offline users */
		SessionOfflineAdd, // 14
		/* Start a session with your group */
		SessionGroupStart, // 15
		/* Start a session without a calling card (finder or objects) */
		SessionCardlessStart, // 16
		/* Send a message to a session */
		SessionSend, // 17
		/* Leave a session */
		SessionDrop, // 18
		/* Indicates that the IM is from an object */
		MessageFromObject, // 19
		/* Sent an IM to a busy user, this is the auto response */
		BusyAutoResponse, // 20
		/* Shows the message in the console and chat history */
		ConsoleAndChatHistory, // 21
		/* Send a teleport lure */
		RequestTeleport, // 22
		/* Response sent to the agent which inititiated a teleport invitation */
		AcceptTeleport, // 23
		/* Response sent to the agent which inititiated a teleport invitation */
		DenyTeleport, // 24
		/* Only useful if you have Linden permissions */
		GodLikeRequestTeleport, // 25
		/* Request a teleport lure */
		RequestLure, // 26
		/* Notification of a new group election, this is depreciated */
		@Deprecated
		Deprecated_GroupElection, // 27
		/*
		 * IM to tell the user to go to an URL. Put a text message in the message field,
		 * and put the url with a trailing \0 in the binary bucket.
		 */
		GotoUrl, // 28
		/* IM for help */
		Session911Start, // 29
		/*
		 * IM sent automatically on call for help, sends a lure to each Helper reached
		 */
		Lure911, // 30
		/* Like an IM but won't go to email */
		FromTaskAsAlert, // 31
		/* IM from a group officer to all group members */
		GroupNotice, // 32
		/* Unknown */
		GroupNoticeInventoryAccepted, // 33
		/* Unknown */
		GroupNoticeInventoryDeclined, // 34
		/* Accept a group invitation */
		GroupInvitationAccept, // 35
		/* Decline a group invitation */
		GroupInvitationDecline, // 36
		/* Unknown */
		GroupNoticeRequested, // 37
		/* An avatar is offering you friendship */
		FriendshipOffered, // 38
		/* An avatar has accepted your friendship offer */
		FriendshipAccepted, // 39
		/* An avatar has declined your friendship offer */
		FriendshipDeclined, // 40
		/* Indicates that a user has started typing */
		StartTyping, // 41
		/* Indicates that a user has stopped typing */
		StopTyping; // 42

		public static InstantMessageDialog setValue(int value) {
			if (values().length > value)
				return values()[value];
			Logger.getLogger(InstantMessageDialog.class).error("Invalid InstantMessageDialog value: " + value);
			return MessageFromAgent;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/**
	 * Flag in Instant Messages, whether the IM should be delivered to offline
	 * avatars as well
	 */
	public enum InstantMessageOnline {
		/* Only deliver to online avatars */
		Online, // 0
		/*
		 * If the avatar is offline the message will be held until they login next, and
		 * possibly forwarded to their e-mail account
		 */
		Offline; // 1

		public static InstantMessageOnline setValue(int value) {
			if (values().length > value)
				return values()[value];
			Logger.getLogger(InstantMessageOnline.class).error("Invalid InstantMessageOnline value: " + value);
			return Offline;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/* Used to specify movement actions for your agent */
	public static class ControlFlags {
		private static final int CONTROL_AT_POS_INDEX = 0;
		private static final int CONTROL_AT_NEG_INDEX = 1;
		private static final int CONTROL_LEFT_POS_INDEX = 2;
		private static final int CONTROL_LEFT_NEG_INDEX = 3;
		private static final int CONTROL_UP_POS_INDEX = 4;
		private static final int CONTROL_UP_NEG_INDEX = 5;
		private static final int CONTROL_PITCH_POS_INDEX = 6;
		private static final int CONTROL_PITCH_NEG_INDEX = 7;
		private static final int CONTROL_YAW_POS_INDEX = 8;
		private static final int CONTROL_YAW_NEG_INDEX = 9;
		private static final int CONTROL_FAST_AT_INDEX = 10;
		private static final int CONTROL_FAST_LEFT_INDEX = 11;
		private static final int CONTROL_FAST_UP_INDEX = 12;
		private static final int CONTROL_FLY_INDEX = 13;
		private static final int CONTROL_STOP_INDEX = 14;
		private static final int CONTROL_FINISH_ANIM_INDEX = 15;
		private static final int CONTROL_STAND_UP_INDEX = 16;
		private static final int CONTROL_SIT_ON_GROUND_INDEX = 17;
		private static final int CONTROL_MOUSELOOK_INDEX = 18;
		private static final int CONTROL_NUDGE_AT_POS_INDEX = 19;
		private static final int CONTROL_NUDGE_AT_NEG_INDEX = 20;
		private static final int CONTROL_NUDGE_LEFT_POS_INDEX = 21;
		private static final int CONTROL_NUDGE_LEFT_NEG_INDEX = 22;
		private static final int CONTROL_NUDGE_UP_POS_INDEX = 23;
		private static final int CONTROL_NUDGE_UP_NEG_INDEX = 24;
		private static final int CONTROL_TURN_LEFT_INDEX = 25;
		private static final int CONTROL_TURN_RIGHT_INDEX = 26;
		private static final int CONTROL_AWAY_INDEX = 27;
		private static final int CONTROL_LBUTTON_DOWN_INDEX = 28;
		private static final int CONTROL_LBUTTON_UP_INDEX = 29;
		private static final int CONTROL_ML_LBUTTON_DOWN_INDEX = 30;
		private static final int CONTROL_ML_LBUTTON_UP_INDEX = 31;

		// Empty flag
		public static final int NONE = 0;
		// Move Forward (SL Keybinding: W/Up Arrow)
		public static final int AGENT_CONTROL_AT_POS = 0x1 << CONTROL_AT_POS_INDEX;
		// t Move Backward (SL Keybinding: S/Down Arrow)
		public static final int AGENT_CONTROL_AT_NEG = 0x1 << CONTROL_AT_NEG_INDEX;
		// Move Left (SL Keybinding: Shift-(A/Left Arrow))
		public static final int AGENT_CONTROL_LEFT_POS = 0x1 << CONTROL_LEFT_POS_INDEX;
		// Move Right (SL Keybinding: Shift-(D/Right Arrow))
		public static final int AGENT_CONTROL_LEFT_NEG = 0x1 << CONTROL_LEFT_NEG_INDEX;
		// Not Flying: Jump/Flying: Move Up (SL Keybinding: E)
		public static final int AGENT_CONTROL_UP_POS = 0x1 << CONTROL_UP_POS_INDEX;
		// Not Flying: Croutch/Flying: Move Down (SL Keybinding: C)
		public static final int AGENT_CONTROL_UP_NEG = 0x1 << CONTROL_UP_NEG_INDEX;
		// Unused
		public static final int AGENT_CONTROL_PITCH_POS = 0x1 << CONTROL_PITCH_POS_INDEX;
		// Unused
		public static final int AGENT_CONTROL_PITCH_NEG = 0x1 << CONTROL_PITCH_NEG_INDEX;
		// Unused
		public static final int AGENT_CONTROL_YAW_POS = 0x1 << CONTROL_YAW_POS_INDEX;
		// Unused
		public static final int AGENT_CONTROL_YAW_NEG = 0x1 << CONTROL_YAW_NEG_INDEX;
		// ORed with AGENT_CONTROL_AT_* if the keyboard is being used
		public static final int AGENT_CONTROL_FAST_AT = 0x1 << CONTROL_FAST_AT_INDEX;
		// ORed with AGENT_CONTROL_LEFT_* if the keyboard is being used
		public static final int AGENT_CONTROL_FAST_LEFT = 0x1 << CONTROL_FAST_LEFT_INDEX;
		// ORed with AGENT_CONTROL_UP_* if the keyboard is being used
		public static final int AGENT_CONTROL_FAST_UP = 0x1 << CONTROL_FAST_UP_INDEX;
		// Fly
		public static final int AGENT_CONTROL_FLY = 0x1 << CONTROL_FLY_INDEX;
		//
		public static final int AGENT_CONTROL_STOP = 0x1 << CONTROL_STOP_INDEX;
		// Finish our current animation
		public static final int AGENT_CONTROL_FINISH_ANIM = 0x1 << CONTROL_FINISH_ANIM_INDEX;
		// Stand up from the ground or a prim seat
		public static final int AGENT_CONTROL_STAND_UP = 0x1 << CONTROL_STAND_UP_INDEX;
		// Sit on the ground at our current location
		public static final int AGENT_CONTROL_SIT_ON_GROUND = 0x1 << CONTROL_SIT_ON_GROUND_INDEX;
		// Whether mouselook is currently enabled
		public static final int AGENT_CONTROL_MOUSELOOK = 0x1 << CONTROL_MOUSELOOK_INDEX;
		// Legacy, used if a key was pressed for less than a certain amount of
		// time
		public static final int AGENT_CONTROL_NUDGE_AT_POS = 0x1 << CONTROL_NUDGE_AT_POS_INDEX;
		// Legacy, used if a key was pressed for less than a certain amount of
		// time
		public static final int AGENT_CONTROL_NUDGE_AT_NEG = 0x1 << CONTROL_NUDGE_AT_NEG_INDEX;
		// Legacy, used if a key was pressed for less than a certain amount of
		// time
		public static final int AGENT_CONTROL_NUDGE_LEFT_POS = 0x1 << CONTROL_NUDGE_LEFT_POS_INDEX;
		// Legacy, used if a key was pressed for less than a certain amount of
		// time
		public static final int AGENT_CONTROL_NUDGE_LEFT_NEG = 0x1 << CONTROL_NUDGE_LEFT_NEG_INDEX;
		// Legacy, used if a key was pressed for less than a certain amount of
		// time
		public static final int AGENT_CONTROL_NUDGE_UP_POS = 0x1 << CONTROL_NUDGE_UP_POS_INDEX;
		// Legacy, used if a key was pressed for less than a certain amount of
		// time
		public static final int AGENT_CONTROL_NUDGE_UP_NEG = 0x1 << CONTROL_NUDGE_UP_NEG_INDEX;
		//
		public static final int AGENT_CONTROL_TURN_LEFT = 0x1 << CONTROL_TURN_LEFT_INDEX;
		//
		public static final int AGENT_CONTROL_TURN_RIGHT = 0x1 << CONTROL_TURN_RIGHT_INDEX;
		// Set when the avatar is idled or set to away. Note that the away
		// animation is
		// activated separately from setting this flag
		public static final int AGENT_CONTROL_AWAY = 0x1 << CONTROL_AWAY_INDEX;
		//
		public static final int AGENT_CONTROL_LBUTTON_DOWN = 0x1 << CONTROL_LBUTTON_DOWN_INDEX;
		//
		public static final int AGENT_CONTROL_LBUTTON_UP = 0x1 << CONTROL_LBUTTON_UP_INDEX;
		//
		public static final int AGENT_CONTROL_ML_LBUTTON_DOWN = 0x1 << CONTROL_ML_LBUTTON_DOWN_INDEX;
		//
		public static final int AGENT_CONTROL_ML_LBUTTON_UP = 0x1 << CONTROL_ML_LBUTTON_UP_INDEX;

		public static int setValue(int value) {
			return value;
		}

		public static int getValue(int value) {
			return value;
		}
	}

	/* */
	public static class TeleportFlags {
		/* No flags set, or teleport failed */
		public static final int Default = 0;
		/* Set when newbie leaves help island for first time */
		public static final int SetHomeToTarget = 1 << 0;
		/* */
		public static final int SetLastToTarget = 1 << 1;
		/* Via Lure */
		public static final int ViaLure = 1 << 2;
		/* Via Landmark */
		public static final int ViaLandmark = 1 << 3;
		/* Via Location */
		public static final int ViaLocation = 1 << 4;
		/* Via Home */
		public static final int ViaHome = 1 << 5;
		/* Via Telehub */
		public static final int ViaTelehub = 1 << 6;
		/* Via Login */
		public static final int ViaLogin = 1 << 7;
		/* Linden Summoned */
		public static final int ViaGodlikeLure = 1 << 8;
		/* Linden Forced me */
		public static final int Godlike = 1 << 9;
		/* */
		public static final int NineOneOne = 1 << 10;
		/* Agent Teleported Home via Script */
		public static final int DisableCancel = 1 << 11;
		/* */
		public static final int ViaRegionID = 1 << 12;
		/* */
		public static final int IsFlying = 1 << 13;
		/* */
		public static final int ResetHome = 1 << 14;
		/* forced to new location for example when avatar is banned or ejected */
		public static final int ForceRedirect = 1 << 15;
		/* Teleport Finished via a Lure */
		public static final int FinishedViaLure = 1 << 26;
		/* Finished, Sim Changed */
		public static final int FinishedViaNewSim = 1 << 28;
		/* Finished, Same Sim */
		public static final int FinishedViaSameSim = 1 << 29;

		public static int setValue(int value) {
			return (value & _mask);
		}

		public static int getValue(int value) {
			return (value & _mask);
		}

		private static final int _mask = 0x3400FFFF;
	}

}
