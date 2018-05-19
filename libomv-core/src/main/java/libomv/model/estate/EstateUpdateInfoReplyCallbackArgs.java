package libomv.model.estate;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Returned, along with other info, upon a successful .RequestInfo()
 */
public class EstateUpdateInfoReplyCallbackArgs implements CallbackArgs {
	private final int estateID;
	private final boolean denyNoPaymentInfo;
	private final String estateName;
	private final UUID estateOwner;

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
		this.estateName = estateName;
		this.estateOwner = estateOwner;
		this.estateID = estateID;
		this.denyNoPaymentInfo = denyNoPaymentInfo;

	}

	// The estate's name
	public String getEstateName() {
		return estateName;
	}

	// The Estate Owner's ID (can be a GroupID)
	public UUID getEstateOwner() {
		return estateOwner;
	}

	// The identifier of the estate on the grid
	public int getEstateID() {
		return estateID;
	}

	public boolean getDenyNoPaymentInfo() {
		return denyNoPaymentInfo;
	}

}