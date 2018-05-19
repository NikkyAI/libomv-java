package libomv.model.network;

import java.util.Vector;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * An event triggered when the logout is confirmed
 *
 * An empty itemIDs list indicates a abortion of the logout procedure after the
 * logout timout has expired without receiving any confirmation from the server
 */
public class LoggedOutCallbackArgs implements CallbackArgs {
	private final Vector<UUID> itemIDs;

	public Vector<UUID> getItemIDs() {
		return itemIDs;
	}

	public LoggedOutCallbackArgs(Vector<UUID> itemIDs) {
		this.itemIDs = itemIDs;
	}
}