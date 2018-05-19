package libomv.model.grid;

import libomv.utils.CallbackArgs;

public class GridListUpdateCallbackArgs implements CallbackArgs {
	private GridListUpdate operation;
	private GridInfo info;

	public GridListUpdateCallbackArgs(GridListUpdate operation, GridInfo info) {
		this.operation = operation;
		this.info = info;
	}

	public GridListUpdate getOperation() {
		return operation;
	}

	public GridInfo getGridInfo() {
		return info;
	}

}