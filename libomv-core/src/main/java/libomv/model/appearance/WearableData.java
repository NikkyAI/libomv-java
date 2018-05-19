package libomv.model.appearance;

import libomv.assets.AssetWearable;
import libomv.assets.AssetWearable.WearableType;
import libomv.model.asset.AssetType;
import libomv.types.UUID;

// Contains information about a wearable inventory item
public class WearableData {
	// Inventory ItemID of the wearable</summary>
	public UUID ItemID;
	// AssetID of the wearable asset</summary>
	public UUID AssetID;
	// WearableType of the wearable</summary>
	public WearableType WearableType;
	// AssetType of the wearable</summary>
	public AssetType AssetType;
	// Asset data for the wearable</summary>
	public AssetWearable Asset;

	@Override
	public String toString() {
		return String.format("ItemID: %s, AssetID: %s, WearableType: %s, AssetType: %s, Asset: %s", ItemID, AssetID,
				WearableType, AssetType, Asset != null ? Asset.name : "(null)");
	}
}