package libomv.model.directory;

import libomv.types.UUID;
import libomv.utils.Helpers;

/**
 * Parcel information returned from a <see cref="StartPlacesSearch"/> request
 *
 * Represents one of the following:
 *
 * A parcel of land on the grid that has its Show In Search flag set A parcel of
 * land owned by the agent making the request A parcel of land owned by a group
 * the agent making the request is a member of
 *
 * In a request for Group Land, the First record will contain an empty record
 *
 * Note: This is not the same as searching the land for sale data source
 */
public final class PlacesSearchData {
	/** The ID of the Agent of Group that owns the parcel */
	public UUID OwnerID;
	/** The name */
	public String Name;
	/** The description */
	public String Desc;
	/** The Size of the parcel */
	public int ActualArea;
	/**
	 * The billable Size of the parcel, for mainland parcels this will match the
	 * ActualArea field. For Group owned land this will be 10 percent smaller than
	 * the ActualArea. For Estate land this will always be 0
	 */
	public int BillableArea;
	/** Indicates the ForSale status of the parcel */
	public PlacesFlags Flags;
	/** The Gridwide X position */
	public float GlobalX;
	/** The Gridwide Y position */
	public float GlobalY;
	/** The Z position of the parcel, or 0 if no landing point set */
	public float GlobalZ;
	/** The name of the Region the parcel is located in */
	public String SimName;
	/** The Asset ID of the parcels Snapshot texture */
	public UUID SnapshotID;
	/** The calculated visitor traffic */
	public float Dwell;
	/**
	 * The billing product SKU
	 *
	 * Known values are: <list> <item><term>023</term><description>Mainland / Full
	 * Region</description></item> <item><term>024</term><description>Estate / Full
	 * Region</description></item> <item><term>027</term><description>Estate /
	 * Openspace</description></item> <item><term>029</term><description>Estate /
	 * Homestead</description></item> <item><term>129</term><description>Mainland /
	 * Homestead (Linden Owned)</description></item> </list>
	 */
	public String SKU;
	/** No longer used, will always be 0 */
	public int Price;

	/**
	 * Get a SL URL for the parcel
	 *
	 * @return A string, containing a standard SLURL
	 */
	public String toSLurl() {
		float[] values = new float[2];
		Helpers.GlobalPosToRegionHandle(this.GlobalX, this.GlobalY, values);
		return "secondlife://" + this.SimName + "/" + values[0] + "/" + values[1] + "/" + this.GlobalZ;
	}

	/**
	 * Print the struct data as a string
	 *
	 * @return A string containing the field name, and field value
	 */
	@Override
	public String toString() {
		return Helpers.StructToString(this);
	}
}