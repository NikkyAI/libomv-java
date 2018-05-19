package libomv.model.directory;

import java.util.Date;

import libomv.types.UUID;
import libomv.utils.Helpers;

/** A classified ad on the grid */
public final class Classified {
	/**
	 * UUID for this ad, useful for looking up detailed information about it
	 */
	public UUID id;
	/** The title of this classified ad */
	public String name;
	/** Flags that show certain options applied to the classified */
	public byte flags;
	/** Creation date of the ad */
	public Date creationDate;
	/** Expiration date of the ad */
	public Date expirationDate;
	/** Price that was paid for this ad */
	public int price;

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