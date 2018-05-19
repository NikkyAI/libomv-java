package libomv.model.object;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class PayPriceReplyCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final UUID objectID;
	private final int defaultPrice;
	private final int[] buttonPrices;

	public PayPriceReplyCallbackArgs(Simulator simulator, UUID objectID, int defaultPrice, int[] buttonPrices) {
		this.simulator = simulator;
		this.objectID = objectID;
		this.defaultPrice = defaultPrice;
		this.buttonPrices = buttonPrices;
	}

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return simulator;
	}

	public final UUID getObjectID() {
		return objectID;
	}

	public final int getDefaultPrice() {
		return defaultPrice;
	}

	public final int[] getButtonPrices() {
		return buttonPrices;
	}

}