package libomv.model.inventory;

import libomv.inventory.InventoryItem;
import libomv.utils.CallbackArgs;

/**
 * Callback for an inventory item copying finished
 *
 * @param item
 *            InventoryItem being copied
 */
public class ItemCopiedCallbackArgs implements CallbackArgs {
	InventoryItem item;

	public InventoryItem getInventoryItem() {
		return item;
	}

	public ItemCopiedCallbackArgs(InventoryItem item) {
		this.item = item;
	}
}