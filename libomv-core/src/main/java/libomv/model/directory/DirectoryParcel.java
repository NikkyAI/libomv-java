package libomv.model.directory;

import libomv.types.UUID;
import libomv.utils.Helpers;

/**
 * A parcel retrieved from the dataserver such as results from the "For-Sale"
 * listings or "Places" Search
 */
public final class DirectoryParcel {
	/**
	 * The unique dataserver parcel ID This id is used to obtain additional
	 * information from the entry by using the
	 * <see cref="ParcelManager.InfoRequest"/> method
	 */
	public UUID ID;
	/** A string containing the name of the parcel */
	public String Name;
	/**
	 * The size of the parcel This field is not returned for Places searches
	 */
	public int ActualArea;
	/**
	 * The price of the parcel This field is not returned for Places searches
	 */
	public int SalePrice;
	/** If True, this parcel is flagged to be auctioned */
	public boolean Auction;
	/** If true, this parcel is currently set for sale */
	public boolean ForSale;
	/** Parcel traffic */
	public float Dwell;

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