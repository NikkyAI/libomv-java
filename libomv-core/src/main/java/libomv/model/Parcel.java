package libomv.model;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import libomv.capabilities.CapsMessage.ParcelPropertiesUpdateMessage;
import libomv.io.SimulatorManager;
import libomv.io.capabilities.CapsClient;
import libomv.model.parcel.LandingTypeEnum;
import libomv.model.parcel.ParcelAccessEntry;
import libomv.model.parcel.ParcelCategory;
import libomv.model.parcel.ParcelMedia;
import libomv.model.parcel.ParcelStatus;
import libomv.packets.ParcelPropertiesUpdatePacket;
import libomv.packets.ParcelSetOtherCleanTimePacket;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

//Parcel information retrieved from a simulator
public class Parcel {
	// The total number of contiguous 4x4 meter blocks your agent owns
	// within this parcel
	public int selfCount;
	// The total number of contiguous 4x4 meter blocks contained in this
	// parcel owned by a group or agent other than your own
	public int otherCount;
	// Deprecated, Value appears to always be 0
	public int publicCount;
	// Simulator-local ID of this parcel
	public int localID;
	// UUID of the owner of this parcel
	public UUID ownerID;
	// Whether the land is deeded to a group or not
	public boolean isGroupOwned;
	//
	public int auctionID;
	// Date land was claimed
	public Date claimDate;
	// Appears to always be zero
	public int claimPrice;
	// This field is no longer used
	public int rentPrice;
	// Minimum corner of the axis-aligned bounding box for this
	// Tangible_doc_comment_body parcel
	public Vector3 aabbMin;
	// Maximum corner of the axis-aligned bounding box for this
	// Tangible_doc_comment_body parcel
	public Vector3 aabbMax;
	// Bitmap describing land layout in 4x4m squares across the
	// Tangible_doc_comment_body entire region
	public byte[] bitmap;
	// Total parcel land area
	public int area;
	//
	public ParcelStatus status;
	// Maximum primitives across the entire simulator owned by the same
	// agent or group that owns this parcel that can be used
	public int simWideMaxPrims;
	// Total primitives across the entire simulator calculated by combining
	// the allowed prim counts for each parcel
	// Tangible_doc_comment_body owned by the agent or group that owns this
	// parcel
	public int simWideTotalPrims;
	// Maximum number of primitives this parcel supports
	public int maxPrims;
	// Total number of primitives on this parcel
	public int totalPrims;
	// For group-owned parcels this indicates the total number of prims
	// deeded to the group,
	// for parcels owned by an individual this inicates the number of prims
	// owned by the individual
	public int ownerPrims;
	// Total number of primitives owned by the parcel group on this parcel,
	// or for parcels owned by an individual with a group set the total
	// number of prims set to that group.
	public int groupPrims;
	// Total number of prims owned by other avatars that are not set to
	// group, or not the parcel owner
	public int otherPrims;
	// A bonus multiplier which allows parcel prim counts to go over times
	// this amount, this does not affect
	// the max prims per simulator. e.g: 117 prim parcel limit x 1.5 bonus =
	// 175 allowed
	public float parcelPrimBonus;
	// Autoreturn value in minutes for others' objects
	public int otherCleanTime;
	//
	public int flags;
	// Sale price of the parcel, only useful if ForSale is set
	// The SalePrice will remain the same after an ownership transfer
	// (sale), so it can be used to
	// see the purchase price after a sale if the new owner has not changed
	// it
	public int salePrice;
	// Parcel Name
	public String name;
	// Parcel Description
	public String desc;
	// URL For Music Stream
	public String musicURL;
	//
	public UUID groupID;
	// Price for a temporary pass
	public int passPrice;
	// How long is pass valid for
	public float passHours;
	//
	public ParcelCategory category;
	// Key of authorized buyer
	public UUID authBuyerID;
	// Key of parcel snapshot
	public UUID snapshotID;
	// The landing point location
	public Vector3 userLocation;
	// The landing point LookAt
	public Vector3 userLookAt;
	// The type of landing enforced from the <see cref="LandingType"/> enum
	public LandingTypeEnum landing;
	//
	public float dwell;
	//
	public boolean regionDenyAnonymous;
	//
	public boolean regionPushOverride;
	// Access list of who is whitelisted on this
	// Tangible_doc_comment_body parcel
	public List<ParcelAccessEntry> accessWhiteList;
	// Access list of who is blacklisted on this
	// Tangible_doc_comment_body parcel
	public List<ParcelAccessEntry> accessBlackList;
	// TRUE of region denies access to age unverified users
	public boolean regionDenyAgeUnverified;
	// true to obscure (hide) media url
	public boolean obscureMedia;
	// true to obscure (hide) music url
	public boolean obscureMusic;
	// A struct containing media details
	public ParcelMedia media;
	// true if avatars in this parcel should be invisible to people outside
	public boolean seeAVs;
	// true if avatars outside can hear any sounds avatars inside play
	public boolean anyAVSounds;
	// true if group members outside can hear any sounds avatars inside play
	public boolean groupAVSounds;

