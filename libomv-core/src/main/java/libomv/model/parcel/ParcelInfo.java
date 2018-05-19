package libomv.model.parcel;

import libomv.types.UUID;

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