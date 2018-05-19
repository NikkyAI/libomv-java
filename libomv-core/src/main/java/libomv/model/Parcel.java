package libomv.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public interface Parcel {

	// #region Enums

	/** Type of return to use when returning objects from a parcel */
	// [Flags]
	public static class ObjectReturnType {
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

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static int getValue(byte value) {
			return value & _mask;
		}

		private static final byte _mask = 0x1F;
	}

	// TODO:FIXME
	// Rename this to LandingType ;)
	/** Type of teleport landing for a parcel */
	public enum LandingTypeEnum {
		// Unset, simulator default
		None,
		// Specific landing point set for this parcel
		LandingPoint,
		// No landing point set, direct teleports enabled for this parcel
		Direct;

		public static LandingTypeEnum setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/** Category parcel is listed in under search */
	public enum ParcelCategory {
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

		public static ParcelCategory setValue(int value) {
			for (ParcelCategory e : values()) {
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue() {
			return _value;
		}

		private byte _value;

		private ParcelCategory(int value) {
			this._value = (byte) value;
		}
	}

	/** Various parcel properties */
	public static class ParcelFlags {
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

		public static int setValue(int value) {
			return value & _mask;
		}

		public static int getValue(int value) {
			return value & _mask;
		}

		private static final int _mask = 0xFFFFFFFF;
	}

	// Parcel Media Information
	public final class ParcelMedia {
		// A byte, if 0x1 viewer should auto scale media to fit object
		public boolean mediaAutoScale;
		// A boolean, if true the viewer should loop the media
		public boolean mediaLoop;
		// The Asset UUID of the Texture which when applied to a primitive will
		// display the media
		public UUID mediaID;
		// A URL which points to any Quicktime supported media type
		public String mediaURL;
		// A description of the media
		public String mediaDesc;
		// An Integer which represents the height of the media
		public int mediaHeight;
		// An integer which represents the width of the media
		public int mediaWidth;
		// A string which contains the mime type of the media
		public String mediaType;
	}

	/** The result of a request for parcel properties */
	public enum ParcelResult {
		// No matches were found for the request
		NoData(-1),
		// Request matched a single parcel
		Single(0),
		// Request matched multiple parcels
		Multiple(1);

		public static ParcelResult setValue(int value) {
			for (ParcelResult e : values()) {
				if (e._value == value)
					return e;
			}
			return NoData;
		}

		public byte getValue() {
			return _value;
		}

		private byte _value;

		private ParcelResult(int value) {
			this._value = (byte) value;
		}
	}

	/** Parcel ownership status */
	public enum ParcelStatus {
		// Placeholder
		None(-1),
		// Parcel is leased (owned) by an avatar or group
		Leased(0),
		// Parcel is in process of being leased (purchased) by an avatar or
		// group
		LeasePending(1),
		// Parcel has been abandoned back to Governor Linden
		Abandoned(2);

		public static ParcelStatus setValue(int value) {
			for (ParcelStatus e : values()) {
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue() {
			return _value;
		}

		private byte _value;

		private ParcelStatus(int value) {
			this._value = (byte) value;
		}
	}

	/** Blacklist/Whitelist flags used in parcels Access List */
	public enum ParcelAccessFlags {
		// Agent is denied access
		NoAccess,
		// Agent is granted access
		Access;

		public static ParcelAccessFlags setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/**
	 * Flags used in the ParcelAccessListRequest packet to specify whether we want
	 * the access list (whitelist), ban list (blacklist), or both
	 */
	// [Flags]
	public static class AccessList {
		// Request the access list
		public static final byte Access = 0x1;
		// Request the ban list
		public static final byte Ban = 0x2;
		// Request both White and Black lists
		public static final byte Both = 0x3;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static int getValue(byte value) {
			return value & _mask;
		}

		private static final byte _mask = 0x3;
	}

	/**
	 * Sequence ID in ParcelPropertiesReply packets (sent when avatar tries to cross
	 * a parcel border)
	 */
	public enum ParcelPropertiesStatus {
		None(0),
		// Parcel is currently selected
		ParcelSelected(-10000),
		// Parcel restricted to a group the avatar is not a member of
		CollisionNotInGroup(-20000),
		// Avatar is banned from the parcel
		CollisionBanned(-30000),
		// Parcel is restricted to an access list that the avatar is not on
		CollisionNotOnAccessList(-40000),
		// Response to hovering over a parcel
		HoveredOverParcel(-50000);

		public static ParcelPropertiesStatus setValue(int value) {
			for (ParcelPropertiesStatus e : values()) {
				if (e._value == value)
					return e;
			}
			return None;
		}

		public int getValue() {
			return _value;
		}

		private int _value;

		ParcelPropertiesStatus(int value) {
			this._value = value;
		}
	}

	/** The tool to use when modifying terrain levels */
	public enum TerraformAction {
		// Level the terrain
		Level,
		// Raise the terrain
		Raise,
		// Lower the terrain
		Lower,
		// Smooth the terrain
		Smooth,
		// Add random noise to the terrain
		Noise,
		// Revert terrain to simulator default
		Revert;

		public static TerraformAction setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/** The tool size to use when changing terrain levels */
	// [Flags]
	public static class TerraformBrushSize {
		static float LAND_BRUSH_SIZE[] = { 1.0f, 2.0f, 4.0f };

		// Small
		public static final byte Small = 1 << 0;
		// Medium
		public static final byte Medium = 1 << 1;
		// Large
		public static final byte Large = 1 << 2;

		public static byte getIndex(float value) {
			for (byte i = 2; i >= 0; i--) {
				if (value > LAND_BRUSH_SIZE[i]) {
					return i;
				}
			}
			return 0;
		}

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static int getValue(byte value) {
			return value & _mask;
		}

		private static final byte _mask = 0x7;
	}

	/** Reasons agent is denied access to a parcel on the simulator */
	public enum AccessDeniedReason {
		// Agent is not denied, access is granted
		NotDenied,
		// Agent is not a member of the group set for the parcel, or which owns
		// the parcel
		NotInGroup,
		// Agent is not on the parcels specific allow list
		NotOnAllowList,
		// Agent is on the parcels ban list
		BannedFromParcel,
		// Unknown
		NoAccess,
		// Agent is not age verified and parcel settings deny access to non age
		// verified avatars
		NotAgeVerified;

		public static AccessDeniedReason setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/**
	 * Parcel overlay type. This is used primarily for highlighting and coloring
	 * which is why it is a single integer instead of a set of flags
	 *
	 * These values seem to be poorly thought out. The first three bits represent a
	 * single value, not flags. For example Auction (0x05) is not a combination of
	 * OwnedByOther (0x01) and ForSale(0x04). However, the BorderWest and
	 * BorderSouth values are bit flags that get attached to the value stored in the
	 * first three bits. Bits four, five, and six are unused
	 */
	public enum ParcelOverlayType {
		// Public land
		Public(0),
		// Land is owned by another avatar
		OwnedByOther(1),
		// Land is owned by a group
		OwnedByGroup(2),
		// Land is owned by the current avatar
		OwnedBySelf(3),
		// Land is for sale
		ForSale(4),
		// Land is being auctioned
		Auction(5),
		// Land is private
		Private(32),
		// To the west of this area is a parcel border
		BorderWest(64),
		// To the south of this area is a parcel border
		BorderSouth(128);

		public static ParcelOverlayType setValue(int value) {
			for (ParcelOverlayType e : values()) {
				if (e._value == value)
					return e;
			}
			return Public;
		}

		public byte getValue() {
			return _value;
		}

		private byte _value;

		private ParcelOverlayType(int value) {
			this._value = (byte) value;
		}
	}

	/** Parcel Media Command used in ParcelMediaCommandMessage */
	public enum ParcelMediaCommand {
		// Stop the media stream and go back to the first frame
		Stop,
		// Pause the media stream (stop playing but stay on current frame)
		Pause,
		// Start the current media stream playing and stop when the end is
		// reached
		Play,
		// Start the current media stream playing, loop to the beginning
		// when the end is reached and continue to play
		Loop,
		// Specifies the texture to replace with video. If passing the key of a
		// texture,
		// it must be explicitly typecast as a key, not just passed within
		// double quotes.
		Texture,
		// Specifies the movie URL (254 characters max)
		URL,
		// Specifies the time index at which to begin playing
		Time,
		// Specifies a single agent to apply the media command to
		Agent,
		// Unloads the stream. While the stop command sets the texture to the
		// first frame of the
		// movie, unload resets it to the real texture that the movie was
		// replacing.
		Unload,
		// Turn on/off the auto align feature, similar to the auto align
		// checkbox in the parcel
		// media properties.
		// (NOT to be confused with the "align" function in the textures view of
		// the editor!)
		// Takes TRUE or FALSE as parameter.
		AutoAlign,
		// Allows a Web page or image to be placed on a prim (1.19.1 RC0 and
		// later only).
		// Use "text/html" for HTML.
		Type,
		// Resizes a Web page to fit on x, y pixels (1.19.1 RC0 and later only).
		// This might still not be working
		Size,
		// Sets a description for the media being displayed (1.19.1 RC0 and
		// later only).
		Desc;

		public static ParcelMediaCommand setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// #region Structs

	// Some information about a parcel of land returned from a DirectoryManager
	// search
	public final class ParcelInfo {
		// Global Key of record
		public UUID ID;
		// Parcel Owners {@link UUID}
		public UUID OwnerID;
		// Name field of parcel, limited to 128 characters
		public String Name;
		// Description field of parcel, limited to 256 characters
		public String Description;
		// Total Square meters of parcel
		public int ActualArea;
		// Total area billable as Tier, for group owned land this will be 10%
		// less than ActualArea
		public int BillableArea;
		// True of parcel is in Mature simulator
		public boolean Mature;
		// Grid global X position of parcel
		public float GlobalX;
		// Grid global Y position of parcel
		public float GlobalY;
		// Grid global Z position of parcel (not used)
		public float GlobalZ;
		// Name of simulator parcel is located in
		public String SimName;
		// Texture {@link T:OpenMetaverse.UUID} of parcels display picture
		public UUID SnapshotID;
		// Float representing calculated traffic based on time spent on parcel
		// by avatars
		public float Dwell;
		// Sale price of parcel (not used)
		public int SalePrice;
		// Auction ID of parcel
		public int AuctionID;
	}

	public final class ParcelAccessEntry {
		// Agents {@link T:OpenMetaverse.UUID}
		public UUID AgentID;
		//
		public Date Time;
		// Flags for specific entry in white/black lists
		public byte Flags;
	}

	// Owners of primitives on parcel
	public final class ParcelPrimOwners {
		// Prim Owners {@link T:OpenMetaverse.UUID}
		public UUID OwnerID;
		// True of owner is group
		public boolean IsGroupOwned;
		// Total count of prims owned by OwnerID
		public int Count;
		// true of OwnerID is currently online and is not a group
		public boolean OnlineStatus;
		// The date of the most recent prim left by OwnerID
		public Date NewestPrim;
	}

	// #endregion Structs

	// Contains a parcels dwell data returned from the simulator in response to
	// an <see cref="RequestParcelDwell"/>
	public class ParcelDwellReplyCallbackArgs implements CallbackArgs {
		private final UUID m_ParcelID;
		private final int m_LocalID;
		private final float m_Dwell;

		// Get the global ID of the parcel
		public final UUID getParcelID() {
			return m_ParcelID;
		}

		// Get the simulator specific ID of the parcel
		public final int getLocalID() {
			return m_LocalID;
		}

		// Get the calculated dwell
		public final float getDwell() {
			return m_Dwell;
		}

		/**
		 * Construct a new instance of the ParcelDwellReplyCallbackArgs class
		 *
		 * @param parcelID
		 *            The global ID of the parcel
		 * @param localID
		 *            The simulator specific ID of the parcel
		 * @param dwell
		 *            The calculated dwell for the parcel
		 */
		public ParcelDwellReplyCallbackArgs(UUID parcelID, int localID, float dwell) {
			this.m_ParcelID = parcelID;
			this.m_LocalID = localID;
			this.m_Dwell = dwell;
		}
	}

	// Contains basic parcel information data returned from the simulator in
	// response to an <see cref="RequestParcelInfo"/> request
	public class ParcelInfoReplyCallbackArgs implements CallbackArgs {
		private final ParcelInfo m_Parcel;

		// Get the <see cref="ParcelInfo"/> object containing basic parcel info
		public final ParcelInfo getParcel() {
			return m_Parcel;
		}

		/**
		 * Construct a new instance of the ParcelInfoReplyCallbackArgs class
		 *
		 * @param parcel
		 *            The <see cref="ParcelInfo"/> object containing basic parcel info
		 */
		public ParcelInfoReplyCallbackArgs(ParcelInfo parcel) {
			this.m_Parcel = parcel;
		}
	}

	// Contains basic parcel information data returned from the simulator in
	// response to an <see cref="RequestParcelInfo"/> request
	public class ParcelPropertiesCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private Parcel m_Parcel;
		private final ParcelResult m_Result;
		private final int m_SelectedPrims;
		private final int m_SequenceID;
		private final boolean m_SnapSelection;

		// Get the simulator the parcel is located in
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		// Get the <see cref="Parcel"/> object containing the details. If Result
		// is NoData, this object will not contain valid data
		public final Parcel getParcel() {
			return m_Parcel;
		}

		// Get the result of the request
		public final ParcelResult getResult() {
			return m_Result;
		}

		// Get the number of primitieves your agent is currently selecting and
		// or sitting on in this parcel
		public final int getSelectedPrims() {
			return m_SelectedPrims;
		}

		// Get the user assigned ID used to correlate a request with these
		// results
		public final int getSequenceID() {
			return m_SequenceID;
		}

		// TODO:
		public final boolean getSnapSelection() {
			return m_SnapSelection;
		}

		/**
		 * Construct a new instance of the ParcelPropertiesCallbackArgs class
		 *
		 * @param simulator
		 *            The <see cref="Parcel"/> object containing the details
		 * @param parcel
		 *            The <see cref="Parcel"/> object containing the details
		 * @param result
		 *            The result of the request
		 * @param selectedPrims
		 *            The number of primitieves your agent is currently selecting and or
		 *            sitting on in this parcel
		 * @param sequenceID
		 *            The user assigned ID used to correlate a request with these
		 *            results
		 * @param snapSelection
		 *            TODO:
		 */
		public ParcelPropertiesCallbackArgs(Simulator simulator, Parcel parcel, ParcelResult result, int selectedPrims,
				int sequenceID, boolean snapSelection) {
			this.m_Simulator = simulator;
			this.m_Parcel = parcel;
			this.m_Result = result;
			this.m_SelectedPrims = selectedPrims;
			this.m_SequenceID = sequenceID;
			this.m_SnapSelection = snapSelection;
		}
	}

	// Contains blacklist and whitelist data returned from the simulator in
	// response to an <see cref="RequestParcelAccesslist"/> request
	public class ParcelAccessListReplyCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final int m_SequenceID;
		private final int m_LocalID;
		private final int m_Flags;
		private final ArrayList<ParcelAccessEntry> m_AccessList;

		// Get the simulator the parcel is located in
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		// Get the user assigned ID used to correlate a request with these
		// results
		public final int getSequenceID() {
			return m_SequenceID;
		}

		// Get the simulator specific ID of the parcel
		public final int getLocalID() {
			return m_LocalID;
		}

		// TODO:
		public final int getFlags() {
			return m_Flags;
		}

		// Get the list containing the white/blacklisted agents for the parcel
		public final ArrayList<ParcelAccessEntry> getAccessList() {
			return m_AccessList;
		}

		/**
		 * Construct a new instance of the ParcelAccessListReplyCallbackArgs class
		 *
		 * @param simulator
		 *            The simulator the parcel is located in
		 * @param sequenceID
		 *            The user assigned ID used to correlate a request with these
		 *            results
		 * @param localID
		 *            The simulator specific ID of the parcel
		 * @param flags
		 *            TODO:
		 * @param accessEntries
		 *            The list containing the white/blacklisted agents for the parcel
		 */
		public ParcelAccessListReplyCallbackArgs(Simulator simulator, int sequenceID, int localID, int flags,
				ArrayList<ParcelAccessEntry> accessEntries) {
			this.m_Simulator = simulator;
			this.m_SequenceID = sequenceID;
			this.m_LocalID = localID;
			this.m_Flags = flags;
			this.m_AccessList = accessEntries;
		}
	}

	// Contains blacklist and whitelist data returned from the simulator in
	// response to an <see cref="RequestParcelAccesslist"/> request
	public class ParcelObjectOwnersReplyCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final java.util.ArrayList<ParcelPrimOwners> m_Owners;

		// Get the simulator the parcel is located in
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		// Get the list containing prim ownership counts
		public final java.util.ArrayList<ParcelPrimOwners> getPrimOwners() {
			return m_Owners;
		}

		/**
		 * Construct a new instance of the ParcelObjectOwnersReplyCallbackArgs class
		 *
		 * @param simulator
		 *            The simulator the parcel is located in
		 * @param primOwners
		 *            The list containing prim ownership counts
		 */
		public ParcelObjectOwnersReplyCallbackArgs(Simulator simulator,
				java.util.ArrayList<ParcelPrimOwners> primOwners) {
			this.m_Simulator = simulator;
			this.m_Owners = primOwners;
		}
	}

	// Contains the data returned when all parcel data has been retrieved from a
	// simulator
	public class SimParcelsDownloadedCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final HashMap<Integer, Parcel> m_Parcels;
		private final int[] m_ParcelMap;

		// Get the simulator the parcel data was retrieved from
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		// A dictionary containing the parcel data where the key correlates to
		// the ParcelMap entry
		public final HashMap<Integer, Parcel> getParcels() {
			return m_Parcels;
		}

		// Get the multidimensional array containing a x,y grid mapped to each
		// 64x64 parcel's LocalID.
		public final int[] getParcelMap() {
			return m_ParcelMap;
		}

		/**
		 * Construct a new instance of the SimParcelsDownloadedCallbackArgs class
		 *
		 * @param simulator
		 *            The simulator the parcel data was retrieved from
		 * @param simParcels
		 *            The dictionary containing the parcel data
		 * @param is
		 *            The multidimensional array containing a x,y grid mapped to each
		 *            64x64 parcel's LocalID.
		 */
		public SimParcelsDownloadedCallbackArgs(Simulator simulator, HashMap<Integer, Parcel> simParcels, int[] is) {
			this.m_Simulator = simulator;
			this.m_Parcels = simParcels;
			this.m_ParcelMap = is;
		}
	}

	// Contains the data returned when a <see cref="RequestForceSelectObjects"/>
	// request
	public class ForceSelectObjectsReplyCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final int[] m_ObjectIDs;
		private final boolean m_ResetList;

		// Get the simulator the parcel data was retrieved from
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		// Get the list of primitive IDs
		public final int[] getObjectIDs() {
			return m_ObjectIDs;
		}

		// true if the list is clean and contains the information only for a
		// given request
		public final boolean getResetList() {
			return m_ResetList;
		}

		/**
		 * Construct a new instance of the ForceSelectObjectsReplyCallbackArgs class
		 *
		 * @param simulator
		 *            The simulator the parcel data was retrieved from
		 * @param objectIDs
		 *            The list of primitive IDs
		 * @param resetList
		 *            true if the list is clean and contains the information only for a
		 *            given request
		 */
		public ForceSelectObjectsReplyCallbackArgs(Simulator simulator, int[] objectIDs, boolean resetList) {
			this.m_Simulator = simulator;
			this.m_ObjectIDs = objectIDs;
			this.m_ResetList = resetList;
		}
	}

	// Contains data when the media data for a parcel the avatar is on changes
	public class ParcelMediaUpdateReplyCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final ParcelMedia m_ParcelMedia;

		// Get the simulator the parcel media data was updated in
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		// Get the updated media information
		public final ParcelMedia getMedia() {
			return m_ParcelMedia;
		}

		/**
		 * Construct a new instance of the ParcelMediaUpdateReplyCallbackArgs class
		 *
		 * @param simulator
		 *            the simulator the parcel media data was updated in
		 * @param media
		 *            The updated media information
		 */
		public ParcelMediaUpdateReplyCallbackArgs(Simulator simulator, ParcelMedia media) {
			this.m_Simulator = simulator;
			this.m_ParcelMedia = media;
		}
	}

	// Contains the media command for a parcel the agent is currently on
	public class ParcelMediaCommandCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final int m_Sequence;
		private final int m_ParcelFlags;
		private final ParcelMediaCommand m_MediaCommand;
		private final float m_Time;

		// Get the simulator the parcel media command was issued in
		public final Simulator getSimulator() {
			return m_Simulator;
		}

		public final int getSequence() {
			return m_Sequence;
		}

		public final int getParcelFlags() {
			return m_ParcelFlags;
		}

		// Get the media command that was sent
		public final ParcelMediaCommand getMediaCommand() {
			return m_MediaCommand;
		}

		public final float getTime() {
			return m_Time;
		}

		/**
		 * Construct a new instance of the ParcelMediaCommandCallbackArgs class
		 *
		 * @param simulator
		 *            The simulator the parcel media command was issued in
		 * @param sequence
		 * @param flags
		 * @param command
		 *            The media command that was sent
		 * @param time
		 */
		public ParcelMediaCommandCallbackArgs(Simulator simulator, int sequence, int flags, ParcelMediaCommand command,
				float time) {
			this.m_Simulator = simulator;
			this.m_Sequence = sequence;
			this.m_ParcelFlags = flags;
			this.m_MediaCommand = command;
			this.m_Time = time;
		}
	}

}
