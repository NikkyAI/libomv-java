package libomv.model.agent;

import libomv.utils.CallbackArgs;

public class AlertMessageCallbackArgs implements CallbackArgs {
	private final String alert;

	public AlertMessageCallbackArgs(String alert) {
		this.alert = alert;
	}

	public String getAlert() {
		return alert;
	}

}