package libomv.model.estate;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()
 */
public class EstateUsersReplyCallbackArgs implements CallbackArgs {
	private final int m_estateID;
	private final int m_count;
	private final ArrayList<UUID> m_allowedUsers;

	// The identifier of the estate
	public int getEstateID() {
		return m_estateID;
	}

	// The number of returned items
	public int getCount() {
		return m_count;
	}

	// List of UUIDs of Allowed Users
	public ArrayList<UUID> getAllowedUsers() {
		return m_allowedUsers;
	}

	/**
	 * Construct a new instance of the EstateUsersReplyEventArgs class
	 *
	 * @param estateID
	 *            The estate's identifier on the grid
	 * @param count
	 *            The number of returned users in LandStatReply
	 * @param allowedUsers
	 *            Allowed users UUIDs
	 */
	public EstateUsersReplyCallbackArgs(int estateID, int count, ArrayList<UUID> allowedUsers) {
		this.m_estateID = estateID;
		this.m_count = count;
		this.m_allowedUsers = allowedUsers;
	}
}