package libomv.model;

import libomv.types.UUID;

public interface Parcel {

	// #region Enums

	/** Type of return to use when returning objects from a parcel */
	// [Flags]
	public static class ObjectReturnType
	{
		//
		public static final byte None = 0;
		// Return objects owned by parcel owner
		public static final byte Owner = 1 << 1;
		// Return objects set to group
		public static final byte Group = 1 << 2;
		// Return objects not owned by parcel owner or set to group
		public static final byte Other = 1 << 3;
		// Return a specific list of objects on parcel
		public static final byte List = 1 << 4;
		// Return objects that are marked for-sale
		public static final byte Sell = 1 << 5;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static int getValue(byte value)
		{
			return value & _mask;
		}

		private static final byte _mask = 0x1F;
	}

	/** Type of teleport landing for a parcel */
	public enum LandingTypeEnum
	{
		// Unset, simulator default
		None,
		// Specific landing point set for this parcel
		LandingPoint,
		// No landing point set, direct teleports enabled for this parcel
		Direct;

		public static LandingTypeEnum setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/** Category parcel is listed in under search */
	public enum ParcelCategory
	{
		// No assigned category
		None(0),
		// Linden Infohub or public area
		Linden(1),
		// Adult themed area
		Adult(2),
		// Arts and Culture
		Arts(3),
		// Business
		Business(4),
		// Educational
		Educational(5),
		// Gaming
		Gaming(6),
		// Hangout or Club
		Hangout(7),
		// Newcomer friendly
		Newcomer(8),
		// Parks and Nature
		Park(9),
		// Residential
		Residential(10),
		// Shopping
		Shopping(11),
		// Not Used?
		Stage(12),
		// Other
		Other(13),
		// Not an actual category, only used for queries
		Any(-1);

		public static ParcelCategory setValue(int value)
		{
			for (ParcelCategory e : values())
			{
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelCategory(int value)
		{
			this._value = (byte) value;
		}
	}

	/** Various parcel properties */
	public static class ParcelFlags
	{
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

		public static int setValue(int value)
		{
			return value & _mask;
		}

		public static int getValue(int value)
		{
			return value & _mask;
		}

		private static final int _mask = 0xFFFFFFFF;
	}

	// Parcel Media Information
	public final class ParcelMedia
	{
		// A byte, if 0x1 viewer should auto scale media to fit object
		public boolean MediaAutoScale;
		// A boolean, if true the viewer should loop the media
		public boolean MediaLoop;
		// The Asset UUID of the Texture which when applied to a primitive will
		// display the media
		public UUID MediaID;
		// A URL which points to any Quicktime supported media type
		public String MediaURL;
		// A description of the media
		public String MediaDesc;
		// An Integer which represents the height of the media
		public int MediaHeight;
		// An integer which represents the width of the media
		public int MediaWidth;
		// A string which contains the mime type of the media
		public String MediaType;
	}

	/** The result of a request for parcel properties */
	public enum ParcelResult
	{
		// No matches were found for the request
		NoData(-1),
		// Request matched a single parcel
		Single(0),
		// Request matched multiple parcels
		Multiple(1);

		public static ParcelResult setValue(int value)
		{
			for (ParcelResult e : values())
			{
				if (e._value == value)
					return e;
			}
			return NoData;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelResult(int value)
		{
			this._value = (byte) value;
		}
	}

	/** Parcel ownership status */
	public enum ParcelStatus
	{
		// Placeholder
		None(-1),
		// Parcel is leased (owned) by an avatar or group
		Leased(0),
		// Parcel is in process of being leased (purchased) by an avatar or
		// group
		LeasePending(1),
		// Parcel has been abandoned back to Governor Linden
		Abandoned(2);

		public static ParcelStatus setValue(int value)
		{
			for (ParcelStatus e : values())
			{
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelStatus(int value)
		{
			this._value = (byte) value;
		}
	}


}
