package libomv.model.grid;

import libomv.utils.CallbackArgs;

public class GridRegionCallbackArgs implements CallbackArgs {
	private final GridRegion region;

	public GridRegionCallbackArgs(GridRegion region) {
		this.region = region;
	}

	public final GridRegion getRegion() {
		return region;
	}

}