package libomv.io.impl;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import libomv.capabilities.CapsMessage.ParcelPropertiesUpdateMessage;
import libomv.io.ParcelManager;
import libomv.io.SimulatorManager;
import libomv.io.capabilities.CapsClient;
import libomv.model.Parcel;
import libomv.packets.ParcelPropertiesUpdatePacket;
import libomv.packets.ParcelSetOtherCleanTimePacket;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

//Parcel information retrieved from a simulator
public class ParcelImpl implements Parcel {
	// The total number of contiguous 4x4 meter blocks your agent owns
	// within this parcel
	public int SelfCount;
	// The total number of contiguous 4x4 meter blocks contained in this
	// parcel owned by a group or agent other than your own
	public int OtherCount;
	// Deprecated, Value appears to always be 0
	public int PublicCount;
	// Simulator-local ID of this parcel
	public int LocalID;
	// UUID of the owner of this parcel
	public UUID OwnerID;
	// Whether the land is deeded to a group or not
	public boolean IsGroupOwned;
	//
	public int AuctionID;
	// Date land was claimed
	public Date ClaimDate;
	// Appears to always be zero
	public int ClaimPrice;
	// This field is no longer used
	public int RentPrice;
	// Minimum corner of the axis-aligned bounding box for this
	// Tangible_doc_comment_body parcel
	public Vector3 AABBMin;
	// Maximum corner of the axis-aligned bounding box for this
	// Tangible_doc_comment_body parcel
	public Vector3 AABBMax;
	// Bitmap describing land layout in 4x4m squares across the
	// Tangible_doc_comment_body entire region
	public byte[] Bitmap;
	// Total parcel land area
	public int Area;
	//
	public ParcelStatus Status;
	// Maximum primitives across the entire simulator owned by the same
	// agent or group that owns this parcel that can be used
	public int SimWideMaxPrims;
	// Total primitives across the entire simulator calculated by combining
	// the allowed prim counts for each parcel
	// Tangible_doc_comment_body owned by the agent or group that owns this
	// parcel
	public int SimWideTotalPrims;
	// Maximum number of primitives this parcel supports
	public int MaxPrims;
	// Total number of primitives on this parcel
	public int TotalPrims;
	// For group-owned parcels this indicates the total number of prims
	// deeded to the group,
	// for parcels owned by an individual this inicates the number of prims
	// owned by the individual
	public int OwnerPrims;
	// Total number of primitives owned by the parcel group on this parcel,
	// or for parcels owned by an individual with a group set the total
	// number of prims set to that group.
	public int GroupPrims;
	// Total number of prims owned by other avatars that are not set to
	// group, or not the parcel owner
	public int OtherPrims;
	// A bonus multiplier which allows parcel prim counts to go over times
	// this amount, this does not affect
	// the max prims per simulator. e.g: 117 prim parcel limit x 1.5 bonus =
	// 175 allowed
	public float ParcelPrimBonus;
	// Autoreturn value in minutes for others' objects
	public int OtherCleanTime;
	//
	public int Flags;
	// Sale price of the parcel, only useful if ForSale is set
	// The SalePrice will remain the same after an ownership transfer
	// (sale), so it can be used to
	// see the purchase price after a sale if the new owner has not changed
	// it
	public int SalePrice;
	// Parcel Name
	public String Name;
	// Parcel Description
	public String Desc;
	// URL For Music Stream
	public String MusicURL;
	//
	public UUID GroupID;
	// Price for a temporary pass
	public int PassPrice;
	// How long is pass valid for
	public float PassHours;
	//
	public ParcelCategory Category;
	// Key of authorized buyer
	public UUID AuthBuyerID;
	// Key of parcel snapshot
	public UUID SnapshotID;
	// The landing point location
	public Vector3 UserLocation;
	// The landing point LookAt
	public Vector3 UserLookAt;
	// The type of landing enforced from the <see cref="LandingType"/> enum
	public LandingTypeEnum Landing;
	//
	public float Dwell;
	//
	public boolean RegionDenyAnonymous;
	//
	public boolean RegionPushOverride;
	// Access list of who is whitelisted on this
	// Tangible_doc_comment_body parcel
	public ArrayList<ParcelManager.ParcelAccessEntry> AccessWhiteList;
	// Access list of who is blacklisted on this
	// Tangible_doc_comment_body parcel
	public ArrayList<ParcelManager.ParcelAccessEntry> AccessBlackList;
	// TRUE of region denies access to age unverified users
	public boolean RegionDenyAgeUnverified;
	// true to obscure (hide) media url
	public boolean ObscureMedia;
	// true to obscure (hide) music url
	public boolean ObscureMusic;
	// A struct containing media details
	public ParcelMedia Media;
	// true if avatars in this parcel should be invisible to people outside
	public boolean SeeAVs;
	// true if avatars outside can hear any sounds avatars inside play
	public boolean AnyAVSounds;
	// true if group members outside can hear any sounds avatars inside play
	public boolean GroupAVSounds;

	/**
	 * Displays a parcel object in string format
	 *
	 * @return string containing key=value pairs of a parcel object
	 */
	@Override
	public String toString() {
		String result = "";
		Class<? extends Parcel> parcelType = this.getClass();
		Field[] fields = parcelType.getFields();
		for (Field field : fields) {
			try {
				result += (field.getName() + " = " + field.get(this) + " ");
			} catch (Exception ex) {
			}
		}
		return result;
	}

