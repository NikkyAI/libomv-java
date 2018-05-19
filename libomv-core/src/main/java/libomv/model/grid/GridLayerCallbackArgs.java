package libomv.model.grid;

import libomv.utils.CallbackArgs;

public class GridLayerCallbackArgs implements CallbackArgs {
	private final GridLayer m_Layer;

	public final GridLayer getLayer() {
		return m_Layer;
	}

	public GridLayerCallbackArgs(GridLayer layer) {
		this.m_Layer = layer;
	}
}