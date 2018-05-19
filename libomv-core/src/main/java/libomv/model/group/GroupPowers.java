package libomv.model.group;

// [Flags]
public class GroupPowers {
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