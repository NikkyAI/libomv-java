package libomv.model.estate;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()</summary>
 */
public class EstateManagersReplyCallbackArgs implements CallbackArgs {
	private final int estateID;
	private final int count;
	private final List<UUID> managers;

	/**
	 * Construct a new instance of the EstateManagersReplyEventArgs class
	 *
	 * @param estateID
	 *            The estate's identifier on the grid
	 * @param count
	 *            The number of returned managers in LandStatReply
	 * @param managers
	 *            Managers UUIDs
	 */
	public EstateManagersReplyCallbackArgs(int estateID, int count, List<UUID> managers) {
		this.estateID = estateID;
		this.count = count;
		this.managers = managers;
	}

	// The identifier of the estate
	public int getEstateID() {
		return estateID;
	}

	// The number of returned items
	public int getCount() {
		return count;
	}

	// List of UUIDs of the Estate's Managers
	public List<UUID> getManagers() {
		return managers;
	}

}