	/**
	 * Default constructor
	 *
	 * @param localID
	 *            Local ID of this parcel
	 */
	public Parcel(int localID) {
		this.localID = localID;
		this.claimDate = Helpers.Epoch;
		this.bitmap = Helpers.EmptyBytes;
		this.name = Helpers.EmptyString;
		this.desc = Helpers.EmptyString;
		this.musicURL = Helpers.EmptyString;
		this.accessWhiteList = new ArrayList<>(0);
		this.accessBlackList = new ArrayList<>(0);
		this.media = new ParcelMedia();
	}

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
	 * Update the simulator with any local changes to this Parcel object
	 *
	 * @param simulator
	 *            Simulator to send updates to
	 * @param wantReply
	 *            Whether we want the simulator to confirm the update with a reply
	 *            packet or not
	 * @throws Exception
	 */
	public final void update(SimulatorManager simulator, boolean wantReply) throws Exception {
		URI url = simulator.getClient().network.getCapabilityURI("ParcelPropertiesUpdate");
		if (url != null) {
			ParcelPropertiesUpdateMessage req = simulator.getClient().messages.new ParcelPropertiesUpdateMessage();
			req.authBuyerID = this.authBuyerID;
			req.category = this.category;
			req.desc = this.desc;
			req.groupID = this.groupID;
			req.landingType = this.landing;
			req.localID = this.localID;
			req.mediaAutoScale = this.media.mediaAutoScale;
			req.mediaDesc = this.media.mediaDesc;
			req.mediaHeight = this.media.mediaHeight;
			req.mediaID = this.media.mediaID;
			req.mediaLoop = this.media.mediaLoop;
			req.mediaType = this.media.mediaType;
			req.mediaURL = this.media.mediaURL;
			req.mediaWidth = this.media.mediaWidth;
			req.musicURL = this.musicURL;
			req.name = this.name;
			req.obscureMedia = this.obscureMedia;
			req.obscureMusic = this.obscureMusic;
			req.parcelFlags = this.flags;
			req.passHours = this.passHours;
			req.passPrice = this.passPrice;
			req.salePrice = this.salePrice;
			req.snapshotID = this.snapshotID;
			req.userLocation = this.userLocation;
			req.userLookAt = this.userLookAt;
			req.seeAVs = this.seeAVs;
			req.anyAVSounds = this.anyAVSounds;
			req.groupAVSounds = this.groupAVSounds;

			new CapsClient(simulator.getClient(), "UpdateParcel").executeHttpPost(url, req, null,
					simulator.getClient().settings.CAPS_TIMEOUT);
		} else {
			ParcelPropertiesUpdatePacket request = new ParcelPropertiesUpdatePacket();

			request.AgentData.AgentID = simulator.getClient().agent.getAgentID();
			request.AgentData.SessionID = simulator.getClient().agent.getSessionID();

			request.ParcelData.LocalID = this.localID;

			request.ParcelData.AuthBuyerID = this.authBuyerID;
			request.ParcelData.Category = this.category.getValue();
			request.ParcelData.setDesc(Helpers.stringToBytes(this.desc));
			request.ParcelData.GroupID = this.groupID;
			request.ParcelData.LandingType = this.landing.getValue();
			request.ParcelData.MediaAutoScale = this.media.mediaAutoScale ? (byte) 0x1 : (byte) 0x0;
			request.ParcelData.MediaID = this.media.mediaID;
			request.ParcelData.setMediaURL(Helpers.stringToBytes(this.media.mediaURL.toString()));
			request.ParcelData.setMusicURL(Helpers.stringToBytes(this.musicURL.toString()));
			request.ParcelData.setName(Helpers.stringToBytes(this.name));
			if (wantReply) {
				request.ParcelData.Flags = 1;
			}
			request.ParcelData.ParcelFlags = this.flags;
			request.ParcelData.PassHours = this.passHours;
			request.ParcelData.PassPrice = this.passPrice;
			request.ParcelData.SalePrice = this.salePrice;
			request.ParcelData.SnapshotID = this.snapshotID;
			request.ParcelData.UserLocation = this.userLocation;
			request.ParcelData.UserLookAt = this.userLookAt;

			simulator.sendPacket(request);
		}
		updateOtherCleanTime(simulator);
	}

	/**
	 * Set Autoreturn time
	 *
	 * @param simulator
	 *            Simulator to send the update to
	 * @throws Exception
	 */
	public final void updateOtherCleanTime(SimulatorManager simulator) throws Exception {
		ParcelSetOtherCleanTimePacket request = new ParcelSetOtherCleanTimePacket();
		request.AgentData.AgentID = simulator.getClient().agent.getAgentID();
		request.AgentData.SessionID = simulator.getClient().agent.getSessionID();
		request.ParcelData.LocalID = this.localID;
		request.ParcelData.OtherCleanTime = this.otherCleanTime;

		simulator.sendPacket(request);
	}
}
