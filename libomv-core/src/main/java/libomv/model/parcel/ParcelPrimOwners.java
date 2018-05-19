package libomv.model.parcel;

import java.util.Date;

import libomv.types.UUID;

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