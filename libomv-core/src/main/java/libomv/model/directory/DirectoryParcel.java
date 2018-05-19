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
	public UUID id;
	/** A string containing the name of the parcel */
	public String name;
	/**
	 * The size of the parcel This field is not returned for Places searches
	 */
	public int actualArea;
	/**
	 * The price of the parcel This field is not returned for Places searches
	 */
	public int salePrice;
	/** If True, this parcel is flagged to be auctioned */
	public boolean auction;
	/** If true, this parcel is currently set for sale */
	public boolean forSale;
	/** Parcel traffic */
	public float dwell;

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