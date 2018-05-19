package libomv.model.estate;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()
 */
public class EstateBansReplyCallbackArgs implements CallbackArgs {
	private final int estateID;
	private final int count;
	private final ArrayList<UUID> banned;

	/**
	 * Construct a new instance of the EstateBansReplyEventArgs class
	 *
	 * @param estateID
	 *            The estate's identifier on the grid
	 * @param count
	 *            The number of returned items in LandStatReply
	 * @param banned
	 *            User UUIDs banned
	 */
	public EstateBansReplyCallbackArgs(int estateID, int count, ArrayList<UUID> banned) {
		this.estateID = estateID;
		this.count = count;
		this.banned = banned;
	}

	// The identifier of the estate
	public int getEstateID() {
		return estateID;
	}

	// The number of returned items
	public int getCount() {
		return count;
	}

	// List of UUIDs of Banned Users
	public ArrayList<UUID> getBanned() {
		return banned;
	}

}