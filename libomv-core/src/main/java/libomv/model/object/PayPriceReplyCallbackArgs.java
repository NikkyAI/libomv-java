package libomv.model.object;

import libomv.model.Simulator;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class PayPriceReplyCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final UUID m_ObjectID;
	private final int m_DefaultPrice;
	private final int[] m_ButtonPrices;

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	public final UUID getObjectID() {
		return m_ObjectID;
	}

	public final int getDefaultPrice() {
		return m_DefaultPrice;
	}

	public final int[] getButtonPrices() {
		return m_ButtonPrices;
	}

	public PayPriceReplyCallbackArgs(Simulator simulator, UUID objectID, int defaultPrice, int[] buttonPrices) {
		this.m_Simulator = simulator;
		this.m_ObjectID = objectID;
		this.m_DefaultPrice = defaultPrice;
		this.m_ButtonPrices = buttonPrices;
	}
}