package libomv.model.estate;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()
 */
public class EstateBansReplyCallbackArgs implements CallbackArgs {
	private final int m_estateID;
	private final int m_count;
	private final ArrayList<UUID> m_banned;

	// The identifier of the estate
	public int getEstateID() {
		return m_estateID;
	}

	// The number of returned items
	public int getCount() {
		return m_count;
	}

	// List of UUIDs of Banned Users
	public ArrayList<UUID> getBanned() {
		return m_banned;
	}

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
		this.m_estateID = estateID;
		this.m_count = count;
		this.m_banned = banned;
	}
}