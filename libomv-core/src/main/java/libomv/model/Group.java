package libomv.model;

import java.io.IOException;
import java.util.Date;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.model.Asset.AssetType;
import libomv.types.UUID;
import libomv.utils.HashMapInt;
import libomv.utils.Helpers;

public interface Group {

	// /#region Structs

	// Avatar group management
	public final class GroupMember {
		// Key of Group Member
		public UUID ID;
		// Total land contribution
		public int Contribution;
		// Online status information
		public String OnlineStatus;
		// Abilities that the Group Member has
		public long Powers;
		// Current group title
		public String Title;
		// Is a group owner
		public boolean IsOwner;

		public GroupMember(UUID agentID) {
			ID = agentID;
		}
	}

	// Role manager for a group
	public final class GroupRole {
		// Key of the group
		public UUID GroupID;
		// Key of Role
		public UUID ID;
		// Name of Role
		public String Name;
		// Group Title associated with Role
		public String Title;
		// Description of Role
		public String Description;
		// Abilities Associated with Role
		public long Powers;

		// Returns the role's title
		@Override
		public String toString() {
			return Name;
		}

		public GroupRole(UUID roleID) {
			ID = roleID;
		}
	}

	// Class to represent Group Title
	public final class GroupTitle {
		// Key of the group
		public UUID GroupID;
		// ID of the role title belongs to
		public UUID RoleID;
		// Group Title
		public String Title;
		// Whether title is Active
		public boolean Selected;

		// Returns group title
		@Override
		public String toString() {
			return Title;
		}

		public GroupTitle() {
		}
	}

	// @formatter:off
	/*
	public final class Group {
		// Key of Group
		private UUID ID;
		// Key of Group Insignia
		public UUID InsigniaID;
		// Key of Group Founder
		public UUID FounderID;
		// Key of Group Role for Owners
		public UUID OwnerRole;
		// Name of Group
		private String Name;
		// Text of Group Charter
		public String Charter;
		// Title of "everyone" role
		public String MemberTitle;
		// Is the group open for enrolement to everyone
		public boolean OpenEnrollment;
		// Will group show up in search
		public boolean ShowInList;
		// GroupPowers flags
		public long Powers;
		//
		public boolean AcceptNotices;
		//
		public boolean AllowPublish;
		// Is the group Mature
		public boolean MaturePublish;
		// Cost of group membership
		public int MembershipFee;
		//
		public int Money;
		//
		public int Contribution;
		// The total number of current members this group has
		public int GroupMembershipCount;
		// The number of roles this group has configured
		public int GroupRolesCount;
		// Show this group in agent's profile
		public boolean ListInProfile;

		public UUID getID() {
			return ID;
		}

		public String getName() {
			return Name;
		}

		@Override
		public boolean equals(Object o) {
			return (o != null && o instanceof Group) ? equals((Group) o) : false;
		}

		public boolean equals(Group o) {
			return o != null ? ID.equals(o.ID) : false;
		}

		@Override
		public int hashCode() {
			return ID.hashCode();
		}

		// Returns the name of the group
		@Override
		public String toString() {
			return Name;
		}

		public Group(UUID id) {
			ID = id;
			InsigniaID = new UUID();
		}
	}
	*/
	// @formatter:on

	// A group Vote
	public final class Vote {
		// Key of Avatar who created Vote
		public UUID Candidate;
		// Text of the Vote proposal
		public String VoteString;
		// Total number of votes
		public int NumVotes;
	}

	// A group proposal
	public final class GroupProposal {
		// The minimum number of members that must vote before proposal passes
		// or fails
		public int Quorum;
		// The required ration of yes/no votes required for vote to pass
		// The three options are Simple Majority, 2/3 Majority, and Unanimous
		// TODO: this should be an enum
		public float Majority;
		// The duration in days votes are accepted
		public int Duration;
		// The Text of the proposal
		public String ProposalText;
	}

	// A group proposal
	public final class GroupProposalItem {
		public UUID VoteID;
		public UUID VoteInitiator;
		public String TerseDateID;
		public boolean AlreadyVoted;
		public String VoteCast;
		// The minimum number of members that must vote before proposal passes
		// or failes
		public int Quorum;
		// The required ration of yes/no votes required for vote to pass
		// The three options are Simple Majority, 2/3 Majority, and Unanimous
		// TODO: this should be an enum
		public float Majority;
		public Date StartDateTime;
		public Date EndDateTime;
		// The Text of the proposal
		public String ProposalText;
	}

