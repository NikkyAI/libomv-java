package libomv.model.inventory;

import libomv.inventory.InventoryItem;
import libomv.utils.CallbackArgs;

public class ItemReceivedCallbackArgs implements CallbackArgs {
	private final InventoryItem item;

	public ItemReceivedCallbackArgs(InventoryItem item) {
		this.item = item;
	}

	public final InventoryItem getItem() {
		return item;
	}

}