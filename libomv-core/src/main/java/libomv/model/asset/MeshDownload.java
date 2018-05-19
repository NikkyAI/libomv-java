package libomv.model.asset;

import libomv.types.UUID;
import libomv.utils.CallbackHandler;

// TODO:FIXME
// Changing several fields to public, they need getters instead!
public class MeshDownload extends Transfer {
	public UUID ItemID;
	public CallbackHandler<MeshDownload> callbacks; // private

	public MeshDownload() {
		super();
	}
}