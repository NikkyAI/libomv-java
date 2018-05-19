package libomv.model.estate;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()</summary>
 */
public class EstateManagersReplyCallbackArgs implements CallbackArgs {
	private final int m_estateID;
	private final int m_count;
	private final ArrayList<UUID> m_Managers;

	// The identifier of the estate
	public int getEstateID() {
		return m_estateID;
	}

	// The number of returned items
	public int getCount() {
		return m_count;
	}

	// List of UUIDs of the Estate's Managers
	public ArrayList<UUID> getManagers() {
		return m_Managers;
	}

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
	public EstateManagersReplyCallbackArgs(int estateID, int count, ArrayList<UUID> managers) {
		this.m_estateID = estateID;
		this.m_count = count;
		this.m_Managers = managers;
	}
}