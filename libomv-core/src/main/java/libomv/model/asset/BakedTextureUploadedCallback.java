package libomv.model.asset;

import libomv.types.UUID;

/**
 * Callback used upon completion of baked texture upload
 *
 * @param newAssetID
 *            Asset UUID of the newly uploaded baked texture
 */
public abstract class BakedTextureUploadedCallback {
	abstract public void callback(UUID newAssetID);
}