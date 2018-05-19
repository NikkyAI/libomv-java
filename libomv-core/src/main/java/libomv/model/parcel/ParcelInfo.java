package libomv.model.parcel;

import libomv.types.UUID;

// Some information about a parcel of land returned from a DirectoryManager
// search
public final class ParcelInfo {
	// Global Key of record
	public UUID id;
	// Parcel Owners {@link UUID}
	public UUID ownerID;
	// Name field of parcel, limited to 128 characters
	public String name;
	// Description field of parcel, limited to 256 characters
	public String description;
	// Total Square meters of parcel
	public int actualArea;
	// Total area billable as Tier, for group owned land this will be 10%
	// less than ActualArea
	public int billableArea;
	// True of parcel is in Mature simulator
	public boolean mature;
	// Grid global X position of parcel
	public float globalX;
	// Grid global Y position of parcel
	public float globalY;
	// Grid global Z position of parcel (not used)
	public float globalZ;
	// Name of simulator parcel is located in
	public String simName;
	// Texture {@link T:OpenMetaverse.UUID} of parcels display picture
	public UUID snapshotID;
	// Float representing calculated traffic based on time spent on parcel
	// by avatars
	public float dwell;
	// Sale price of parcel (not used)
	public int salePrice;
	// Auction ID of parcel
	public int auctionID;
}
