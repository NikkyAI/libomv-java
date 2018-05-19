package libomv.model.asset;

import libomv.types.UUID;

public class AssetUpload extends Transfer {
	public UUID assetID;
	public long xferID;

	public AssetUpload() {
		super();
	}
}