	/**
	 * Default constructor
	 *
	 * @param localID
	 *            Local ID of this parcel
	 */
	public ParcelImpl(int localID) {
		LocalID = localID;
		ClaimDate = Helpers.Epoch;
		Bitmap = Helpers.EmptyBytes;
		Name = Helpers.EmptyString;
		Desc = Helpers.EmptyString;
		MusicURL = Helpers.EmptyString;
		AccessWhiteList = new ArrayList<ParcelManager.ParcelAccessEntry>(0);
		AccessBlackList = new ArrayList<ParcelManager.ParcelAccessEntry>(0);
		Media = new ParcelMedia();
	}

	/**
	 * Update the simulator with any local changes to this Parcel object
	 *
	 * @param simulator
	 *            Simulator to send updates to
	 * @param wantReply
	 *            Whether we want the simulator to confirm the update with a reply
	 *            packet or not
	 * @throws Exception
	 */
	public final void Update(SimulatorManager simulator, boolean wantReply) throws Exception {
		URI url = simulator.getClient().Network.getCapabilityURI("ParcelPropertiesUpdate");
		if (url != null) {
			ParcelPropertiesUpdateMessage req = simulator.getClient().Messages.new ParcelPropertiesUpdateMessage();
			req.authBuyerID = this.AuthBuyerID;
			req.category = this.Category;
			req.desc = this.Desc;
			req.groupID = this.GroupID;
			req.landingType = this.Landing;
			req.localID = this.LocalID;
			req.mediaAutoScale = this.Media.mediaAutoScale;
			req.mediaDesc = this.Media.mediaDesc;
			req.mediaHeight = this.Media.mediaHeight;
			req.mediaID = this.Media.mediaID;
			req.mediaLoop = this.Media.mediaLoop;
			req.mediaType = this.Media.mediaType;
			req.mediaURL = this.Media.mediaURL;
			req.mediaWidth = this.Media.mediaWidth;
			req.musicURL = this.MusicURL;
			req.name = this.Name;
			req.obscureMedia = this.ObscureMedia;
			req.obscureMusic = this.ObscureMusic;
			req.parcelFlags = this.Flags;
			req.passHours = this.PassHours;
			req.passPrice = this.PassPrice;
			req.salePrice = this.SalePrice;
			req.snapshotID = this.SnapshotID;
			req.userLocation = this.UserLocation;
			req.userLookAt = this.UserLookAt;
			req.seeAVs = this.SeeAVs;
			req.anyAVSounds = this.AnyAVSounds;
			req.groupAVSounds = this.GroupAVSounds;

			new CapsClient(simulator.getClient(), "UpdateParcel").executeHttpPost(url, req, null,
					simulator.getClient().Settings.CAPS_TIMEOUT);
		} else {
			ParcelPropertiesUpdatePacket request = new ParcelPropertiesUpdatePacket();

			request.AgentData.AgentID = simulator.getClient().Self.getAgentID();
			request.AgentData.SessionID = simulator.getClient().Self.getSessionID();

			request.ParcelData.LocalID = this.LocalID;

			request.ParcelData.AuthBuyerID = this.AuthBuyerID;
			request.ParcelData.Category = this.Category.getValue();
			request.ParcelData.setDesc(Helpers.StringToBytes(this.Desc));
			request.ParcelData.GroupID = this.GroupID;
			request.ParcelData.LandingType = this.Landing.getValue();
			request.ParcelData.MediaAutoScale = (this.Media.mediaAutoScale) ? (byte) 0x1 : (byte) 0x0;
			request.ParcelData.MediaID = this.Media.mediaID;
			request.ParcelData.setMediaURL(Helpers.StringToBytes(this.Media.mediaURL.toString()));
			request.ParcelData.setMusicURL(Helpers.StringToBytes(this.MusicURL.toString()));
			request.ParcelData.setName(Helpers.StringToBytes(this.Name));
			if (wantReply) {
				request.ParcelData.Flags = 1;
			}
			request.ParcelData.ParcelFlags = this.Flags;
			request.ParcelData.PassHours = this.PassHours;
			request.ParcelData.PassPrice = this.PassPrice;
			request.ParcelData.SalePrice = this.SalePrice;
			request.ParcelData.SnapshotID = this.SnapshotID;
			request.ParcelData.UserLocation = this.UserLocation;
			request.ParcelData.UserLookAt = this.UserLookAt;

			simulator.sendPacket(request);
		}
		UpdateOtherCleanTime(simulator);
	}

	/**
	 * Set Autoreturn time
	 *
	 * @param simulator
	 *            Simulator to send the update to
	 * @throws Exception
	 */
	public final void UpdateOtherCleanTime(SimulatorManager simulator) throws Exception {
		ParcelSetOtherCleanTimePacket request = new ParcelSetOtherCleanTimePacket();
		request.AgentData.AgentID = simulator.getClient().Self.getAgentID();
		request.AgentData.SessionID = simulator.getClient().Self.getSessionID();
		request.ParcelData.LocalID = this.LocalID;
		request.ParcelData.OtherCleanTime = this.OtherCleanTime;

		simulator.sendPacket(request);
	}
}
