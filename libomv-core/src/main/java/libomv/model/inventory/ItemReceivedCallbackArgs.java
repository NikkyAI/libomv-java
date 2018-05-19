package libomv.model.inventory;

import libomv.inventory.InventoryItem;
import libomv.utils.CallbackArgs;

public class ItemReceivedCallbackArgs implements CallbackArgs {
	private final InventoryItem m_Item;

	public final InventoryItem getItem() {
		return m_Item;
	}

	public ItemReceivedCallbackArgs(InventoryItem item) {
		this.m_Item = item;
	}
}