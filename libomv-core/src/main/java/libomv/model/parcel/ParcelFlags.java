package libomv.model.parcel;

/** Various parcel properties */
public class ParcelFlags {
	// No flags set
	public static final int None = 0;
	// Allow avatars to fly = a client-side only restriction)
	public static final int AllowFly = 1 << 0;
	// Allow foreign scripts to run
	public static final int AllowOtherScripts = 1 << 1;
	// This parcel is for sale
	public static final int ForSale = 1 << 2;
	// Allow avatars to create a landmark on this parcel
	public static final int AllowLandmark = 1 << 3;
	// Allows all avatars to edit the terrain on this parcel
	public static final int AllowTerraform = 1 << 4;
	// Avatars have health and can take damage on this parcel.
	// If set, avatars can be killed and sent home here
	public static final int AllowDamage = 1 << 5;
	// Foreign avatars can create objects here
	public static final int CreateObjects = 1 << 6;
	// All objects on this parcel can be purchased
	public static final int ForSaleObjects = 1 << 7;
	// Access is restricted to a group
	public static final int UseAccessGroup = 1 << 8;
	// Access is restricted to a whitelist
	public static final int UseAccessList = 1 << 9;
	// Ban blacklist is enabled
	public static final int UseBanList = 1 << 10;
	// Unknown
	public static final int UsePassList = 1 << 11;
	// List this parcel in the search directory
	public static final int ShowDirectory = 1 << 12;
	// Allow personally owned parcels to be deeded to group
	public static final int AllowDeedToGroup = 1 << 13;
	// If Deeded, owner contributes required tier to group parcel is deeded
	// to
	public static final int ContributeWithDeed = 1 << 14;
	// Restrict sounds originating on this parcel to the
	// parcel boundaries
	public static final int SoundLocal = 1 << 15;
	// Objects on this parcel are sold when the land is purchsaed
	public static final int SellParcelObjects = 1 << 16;
	// Allow this parcel to be published on the web
	public static final int AllowPublish = 1 << 17;
	// The information for this parcel is mature content
	public static final int MaturePublish = 1 << 18;
	// The media URL is an HTML page
	public static final int UrlWebPage = 1 << 19;
	// The media URL is a raw HTML string
	public static final int UrlRawHtml = 1 << 20;
	// Restrict foreign object pushes
	public static final int RestrictPushObject = 1 << 21;
	// Ban all non identified/transacted avatars
	public static final int DenyAnonymous = 1 << 22;
	// Ban all identified avatars [OBSOLETE]</summary>
	// [Obsolete]
	// This was obsoleted in 1.19.0 but appears to be recycled and is used
	// on linden homes parcels
	public static final int LindenHome = 1 << 23;
	// Ban all transacted avatars [OBSOLETE]</summary>
	// [Obsolete]
	// DenyTransacted = 1 << 24;
	// Allow group-owned scripts to run
	public static final int AllowGroupScripts = 1 << 25;
	// Allow object creation by group members or group objects
	public static final int CreateGroupObjects = 1 << 26;
	// Allow all objects to enter this parcel
	public static final int AllowAPrimitiveEntry = 1 << 27;
	// Only allow group and owner objects to enter this parcel
	public static final int AllowGroupObjectEntry = 1 << 28;
	// Voice Enabled on this parcel
	public static final int AllowVoiceChat = 1 << 29;
	// Use Estate Voice channel for Voice on this parcel
	public static final int UseEstateVoiceChan = 1 << 30;
	// Deny Age Unverified Users
	public static final int DenyAgeUnverified = 1 << 31;

	private static final int _mask = 0xFFFFFFFF;

	private ParcelFlags() {
	}

	public static int setValue(int value) {
		return value & _mask;
	}

	public static int getValue(int value) {
		return value & _mask;
	}

}