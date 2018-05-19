package libomv.model.directory;

import java.util.Date;

import libomv.types.UUID;
import libomv.utils.Helpers;

/** A classified ad on the grid */
public final class Classified {
	/**
	 * UUID for this ad, useful for looking up detailed information about it
	 */
	public UUID ID;
	/** The title of this classified ad */
	public String Name;
	/** Flags that show certain options applied to the classified */
	public byte Flags;
	/** Creation date of the ad */
	public Date CreationDate;
	/** Expiration date of the ad */
	public Date ExpirationDate;
	/** Price that was paid for this ad */
	public int Price;

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