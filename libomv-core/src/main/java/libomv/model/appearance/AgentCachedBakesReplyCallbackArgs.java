package libomv.model.appearance;

import libomv.utils.CallbackArgs;

// Raised when an AgentCachedTextureResponse packet is received, giving a list
// of cached bakes that were found
// on the simulator <see cref="RequestCachedBakes"/> request.
public class AgentCachedBakesReplyCallbackArgs implements CallbackArgs {
	private final int serialNum;
	private final int numBakes;

	// Construct a new instance of the AgentCachedBakesReplyEventArgs class
	public AgentCachedBakesReplyCallbackArgs(int serialNum, int numBakes) {
		this.serialNum = serialNum;
		this.numBakes = numBakes;
	}

	public int getSerialNum() {
		return serialNum;
	}

	public int getNumBakes() {
		return numBakes;
	}

}