package libomv.model.inventory;

import libomv.model.agent.InstantMessage;
import libomv.model.asset.AssetType;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class InventoryObjectOfferedCallbackArgs implements CallbackArgs {
	private final InstantMessage m_Offer;
	private final AssetType m_AssetType;
	private final UUID m_ObjectID;
	private final boolean m_FromTask;
	private boolean m_Accept;
	private UUID m_FolderID;

	/*
	 * Set to true to accept offer, false to decline it
	 */
	public final boolean getAccept() {
		return m_Accept;
	}

	public final void setAccept(boolean value) {
		m_Accept = value;
	}

	/*
	 * The folder to accept the inventory into, if null default folder for <see
	 * cref="AssetType"/> will be used
	 */
	public final UUID getFolderID() {
		return m_FolderID;
	}

	public final void setFolderID(UUID value) {
		m_FolderID = value;
	}

	public final InstantMessage getOffer() {
		return m_Offer;
	}

	public final AssetType getAssetType() {
		return m_AssetType;
	}

	public final UUID getObjectID() {
		return m_ObjectID;
	}

	public final boolean getFromTask() {
		return m_FromTask;
	}

	public InventoryObjectOfferedCallbackArgs(InstantMessage offerDetails, AssetType type, UUID objectID,
			boolean fromTask, UUID folderID) {
		this.m_Accept = false;
		this.m_FolderID = folderID;
		this.m_Offer = offerDetails;
		this.m_AssetType = type;
		this.m_ObjectID = objectID;
		this.m_FromTask = fromTask;
	}
}