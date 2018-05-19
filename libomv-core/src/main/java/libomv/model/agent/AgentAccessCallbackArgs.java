package libomv.model.agent;

import libomv.utils.CallbackArgs;

// Contains the transaction summary when an item is purchased, money is
// given, or land is purchased
public class AgentAccessCallbackArgs implements CallbackArgs {
	final String newLevel;
	final boolean success;

	public AgentAccessCallbackArgs(String newLevel, boolean success) {
		this.newLevel = newLevel;
		this.success = success;
	}

	// New maturity accesss level returned from the sim
	public String getNewLevel() {
		return newLevel;
	}

	public boolean getSuccess() {
		return success;
	};

}