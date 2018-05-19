package libomv.model.asset;

import libomv.model.Simulator;
import libomv.utils.CallbackHandler;

// TODO:FIXME
// Changing several fields to public, they need getters instead!
public class AssetDownload extends Transfer {
	public ChannelType Channel;
	public SourceType Source;
	public TargetType Target;
	public StatusCode Status;
	public float Priority;
	public Simulator Simulator; // private
	public CallbackHandler<AssetDownload> callbacks; // private

	public AssetDownload() {
		super();
	}

	public boolean gotInfo() {
		return Size > 0;
	}
}