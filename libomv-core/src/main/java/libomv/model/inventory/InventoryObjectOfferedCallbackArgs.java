package libomv.model.inventory;

import libomv.model.agent.InstantMessage;
import libomv.model.asset.AssetType;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class InventoryObjectOfferedCallbackArgs implements CallbackArgs {
	private final InstantMessage offer;
	private final AssetType assetType;
	private final UUID objectID;
	private final boolean fromTask;
	private boolean accept;
	private UUID folderID;

	public InventoryObjectOfferedCallbackArgs(InstantMessage offerDetails, AssetType type, UUID objectID,
			boolean fromTask, UUID folderID) {
		this.accept = false;
		this.folderID = folderID;
		this.offer = offerDetails;
		this.assetType = type;
		this.objectID = objectID;
		this.fromTask = fromTask;
	}

	/*
	 * Set to true to accept offer, false to decline it
	 */
	public final boolean getAccept() {
		return accept;
	}

	public final void setAccept(boolean value) {
		accept = value;
	}

	/*
	 * The folder to accept the inventory into, if null default folder for <see
	 * cref="AssetType"/> will be used
	 */
	public final UUID getFolderID() {
		return folderID;
	}

	public final void setFolderID(UUID value) {
		folderID = value;
	}

	public final InstantMessage getOffer() {
		return offer;
	}

	public final AssetType getAssetType() {
		return assetType;
	}

	public final UUID getObjectID() {
		return objectID;
	}

	public final boolean getFromTask() {
		return fromTask;
	}

}