package libomv.model;

import libomv.types.UUID;

// Original code came from InventoryManager
public interface Inventory {

	/* Used for converting shadow_id to asset_id */
	public static final UUID MAGIC_ID = new UUID("3c115e51-04f4-523c-9fa6-98aff1034730");

	/**
	 * Reverses a cheesy XORing with a fixed UUID to convert a shadow_id to an
	 * asset_id
	 * 
	 * @param shadowID
	 *            Obfuscated shadow_id value
	 * @return Deobfuscated asset_id value
	 */
	public static UUID DecryptShadowID(UUID shadowID)
	{
		UUID uuid = new UUID(shadowID);
		uuid.XOr(MAGIC_ID);
		return uuid;
	}

	/**
	 * Does a cheesy XORing with a fixed UUID to convert an asset_id to a
	 * shadow_id
	 * 
	 * @param assetID
	 *            asset_id value to obfuscate
	 * @return Obfuscated shadow_id value
	 */
	public static UUID EncryptAssetID(UUID assetID)
	{
		UUID uuid = new UUID(assetID);
		uuid.XOr(MAGIC_ID);
		return uuid;
	}

}
