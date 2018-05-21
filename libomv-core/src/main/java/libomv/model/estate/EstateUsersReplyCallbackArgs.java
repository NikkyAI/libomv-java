package libomv.model.estate;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()
 */
public class EstateUsersReplyCallbackArgs implements CallbackArgs {
	private final int estateID;
	private final int count;
	private final List<UUID> allowedUsers;

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
	public EstateUsersReplyCallbackArgs(int estateID, int count, List<UUID> allowedUsers) {
		this.estateID = estateID;
		this.count = count;
		this.allowedUsers = allowedUsers;
	}

	// The identifier of the estate
	public int getEstateID() {
		return estateID;
	}

	// The number of returned items
	public int getCount() {
		return count;
	}

	// List of UUIDs of Allowed Users
	public List<UUID> getAllowedUsers() {
		return allowedUsers;
	}

}