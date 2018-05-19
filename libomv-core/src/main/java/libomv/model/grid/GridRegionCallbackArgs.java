package libomv.model.grid;

import libomv.utils.CallbackArgs;

public class GridRegionCallbackArgs implements CallbackArgs {
	private final GridRegion m_Region;

	public final GridRegion getRegion() {
		return m_Region;
	}

	public GridRegionCallbackArgs(GridRegion region) {
		this.m_Region = region;
	}
}