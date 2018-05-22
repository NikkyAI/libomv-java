package libomv.model;

import libomv.types.UUID;

// Original code came from InventoryManager
public class Inventory {

	/* Used for converting shadow_id to asset_id */
	public static final UUID MAGIC_ID = new UUID("3c115e51-04f4-523c-9fa6-98aff1034730");

	private Inventory() {
	}

	/**
	 * Reverses a cheesy XORing with a fixed UUID to convert a shadow_id to an
	 * asset_id
	 *
	 * @param shadowID
	 *            Obfuscated shadow_id value
	 * @return Deobfuscated asset_id value
	 */
	public static UUID decryptShadowID(UUID shadowID) {
		UUID uuid = new UUID(shadowID);
		uuid.xor(MAGIC_ID);
		return uuid;
	}

	/**
	 * Does a cheesy XORing with a fixed UUID to convert an asset_id to a shadow_id
	 *
	 * @param assetID
	 *            asset_id value to obfuscate
	 * @return Obfuscated shadow_id value
	 */
	public static UUID encryptAssetID(UUID assetID) {
		UUID uuid = new UUID(assetID);
		uuid.xor(MAGIC_ID);
		return uuid;
	}

}