	public final class GroupAccountSummary {
		//
		public int IntervalDays;
		//
		public int CurrentInterval;
		//
		public String StartDate;
		//
		public int Balance;
		//
		public int TotalCredits;
		//
		public int TotalDebits;
		//
		public int ObjectTaxCurrent;
		//
		public int LightTaxCurrent;
		//
		public int LandTaxCurrent;
		//
		public int GroupTaxCurrent;
		//
		public int ParcelDirFeeCurrent;
		//
		public int ObjectTaxEstimate;
		//
		public int LightTaxEstimate;
		//
		public int LandTaxEstimate;
		//
		public int GroupTaxEstimate;
		//
		public int ParcelDirFeeEstimate;
		//
		public int NonExemptMembers;
		//
		public String LastTaxDate;
		//
		public String TaxDate;
	}

	public class GroupAccountDetails {
		public int IntervalDays;

		public int CurrentInterval;

		public String StartDate;

		// A list of description/amount pairs making up the account history
		//
		// public List<KeyValuePair<string, int>> HistoryItems;
		// Still needs to implement the GroupAccount Details Handler and define
		// the data type
		public HashMapInt<String> HistoryItems;
	}

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

	// Struct representing a group notice list entry
	public final class GroupNoticesListEntry {
		// Notice ID
		public UUID NoticeID;
		// Creation timestamp of notice
		// TODO: ORIGINAL LINE: public uint Timestamp;
		public int Timestamp;
		// Agent name who created notice
		public String FromName;
		// Notice subject
		public String Subject;
		// Is there an attachment?
		public boolean HasAttachment;
		// Attachment Type
		public AssetType AssetType;

	}

	public class GroupAccountTransactions {
		public class TransactionEntry {
			public String Time;
			public String Item;
			public String User;
			public int Type;
			public int Amount;
		}

		public int IntervalDays;

		public int CurrentInterval;

		public String StartDate;

		public TransactionEntry[] Transactions;
	}

