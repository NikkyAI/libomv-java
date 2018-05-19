package libomv.model.grid;

import libomv.utils.CallbackArgs;

public class GridLayerCallbackArgs implements CallbackArgs {
	private final GridLayer layer;

	public GridLayerCallbackArgs(GridLayer layer) {
		this.layer = layer;
	}

	public final GridLayer getLayer() {
		return layer;
	}

}