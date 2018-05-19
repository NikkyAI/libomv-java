package libomv.model.appearance;

import libomv.assets.AssetWearable;
import libomv.assets.AssetWearable.WearableType;
import libomv.model.asset.AssetType;
import libomv.types.UUID;

// Contains information about a wearable inventory item
public class WearableData {
	// Inventory ItemID of the wearable</summary>
	public UUID itemID;
	// AssetID of the wearable asset</summary>
	public UUID assetID;
	// WearableType of the wearable</summary>
	public WearableType wearableType;
	// AssetType of the wearable</summary>
	public AssetType assetType;
	// Asset data for the wearable</summary>
	public AssetWearable asset;

	@Override
	public String toString() {
		return String.format("ItemID: %s, AssetID: %s, WearableType: %s, AssetType: %s, Asset: %s", itemID, assetID,
				wearableType, assetType, asset != null ? asset.name : "(null)");
	}
}