	// Struct representing a member of a group chat session and their settings
	public final class ChatSessionMember {
		// The <see cref="UUID"/> of the Avatar
		public UUID AvatarKey;
		// True if user has voice chat enabled
		public boolean CanVoiceChat;
		// True of Avatar has moderator abilities
		public boolean IsModerator;
		// True if a moderator has muted this avatars chat
		public boolean MuteText;
		// True if a moderator has muted this avatars voice
		public boolean MuteVoice;

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof ChatSessionMember))
				return false;
			return AvatarKey.equals(((ChatSessionMember) obj).AvatarKey);
		}

		public int hashCode() {
			return AvatarKey.hashCode();
		}
	}

	// #endregion Structs

	// #region Enums

	// Role update flags
	public enum GroupRoleUpdate {
		//
		NoUpdate,
		//
		UpdateData,
		//
		UpdatePowers,
		//
		UpdateAll,
		//
		Create,
		//
		Delete;

		public GroupRoleUpdate setValue(byte value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// [Flags]
	public static class GroupPowers {
		//
		public static final long None = 0;

		// Membership
		// Can send invitations to groups default role
		public static final long Invite = 1L << 1;
		// Can eject members from group
		public static final long Eject = 1L << 2;
		// Can toggle 'Open Enrollment' and change 'Signup fee'
		public static final long ChangeOptions = 1L << 3;
		// Member is visible in the public member list
		public static final long MemberVisible = 1L << 47;

		// Roles
		// Can create new roles
		public static final long CreateRole = 1L << 4;
		// Can delete existing roles
		public static final long DeleteRole = 1L << 5;
		// Can change Role names, titles and descriptions
		public static final long RoleProperties = 1L << 6;
		// Can assign other members to assigners role
		public static final long AssignMemberLimited = 1L << 7;
		// Can assign other members to any role
		public static final long AssignMember = 1L << 8;
		// Can remove members from roles
		public static final long RemoveMember = 1L << 9;
		// Can assign and remove abilities in roles
		public static final long ChangeActions = 1L << 10;

		// Identity
		// Can change group Charter, Insignia, 'Publish on the web' and which
		// members are publicly visible in group member listings
		public static final long ChangeIdentity = 1L << 11;

		// Parcel management
		// Can buy land or deed land to group
		public static final long LandDeed = 1L << 12;
		// Can abandon group owned land to Governor Linden on mainland, or
		// Estate owner for private estates
		public static final long LandRelease = 1L << 13;
		// Can set land for-sale information on group owned parcels
		public static final long LandSetSale = 1L << 14;
		// Can subdivide and join parcels
		public static final long LandDivideJoin = 1L << 15;

		// Group Chat moderation related
		// Can join group chat sessions
		public static final long JoinChat = 1L << 16;
		// Can use voice chat in Group Chat sessions
		public static final long AllowVoiceChat = 1L << 27;
		// Can moderate group chat sessions
		public static final long ModerateChat = 1L << 37;

		// Parcel identity
		// Can toggle "Show in Find Places" and set search category
		public static final long FindPlaces = 1L << 17;
		// Can change parcel name, description, and 'Publish on web' settings
		public static final long LandChangeIdentity = 1L << 18;
		// Can set the landing point and teleport routing on group land
		public static final long SetLandingPoint = 1L << 19;

		// Parcel settings
		// Can change music and media settings
		public static final long ChangeMedia = 1L << 20;
		// Can toggle 'Edit Terrain' option in Land settings
		public static final long LandEdit = 1L << 21;
		// Can toggle various About Land > Options settings
		public static final long LandOptions = 1L << 22;

		// Parcel powers
		// Can always terraform land, even if parcel settings have it turned off
		public static final long AllowEditLand = 1L << 23;
		// Can always fly while over group owned land
		public static final long AllowFly = 1L << 24;
		// Can always rez objects on group owned land
		public static final long AllowRez = 1L << 25;
		// Can always create landmarks for group owned parcels
		public static final long AllowLandmark = 1L << 26;
		// Can set home location on any group owned parcel
		public static final long AllowSetHome = 1L << 28;

		// Parcel access
		// Can modify public access settings for group owned parcels
		public static final long LandManageAllowed = 1L << 29;
		// Can manager parcel ban lists on group owned land
		public static final long LandManageBanned = 1L << 30;
		// Can manage pass list sales information
		public static final long LandManagePasses = 1L << 31;
		// Can eject and freeze other avatars on group owned land
		public static final long LandEjectAndFreeze = 1L << 32;

		// Parcel content
		// Can return objects set to group
		public static final long ReturnGroupSet = 1L << 33;
		// Can return non-group owned/set objects
		public static final long ReturnNonGroup = 1L << 34;
		// Can return group owned objects
		public static final long ReturnGroupOwned = 1L << 48;

		// Can landscape using Linden plants
		public static final long LandGardening = 1L << 35;

		// Object Management
		// Can deed objects to group
		public static final long DeedObject = 1L << 36;
		// Can move group owned objects
		public static final long ObjectManipulate = 1L << 38;
		// Can set group owned objects for-sale
		public static final long ObjectSetForSale = 1L << 39;

		// Accounting
		// Pay group liabilities and receive group dividends
		public static final long Accountable = 1L << 40;

		// List and Host group events
		public static final long HostEvent = 1L << 41;

		// Notices and proposals
		// Can send group notices
		public static final long SendNotices = 1L << 42;
		// Can receive group notices
		public static final long ReceiveNotices = 1L << 43;
		// Can create group proposals
		public static final long StartProposal = 1L << 44;
		// Can vote on group proposals
		public static final long VoteOnProposal = 1L << 45;

		// Experiences
		// Has admin rights to any experiences owned by this group
		public static final long ExperienceAdmin = 1L << 49;
		// Can sign scripts for experiences owned by this group
		public static final long ExperienceCreator = 1L << 50;

		// Group Banning
		// Allows access to ban / un-ban agents from a group
		public static final long GroupBanAccess = 1L << 51;

		public static long setValue(long value) {
			return value & _mask;
		}

		public static long getValue(long value) {
			return value & _mask;
		}

		private static final long _mask = 0x3FFFFFFFFFFFL;
	}

	// Ban actions available for group members
	public enum GroupBanAction {
		// Ban agent from joining a group
		Ban(1),
		// Remove restriction on agent jointing a group
		Unban(2);

		public static GroupBanAction setValue(int value) {
			for (GroupBanAction e : values()) {
				if (e.val == value)
					return e;
			}
			return Ban;
		}

		public int getValue() {
			return val;
		}

		private int val;

		private GroupBanAction(int value) {
			val = value;
		}
	}

	// #endregion Enums

	public UUID getID();
}
