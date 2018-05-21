package libomv.model.inventory;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class ScriptUpdatedCallbackArgs implements CallbackArgs {
	public boolean success;
	public String message;
	public boolean compiled;
	public List<String> errors;
	public UUID itemID;
	public UUID assetID;

	public ScriptUpdatedCallbackArgs(boolean success, String message, boolean compiled, List<String> errors,
			UUID itemID, UUID assetID) {
		this.success = success;
		this.message = message;
		this.compiled = compiled;
		this.errors = errors;
		this.itemID = itemID;
		this.assetID = assetID;
	}
}