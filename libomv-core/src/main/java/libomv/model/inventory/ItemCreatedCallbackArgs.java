package libomv.model.inventory;

import libomv.inventory.InventoryItem;
import libomv.utils.CallbackArgs;

/**
 * Callback for inventory item creation finishing
 *
 * @param success
 *            Whether the request to create an inventory item succeeded or not
 * @param item
 *            Inventory item being created. If success is false this will be
 *            null
 */
public class ItemCreatedCallbackArgs implements CallbackArgs {
	boolean success;
	InventoryItem item;

	public boolean getSuccess() {
		return success;
	}

	public InventoryItem getInventoryItem() {
		return item;
	}

	public ItemCreatedCallbackArgs(boolean success, InventoryItem item) {
		this.success = success;
		this.item = item;
	}
}