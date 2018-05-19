package libomv.model.asset;

import libomv.model.Simulator;
import libomv.utils.CallbackHandler;

// TODO:FIXME
// Changing several fields to public, they need getters instead!
public class AssetDownload extends Transfer {
	public ChannelType channel;
	public SourceType source;
	public TargetType target;
	public StatusCode status;
	public float priority;
	public Simulator simulator; // private
	public CallbackHandler<AssetDownload> callbacks; // private

	public AssetDownload() {
		super();
	}

	public boolean gotInfo() {
		return size > 0;
	}
}