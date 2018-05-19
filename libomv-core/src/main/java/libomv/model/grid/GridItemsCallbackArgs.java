package libomv.model.grid;

import java.util.List;

import libomv.utils.CallbackArgs;

public class GridItemsCallbackArgs implements CallbackArgs {
	private final GridItemType m_Type;
	private final List<MapItem> m_Items;

	public final GridItemType getType() {
		return m_Type;
	}

	public final List<MapItem> getItems() {
		return m_Items;
	}

	public GridItemsCallbackArgs(GridItemType type, List<MapItem> items) {
		this.m_Type = type;
		this.m_Items = items;
	}
}