package libomv.model.agent;

import libomv.utils.CallbackArgs;

// Contains the transaction summary when an item is purchased, money is
// given, or land is purchased
public class AgentAccessCallbackArgs implements CallbackArgs {
	final String mNewLevel;
	final boolean mSuccess;

	// New maturity accesss level returned from the sim
	public String getNewLevel() {
		return mNewLevel;
	}

	public boolean getSuccess() {
		return mSuccess;
	};

	public AgentAccessCallbackArgs(String newLevel, boolean success) {
		mNewLevel = newLevel;
		mSuccess = success;
	}
}