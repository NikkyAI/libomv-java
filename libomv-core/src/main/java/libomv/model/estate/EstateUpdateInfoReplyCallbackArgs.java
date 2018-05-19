package libomv.model.estate;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()
 */
public class EstateUpdateInfoReplyCallbackArgs implements CallbackArgs {
	private final int m_estateID;
	private final boolean m_denyNoPaymentInfo;
	private final String m_estateName;
	private final UUID m_estateOwner;

	// The estate's name
	public String getEstateName() {
		return m_estateName;
	}

	// The Estate Owner's ID (can be a GroupID)
	public UUID getEstateOwner() {
		return m_estateOwner;
	}

	// The identifier of the estate on the grid
	public int getEstateID() {
		return m_estateID;
	}

	public boolean getDenyNoPaymentInfo() {
		return m_denyNoPaymentInfo;
	}

	/**
	 * Construct a new instance of the EstateUpdateInfoReplyEventArgs class
	 *
	 * @param estateName
	 *            The estate's name
	 * @param estateOwner
	 *            The Estate Owners ID (can be a GroupID)
	 * @param estateID
	 *            The estate's identifier on the grid
	 * @param denyNoPaymentInfo
	 */
	public EstateUpdateInfoReplyCallbackArgs(String estateName, UUID estateOwner, int estateID,
			boolean denyNoPaymentInfo) {
		this.m_estateName = estateName;
		this.m_estateOwner = estateOwner;
		this.m_estateID = estateID;
		this.m_denyNoPaymentInfo = denyNoPaymentInfo;

	}
}