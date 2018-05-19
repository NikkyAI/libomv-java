package libomv.model.grid;

import java.util.List;

import libomv.utils.CallbackArgs;

public class GridItemsCallbackArgs implements CallbackArgs {
	private final GridItemType type;
	private final List<MapItem> items;

	public GridItemsCallbackArgs(GridItemType type, List<MapItem> items) {
		this.type = type;
		this.items = items;
	}

	public final GridItemType getType() {
		return type;
	}

	public final List<MapItem> getItems() {
		return items;
	}

}