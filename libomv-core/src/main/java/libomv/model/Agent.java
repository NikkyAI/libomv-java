package libomv.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.AttachmentResourcesMessage;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.utils.CallbackArgs;
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

	/** Permission request flags, asked when a script wants to control an Avatar */
	public static class ScriptPermission {
		/* Placeholder for empty values, shouldn't ever see this */
		public static final int None = 0;
		/* Script wants ability to take money from you */
		public static final int Debit = 1 << 1;
		/* Script wants to take camera controls for you */
		public static final int TakeControls = 1 << 2;
		/* Script wants to remap avatars controls */
		public static final int RemapControls = 1 << 3;
		/*
		 * Script wants to trigger avatar animations This function is not implemented on
		 * the grid
		 */
		public static final int TriggerAnimation = 1 << 4;
		/* Script wants to attach or detach the prim or primset to your avatar */
		public static final int Attach = 1 << 5;
		/*
		 * Script wants permission to release ownership This function is not implemented
		 * on the grid The concept of "public" objects does not exist anymore.
		 */
		public static final int ReleaseOwnership = 1 << 6;
		/* Script wants ability to link/delink with other prims */
		public static final int ChangeLinks = 1 << 7;
		/*
		 * Script wants permission to change joints This function is not implemented on
		 * the grid
		 */
		public static final int ChangeJoints = 1 << 8;
		/*
		 * Script wants permissions to change permissions This function is not
		 * implemented on the grid
		 */
		public static final int ChangePermissions = 1 << 9;
		/* Script wants to track avatars camera position and rotation */
		public static final int TrackCamera = 1 << 10;
		/* Script wants to control your camera */
		public static final int ControlCamera = 1 << 11;

		// Script wants the ability to teleport you
		public static final int Teleport = 1 << 12;

		public static int setValue(int value) {
			return (value & _mask);
		}

		public static int getValue(int value) {
			return (value & _mask);
		}

		private static final int _mask = 0xFFF;
	}

	/*
	 * Conversion type to denote Chat Packet types in an easier-to-understand format
	 */
	public enum ChatType {
		/* Whisper (5m radius) */
		Whisper(0),
		/* Normal chat (10/20m radius), what the official viewer typically sends */
		Normal(1),
		/* Shouting! (100m radius) */
		Shout(2),
		/*
		 * Say chat (10/20m radius) - The official viewer will print
		 * "[4:15] You say, hey" instead of "[4:15] You: hey"
		 */
		// Say = 3,
		/* Event message when an Avatar has begun to type */
		StartTyping(4),
		/* Event message when an Avatar has stopped typing */
		StopTyping(5),
		/* Send the message to the debug channel */
		Debug(6),
		/*  */
		Region(7),
		/* Event message when an object uses llOwnerSay */
		OwnerSay(8),
		/* Event message when an object uses llRegionSayTo() */
		RegionSayTo(9),
		/* Special value to support llRegionSay(), never sent to the client */
		RegionSay(255);

		public static ChatType setValue(byte value) {
			for (ChatType e : values()) {
				if (e.val == value)
					return e;
			}
			return Normal;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private ChatType(int value) {
			val = value;
		}
	}

	/* Identifies the source of a chat message */
	public enum ChatSourceType {
		/* Chat from the grid or simulator */
		System,
		/* Chat from another avatar */
		Agent,
		/* Chat from an object */
		Object;

		public static ChatSourceType setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/*  */
	public enum ChatAudibleLevel {
		/*  */
		Not(-1),
		/*  */
		Barely(0),
		/*  */
		Fully(1);

		public static ChatAudibleLevel setValue(byte value) {
			for (ChatAudibleLevel e : values()) {
				if (e.val == value)
					return e;
			}
			return Barely;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private ChatAudibleLevel(int value) {
			val = value;
		}
	}

	/* Effect type used in ViewerEffect packets */
	public enum EffectType {
		/* */
		Text, // 0
		/* */
		Icon, // 1
		/* */
		Connector, // 2
		/* */
		FlexibleObject, // 3
		/* */
		AnimalControls, // 4
		/* */
		AnimationObject, // 5
		/* */
		Cloth, // 6
		/*
		 * Project a beam from a source to a destination, such as the one used when
		 * editing an object
		 */
		Beam, // 7
		/* */
		Glow, // 8
		/* */
		Point, // 9
		/* */
		Trail, // 10
		/* Create a swirl of particles around an object */
		Sphere, // 11
		/* */
		Spiral, // 12
		/* */
		Edit, // 13
		/* Cause an avatar to look at an object */
		LookAt, // 14
		/* Cause an avatar to point at an object */
		PointAt; // 15

		public static EffectType setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/*
	 * The action an avatar is doing when looking at something, used in ViewerEffect
	 * packets for the LookAt effect
	 */
	public enum LookAtType {
		/* */
		None,
		/* */
		Idle,
		/* */
		AutoListen,
		/* */
		FreeLook,
		/* */
		Respond,
		/* */
		Hover,
		/* Deprecated */
		Conversation,
		/* */
		Select,
		/* */
		Focus,
		/* */
		Mouselook,
		/* */
		Clear;

		public static LookAtType setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/*
	 * The action an avatar is doing when pointing at something, used in
	 * ViewerEffect packets for the PointAt effect
	 */
	public enum PointAtType {
		/* */
		None,
		/* */
		Select,
		/* */
		Grab,
		/* */
		Clear;

		public static PointAtType setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// Money transaction types
	public enum MoneyTransactionType {
		/* */
		None(0),
		/* */
		FailSimulatorTimeout(1),
		/* */
		FailDataserverTimeout(2),
		/* */
		ObjectClaim(1000),
		/* */
		LandClaim(1001),
		/* */
		GroupCreate(1002),
		/* */
		ObjectPublicClaim(1003),
		/* */
		GroupJoin(1004),
		/* */
		TeleportCharge(1100),
		/* */
		UploadCharge(1101),
		/* */
		LandAuction(1102),
		/* */
		ClassifiedCharge(1103),
		/* */
		ObjectTax(2000),
		/* */
		LandTax(2001),
		/* */
		LightTax(2002),
		/* */
		ParcelDirFee(2003),
		/* */
		GroupTax(2004),
		/* */
		ClassifiedRenew(2005),
		/* */
		GiveInventory(3000),
		/* */
		ObjectSale(5000),
		/* */
		Gift(5001),
		/* */
		LandSale(5002),
		/* */
		ReferBonus(5003),
		/* */
		InventorySale(5004),
		/* */
		RefundPurchase(5005),
		/* */
		LandPassSale(5006),
		/* */
		DwellBonus(5007),
		/* */
		PayObject(5008),
		/* */
		ObjectPays(5009),
		/* */
		GroupLandDeed(6001),
		/* */
		GroupObjectDeed(6002),
		/* */
		GroupLiability(6003),
		/* */
		GroupDividend(6004),
		/* */
		GroupMembershipDues(6005),
		/* */
		ObjectRelease(8000),
		/* */
		LandRelease(8001),
		/* */
		ObjectDelete(8002),
		/* */
		ObjectPublicDecay(8003),
		/* */
		ObjectPublicDelete(8004),
		/* */
		LindenAdjustment(9000),
		/* */
		LindenGrant(9001),
		/* */
		LindenPenalty(9002),
		/* */
		EventFee(9003),
		/* */
		EventPrize(9004),
		/* */
		StipendBasic(10000),
		/* */
		StipendDeveloper(10001),
		/* */
		StipendAlways(10002),
		/* */
		StipendDaily(10003),
		/* */
		StipendRating(10004),
		/* */
		StipendDelta(10005);

		public static MoneyTransactionType setValue(byte value) {
			for (MoneyTransactionType e : values()) {
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private MoneyTransactionType(int val) {
			this.val = val;
		}
	}

	/*  */
	public static class TransactionFlags {
		/* */
		public static final byte None = 0x0;
		/* */
		public static final byte SourceGroup = 0x1;
		/* */
		public static final byte DestGroup = 0x2;
		/* */
		public static final byte OwnerGroup = 0x4;
		/* */
		public static final byte SimultaneousContribution = 0x8;
		/* */
		public static final byte ContributionRemoval = 0x10;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x1F;
	}

	public enum MeanCollisionType {
		/* */
		None,
		/* */
		Bump,
		/* */
		LLPushObject,
		/* */
		SelectedObjectCollide,
		/* */
		ScriptedObjectCollide,
		/* */
		PhysicalObjectCollide;

		public static MeanCollisionType setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/*
	 * Flags sent when a script takes or releases a control
	 *
	 * NOTE: (need to verify) These might be a subset of the ControlFlags enum in
	 * Movement,
	 */
	public static class ScriptControlChange {
		/* No Flags set */
		public static final int None = 0;
		/* Forward (W or up Arrow) */
		public static final int Forward = 0x1;
		/* Back (S or down arrow) */
		public static final int Back = 0x2;
		/* Move left (shift+A or left arrow) */
		public static final int Left = 0x4;
		/* Move right (shift+D or right arrow) */
		public static final int Right = 0x8;
		/* Up (E or PgUp) */
		public static final int Up = 0x10;
		/* Down (C or PgDown) */
		public static final int Down = 0x20;
		/* Rotate left (A or left arrow) */
		public static final int RotateLeft = 0x100;
		/* Rotate right (D or right arrow) */
		public static final int RotateRight = 0x200;
		/* Left Mouse Button */
		public static final int LeftButton = 0x10000000;
		/* Left Mouse button in MouseLook */
		public static final int MouseLookLeftButton = 0x40000000;

		public static int setValue(int value) {
			return value & _mask;
		}

		public static int getValue(int value) {
			return value & _mask;
		}

		private static final int _mask = 0x5000033F;
	}

	/* Currently only used to hide your group title */
	public enum AgentFlags {
		/* No flags set */
		None,
		/* Hide your group title */
		HideTitle;

		public static AgentFlags setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/* Action state of the avatar, which can currently be typing and editing */
	public static class AgentState {
		/* */
		public static final byte None = 0x00;
		/* */
		public static final byte Typing = 0x04;
		/* */
		public static final byte Editing = 0x10;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x14;
	}

	/* Current teleport status */
	public enum TeleportStatus {
		/* Unknown status */
		None,
		/* Teleport initialized */
		Start,
		/* Teleport in progress */
		Progress,
		/* Teleport failed */
		Failed,
		/* Teleport completed */
		Finished,
		/* Teleport cancelled */
		Cancelled;

		public static TeleportStatus setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/* */
	public enum TeleportLureFlags {
		/* */
		NormalLure,
		/* */
		GodlikeLure,
		/* */
		GodlikePursuit;

		public static TeleportLureFlags setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/* */
	public static class ScriptSensorTypeFlags {
		/* */
		public static final byte Agent = 1;
		/* */
		public static final byte Active = 2;
		/* */
		public static final byte Passive = 4;
		/* */
		public static final byte Scripted = 8;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0xF;
	}

	/**
	 * Type of mute entry
	 */
	public enum MuteType {
		// Object muted by name
		ByName,
		// Muted resident
		Resident,
		// Object muted by UUID
		Object,
		// Muted group
		Group,
		// Muted external entry
		External;

		public static MuteType setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/**
	 * Flags of mute entry
	 */
	// [Flags]
	public static class MuteFlags {
		// No exceptions
		public static final byte Default = 0x0;
		// Don't mute text chat
		public static final byte TextChat = 0x1;
		// Don't mute voice chat
		public static final byte VoiceChat = 0x2;
		// Don't mute particles
		public static final byte Particles = 0x4;
		// Don't mute sounds
		public static final byte ObjectSounds = 0x8;
		// Don't mute
		public static final byte All = 0xf;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static int getValue(byte value) {
			return value & _mask;
		}

		private static final byte _mask = 0xf;
	}

	/* Instant Message */
	public final class InstantMessage {
		/* Key of sender */
		public UUID FromAgentID;
		/* Name of sender */
		public String FromAgentName;
		/* Key of destination avatar */
		public UUID ToAgentID;
		/* ID of originating estate */

		public int ParentEstateID;
		/* Key of originating region */
		public UUID RegionID;
		/* Coordinates in originating region */
		public Vector3 Position;
		/* Instant message type */
		public InstantMessageDialog Dialog;
		/* Group IM session toggle */
		public boolean GroupIM;
		/* Key of IM session, for Group Messages, the groups UUID */
		public UUID IMSessionID;
		/* Timestamp of the instant message */
		public Date Timestamp;
		/* Instant message text */
		public String Message;
		/* Whether this message is held for offline avatars */
		public InstantMessageOnline Offline;
		/* Context specific packed data */
		public byte[] BinaryBucket;

		/*
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	// Represents muted object or resident
	public class MuteEntry {
		// Type of the mute entry
		public MuteType Type;
		// UUID of the mute entry
		public UUID ID;
		// Mute entry name
		public String Name;
		// Mute flags
		public byte Flags;
	}

	public class LureLocation {
		public long regionHandle;
		public Vector3 position;
		public Vector3 lookAt;
		public String maturity;
	}

	// Transaction detail sent with MoneyBalanceReply message
	public class TransactionInfo {
		// Type of the transaction
		public int TransactionType; // FIXME: this should be an enum
		// UUID of the transaction source
		public UUID SourceID;
		// Is the transaction source a group
		public boolean IsSourceGroup;
		// UUID of the transaction destination
		public UUID DestID;
		// Is transaction destination a group
		public boolean IsDestGroup;
		// Transaction amount
		public int Amount;
		// Transaction description
		public String ItemDescription;
	}

	public class GroupChatJoinedCallbackArgs implements CallbackArgs {
		private final UUID m_SessionID;
		private final String m_SessionName;
		private final UUID m_tmpSessionID;
		private final boolean m_Success;

		// Get the ID of the chat session
		public UUID getSessionID() {
			return m_SessionID;
		}

		// Get the name of the chat session
		public String getSessionName() {
			return m_SessionName;
		}

		// Get the ID of the agent that joined
		public UUID getTempSessionID() {
			return m_tmpSessionID;
		}

		public boolean getSucess() {
			return m_Success;
		}

		public GroupChatJoinedCallbackArgs(UUID sessionID, String name, UUID tmpSessionID, boolean success) {
			m_SessionID = sessionID;
			m_SessionName = name;
			m_tmpSessionID = tmpSessionID;
			m_Success = success;
		}
	}

	// Data sent when an agent joins or leaves a chat session your agent is
	// currently participating in
	public class ChatSessionMemberCallbackArgs implements CallbackArgs {
		private final UUID m_SessionID;
		private final UUID m_AgentID;
		private final boolean m_added;

		// Get the ID of the chat session
		public UUID getSessionID() {
			return m_SessionID;
		}

		// Get the ID of the agent that joined
		public UUID getAgentID() {
			return m_AgentID;
		}

		public boolean getAdded() {
			return m_added;
		}

		public ChatSessionMemberCallbackArgs(UUID sessionID, UUID agentID, boolean added) {
			this.m_SessionID = sessionID;
			this.m_AgentID = agentID;
			this.m_added = added;
		}
	}

	public class ChatCallbackArgs implements CallbackArgs {
		private ChatAudibleLevel audible;
		private ChatType type;
		private ChatSourceType sourcetype;
		private String message, fromName;
		private UUID sourceId;
		private UUID ownerId;
		private Vector3 position;

		public String getMessage() {
			return message;
		}

		public String getFromName() {
			return fromName;
		}

		public ChatAudibleLevel getAudible() {
			return audible;
		}

		public ChatType getType() {
			return type;
		}

		public ChatSourceType getSourceType() {
			return sourcetype;
		}

		public UUID getSourceID() {
			return sourceId;
		}

		public UUID getOwnerID() {
			return ownerId;
		}

		public Vector3 getPosition() {
			return position;
		}

		public ChatCallbackArgs(ChatAudibleLevel audible, ChatType type, ChatSourceType sourcetype, String fromName,
				String message, UUID sourceId, UUID ownerId, Vector3 position) {
			this.message = message;
			this.fromName = fromName;
			this.audible = audible;
			this.type = type;
			this.sourcetype = sourcetype;
			this.sourceId = sourceId;
			this.ownerId = ownerId;
			this.position = position;
		}
	}

	/* The date received from an ImprovedInstantMessage */
	public class InstantMessageCallbackArgs {
		private final InstantMessage m_IM;
		private final Simulator m_Simulator;

		/* Get the InstantMessage object */
		public final InstantMessage getIM() {
			return m_IM;
		}

		/* Get the simulator where the InstantMessage origniated */
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		/**
		 * Construct a new instance of the InstantMessageEventArgs object
		 *
		 * @param im
		 *            the InstantMessage object
		 * @param simulator
		 *            the simulator where the InstantMessage origniated
		 */
		public InstantMessageCallbackArgs(InstantMessage im, Simulator simulator) {
			this.m_IM = im;
			this.m_Simulator = simulator;
		}
	}

	public class TeleportCallbackArgs implements CallbackArgs {
		private final String message;
		private final TeleportStatus status;
		private final int flags;

		public String getMessage() {
			return message;
		}

		public TeleportStatus getStatus() {
			return status;
		}

		public int getFlags() {
			return flags;
		}

		public TeleportCallbackArgs(String message, TeleportStatus status, int flags) {
			this.message = message;
			this.status = status;
			this.flags = flags;
		}
	}

	public class TeleportLureCallbackArgs implements CallbackArgs {
		private final UUID fromID;
		private final String fromName;
		private final UUID lureID;
		private final String message;
		private final LureLocation location; // if null, it's a godlike lure request

		public UUID getFromID() {
			return fromID;
		}

		public String getFromName() {
			return fromName;
		}

		public UUID getLureID() {
			return lureID;
		}

		public String getMessage() {
			return message;
		}

		public LureLocation getLocation() {
			return location;
		}

		public TeleportLureCallbackArgs(UUID fromID, String fromName, UUID lureID, String message,
				LureLocation location) {
			this.fromID = fromID;
			this.fromName = fromName;
			this.lureID = lureID;
			this.location = location;
			this.message = message;
		}
	}

	/* The date received from an ImprovedInstantMessage */
	public class BalanceCallbackArgs {
		private final int balance;
		private final int delta;
		private final boolean firstBalance;

		/* Get the balance value */
		public final int getBalance() {
			return balance;
		}

		/* Get the balance value */
		public final int getDelta() {
			return delta;
		}

		/* Get the balance value */
		public final boolean getFirst() {
			return firstBalance;
		}

		/**
		 * Construct a new instance of the BalanceCallbackArgs object
		 *
		 * @param balance
		 *            the InstantMessage object
		 */
		public BalanceCallbackArgs(int balance, int delta, boolean firstBalance) {
			this.balance = balance;
			this.delta = delta;
			this.firstBalance = firstBalance;
		}
	}

	public class RegionCrossedCallbackArgs implements CallbackArgs {
		private final Simulator oldSim, newSim;

		public Simulator getOldSim() {
			return oldSim;
		}

		public Simulator getNewSim() {
			return newSim;
		}

		public RegionCrossedCallbackArgs(Simulator oldSim, Simulator newSim) {
			this.oldSim = oldSim;
			this.newSim = newSim;
		}
	}

	public class AttachmentResourcesCallbackArgs {
		AttachmentResourcesMessage info;
		boolean success;

		public AttachmentResourcesCallbackArgs(boolean success, AttachmentResourcesMessage info) {
			this.info = info;
			this.success = success;
		}
	}

	// Event arguments with the result of setting display name
	// operation
	public class SetDisplayNameReplyCallbackArgs implements CallbackArgs {
		private final int m_Status;
		private final String m_Reason;
		private final AgentDisplayName m_DisplayName;

		// Status code, 200 indicates setting display name was successful
		public int getStatus() {
			return m_Status;
		}

		// Textual description of the status
		public String getReason() {
			return m_Reason;
		}

		// Details of the newly set display name
		public AgentDisplayName getDisplayName() {
			return m_DisplayName;
		}

		public SetDisplayNameReplyCallbackArgs(int status, String reason, AgentDisplayName displayName) {
			m_Status = status;
			m_Reason = reason;
			m_DisplayName = displayName;
		}
	}

	// Data sent by a script requesting to take or release specified controls to
	// your agent
	public class ScriptControlChangeCallbackArgs implements CallbackArgs {
		private int m_Controls;
		private boolean m_Pass;
		private boolean m_Take;

		// Get the controls the script is attempting to take or release to the agent
		public int getControl() {
			return m_Controls;
		}

		// True if the script is passing controls back to the agent
		public boolean getPass() {
			return m_Pass;
		}

		// True if the script is requesting controls be released to the script
		public boolean getTake() {
			return m_Take;
		}

		/**
		 * Construct a new instance of the ScriptControlEventArgs class
		 *
		 * @param controls
		 *            The controls the script is attempting to take or release to the
		 *            agent
		 * @param pass
		 *            True if the script is passing controls back to the agent
		 * @param take
		 *            True if the script is requesting controls be released to the
		 *            script
		 */
		public ScriptControlChangeCallbackArgs(int controls, boolean pass, boolean take) {
			this.m_Controls = controls;
			this.m_Pass = pass;
			this.m_Take = take;
		}
	}

	// Data containing script sensor requests which allow an agent to know the
	// specific details
	// of a primitive sending script sensor requests
	public class ScriptSensorReplyCallbackArgs implements CallbackArgs {
		private final UUID m_RequestorID;
		private final UUID m_GroupID;
		private final String m_Name;
		private final UUID m_ObjectID;
		private final UUID m_OwnerID;
		private final Vector3 m_Position;
		private final float m_Range;
		private final Quaternion m_Rotation;
		private final byte m_Type;
		private final Vector3 m_Velocity;

		// Get the ID of the primitive sending the sensor
		public UUID getRequestorID() {
			return m_RequestorID;
		}

		// Get the ID of the group associated with the primitive
		public UUID getGroupID() {
			return m_GroupID;
		}

		// Get the name of the primitive sending the sensor
		public String getName() {
			return m_Name;
		}

		// Get the ID of the primitive sending the sensor
		public UUID getObjectID() {
			return m_ObjectID;
		}

		// Get the ID of the owner of the primitive sending the sensor
		public UUID getOwnerID() {
			return m_OwnerID;
		}

		// Get the position of the primitive sending the sensor
		public Vector3 getPosition() {
			return m_Position;
		}

		// Get the range the primitive specified to scan
		public float getRange() {
			return m_Range;
		}

		// Get the rotation of the primitive sending the sensor
		public Quaternion getRotation() {
			return m_Rotation;
		}

		// Get the type of sensor the primitive sent
		public byte getType() {
			return m_Type;
		}

		// Get the velocity of the primitive sending the sensor
		public Vector3 getVelocity() {
			return m_Velocity;
		}

		/**
		 * Construct a new instance of the ScriptSensorReplyEventArgs
		 *
		 * @param requestorID
		 *            The ID of the primitive sending the sensor
		 * @param groupID
		 *            The ID of the group associated with the primitive
		 * @param name
		 *            The name of the primitive sending the sensor
		 * @param objectID
		 *            The ID of the primitive sending the sensor
		 * @param ownerID
		 *            The ID of the owner of the primitive sending the sensor
		 * @param position
		 *            The position of the primitive sending the sensor
		 * @param range
		 *            The range the primitive specified to scan
		 * @param rotation
		 *            The rotation of the primitive sending the sensor
		 * @param type
		 *            The type of sensor the primitive sent
		 * @param velocity
		 *            The velocity of the primitive sending the sensor
		 */
		public ScriptSensorReplyCallbackArgs(UUID requestorID, UUID groupID, String name, UUID objectID, UUID ownerID,
				Vector3 position, float range, Quaternion rotation, byte type, Vector3 velocity) {
			this.m_RequestorID = requestorID;
			this.m_GroupID = groupID;
			this.m_Name = name;
			this.m_ObjectID = objectID;
			this.m_OwnerID = ownerID;
			this.m_Position = position;
			this.m_Range = range;
			this.m_Rotation = rotation;
			this.m_Type = type;
			this.m_Velocity = velocity;
		}
	}

	public class AlertMessageCallbackArgs implements CallbackArgs {
		private final String alert;

		public String getAlert() {
			return alert;
		}

		public AlertMessageCallbackArgs(String alert) {
			this.alert = alert;
		}
	}

	public class GenericMessageCallbackArgs implements CallbackArgs {
		private final UUID sessionID;
		private final UUID transactionID;
		private final String method;
		private final UUID invoiceID;
		private List<String> parameters;

		public UUID getSessionID() {
			return sessionID;
		}

		public UUID getTransactionID() {
			return transactionID;
		}

		public String getMethod() {
			return method;
		}

		public UUID getInvoiceID() {
			return invoiceID;
		}

		public List<String> getParameters() {
			return parameters;
		}

		public GenericMessageCallbackArgs(UUID sessionID, UUID transactionID, String method, UUID invoiceID,
				List<String> parameters) {
			this.sessionID = sessionID;
			this.transactionID = transactionID;
			this.method = method;
			this.invoiceID = invoiceID;
			this.parameters = parameters;
		}
	}

	public class CameraConstraintCallbackArgs implements CallbackArgs {
		private final Vector4 constraints;

		public Vector4 getConstraints() {
			return constraints;
		}

		public CameraConstraintCallbackArgs(Vector4 constraints) {
			this.constraints = constraints;
		}
	}

	// Data sent from a simulator indicating a collision with your agent
	public class MeanCollisionCallbackArgs implements CallbackArgs {
		private final MeanCollisionType m_Type;
		private final UUID m_Aggressor;
		private final UUID m_Victim;
		private final float m_Magnitude;
		private final Date m_Time;

		// Get the Type of collision
		public MeanCollisionType getType() {
			return m_Type;
		}

		// Get the ID of the agent or object that collided with your agent
		public UUID getAggressor() {
			return m_Aggressor;
		}

		// Get the ID of the agent that was attacked
		public UUID getVictim() {
			return m_Victim;
		}

		// A value indicating the strength of the collision
		public float getMagnitude() {
			return m_Magnitude;
		}

		// Get the time the collision occurred
		public Date getTime() {
			return m_Time;
		}

		/**
		 * Construct a new instance of the MeanCollisionEventArgs class
		 *
		 * @param type
		 *            The type of collision that occurred
		 * @param perp
		 *            The ID of the agent or object that perpetrated the agression
		 * @param victim
		 *            The ID of the Victim
		 * @param magnitude
		 *            The strength of the collision
		 * @param time
		 *            The Time the collision occurred
		 */
		public MeanCollisionCallbackArgs(MeanCollisionType type, UUID perp, UUID victim, float magnitude, Date time) {
			this.m_Type = type;
			this.m_Aggressor = perp;
			this.m_Victim = victim;
			this.m_Magnitude = magnitude;
			this.m_Time = time;
		}
	}

	// Contains the response data returned from the simulator in response to a <see
	// cref="RequestSit"/>
	public class AvatarSitResponseCallbackArgs implements CallbackArgs {
		private final UUID m_ObjectID;
		private final boolean m_Autopilot;
		private final Vector3 m_CameraAtOffset;
		private final Vector3 m_CameraEyeOffset;
		private final boolean m_ForceMouselook;
		private final Vector3 m_SitPosition;
		private final Quaternion m_SitRotation;

		/// <summary>Get the ID of the primitive the agent will be sitting on</summary>
		public UUID getObjectID() {
			return m_ObjectID;
		}

		/// <summary>True if the simulator Autopilot functions were involved</summary>
		public boolean getAutopilot() {
			return m_Autopilot;
		}

		/// <summary>Get the camera offset of the agent when seated</summary>
		public Vector3 getCameraAtOffset() {
			return m_CameraAtOffset;
		}

		/// <summary>Get the camera eye offset of the agent when seated</summary>
		public Vector3 getCameraEyeOffset() {
			return m_CameraEyeOffset;
		}

		/// <summary>True of the agent will be in mouselook mode when seated</summary>
		public boolean getForceMouselook() {
			return m_ForceMouselook;
		}

		/// <summary>Get the position of the agent when seated</summary>
		public Vector3 getSitPosition() {
			return m_SitPosition;
		}

		/// <summary>Get the rotation of the agent when seated</summary>
		public Quaternion getSitRotation() {
			return m_SitRotation;
		}

		// Construct a new instance of the AvatarSitResponseEventArgs object
		public AvatarSitResponseCallbackArgs(UUID objectID, boolean autoPilot, Vector3 cameraAtOffset,
				Vector3 cameraEyeOffset, boolean forceMouselook, Vector3 sitPosition, Quaternion sitRotation) {
			this.m_ObjectID = objectID;
			this.m_Autopilot = autoPilot;
			this.m_CameraAtOffset = cameraAtOffset;
			this.m_CameraEyeOffset = cameraEyeOffset;
			this.m_ForceMouselook = forceMouselook;
			this.m_SitPosition = sitPosition;
			this.m_SitRotation = sitRotation;
		}
	}

	public class AnimationsChangedCallbackArgs implements CallbackArgs {
		private final HashMap<UUID, Integer> m_Animations;

		public HashMap<UUID, Integer> getAnimations() {
			return m_Animations;
		}

		public AnimationsChangedCallbackArgs(HashMap<UUID, Integer> animations) {
			m_Animations = animations;
		}
	}

	public class AgentDataReplyCallbackArgs implements CallbackArgs {
		private final String m_FirstName;
		private final String m_LastName;
		private final UUID m_ActiveGroup;
		private final String m_GroupName;
		private final String m_GroupTitle;
		private final long m_ActiveGroupPowers;

		public String getFristName() {
			return m_FirstName;
		}

		public String getLastName() {
			return m_LastName;
		}

		public UUID getActiveGroup() {
			return m_ActiveGroup;
		}

		public String getGroupName() {
			return m_GroupName;
		}

		public String getGroupTitle() {
			return m_GroupTitle;
		}

		public long getActiveGroupPowers() {
			return m_ActiveGroupPowers;
		}

		public AgentDataReplyCallbackArgs(String firstName, String lastName, UUID activeGroup, String groupTitle,
				long activeGroupPowers, String groupName) {
			this.m_FirstName = firstName;
			this.m_LastName = lastName;
			this.m_ActiveGroup = activeGroup;
			this.m_GroupName = groupName;
			this.m_GroupTitle = groupTitle;
			this.m_ActiveGroupPowers = activeGroupPowers;
		}
	}

	// Contains the transaction summary when an item is purchased, money is
	// given, or land is purchased
	public class MoneyBalanceReplyCallbackArgs implements CallbackArgs {
		private final UUID m_TransactionID;
		private final boolean m_Success;
		private final int m_Balance;
		private final int m_MetersCredit;
		private final int m_MetersCommitted;
		private final String m_Description;
		private TransactionInfo m_TransactionInfo;

		// Get the ID of the transaction
		public UUID getTransactionID() {
			return m_TransactionID;
		}

		// True of the transaction was successful
		public boolean getSuccess() {
			return m_Success;
		}

		// Get the remaining currency balance
		public int getBalance() {
			return m_Balance;
		}

		// Get the meters credited
		public int getMetersCredit() {
			return m_MetersCredit;
		}

		// Get the meters comitted
		public int getMetersCommitted() {
			return m_MetersCommitted;
		}

		// Get the description of the transaction
		public String getDescription() {
			return m_Description;
		}

		// Detailed transaction information
		public TransactionInfo getTransactionInfo() {
			return m_TransactionInfo;
		}

		/**
		 * Construct a new instance of the MoneyBalanceReplyEventArgs object
		 *
		 * @param transactionID
		 *            The ID of the transaction
		 * @param transactionSuccess
		 *            True of the transaction was successful
		 * @param balance
		 *            The current currency balance
		 * @param metersCredit
		 *            The meters credited
		 * @param metersCommitted
		 *            The meters comitted
		 * @param description
		 *            A brief description of the transaction
		 * @param transactionInfo
		 *            Transaction info
		 */
		public MoneyBalanceReplyCallbackArgs(UUID transactionID, boolean transactionSuccess, int balance,
				int metersCredit, int metersCommitted, String description, TransactionInfo transactionInfo) {
			this.m_TransactionID = transactionID;
			this.m_Success = transactionSuccess;
			this.m_Balance = balance;
			this.m_MetersCredit = metersCredit;
			this.m_MetersCommitted = metersCommitted;
			this.m_Description = description;
			this.m_TransactionInfo = transactionInfo;
		}
	}

	// Contains the transaction summary when an item is purchased, money is
	// given, or land is purchased
	public class AgentAccessCallbackArgs implements CallbackArgs {
		final String mNewLevel;
		final boolean mSuccess;

		// New maturity accesss level returned from the sim
		public String getNewLevel() {
			return mNewLevel;
		}

		public boolean getSuccess() {
			return mSuccess;
		};

		public AgentAccessCallbackArgs(String newLevel, boolean success) {
			mNewLevel = newLevel;
			mSuccess = success;
		}
	}

}
