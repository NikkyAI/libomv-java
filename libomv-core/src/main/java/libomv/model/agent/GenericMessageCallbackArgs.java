package libomv.model.agent;

import java.util.List;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class GenericMessageCallbackArgs implements CallbackArgs {
	private final UUID sessionID;
	private final UUID transactionID;
	private final String method;
	private final UUID invoiceID;
	private List<String> parameters;

	public GenericMessageCallbackArgs(UUID sessionID, UUID transactionID, String method, UUID invoiceID,
			List<String> parameters) {
		this.sessionID = sessionID;
		this.transactionID = transactionID;
		this.method = method;
		this.invoiceID = invoiceID;
		this.parameters = parameters;
	}

	public UUID getSessionID() {
		return sessionID;
	}

	public UUID getTransactionID() {
		return transactionID;
	}

	public String getMethod() {
		return method;
	}

	public UUID getInvoiceID() {
		return invoiceID;
	}

	public List<String> getParameters() {
		return parameters;
	}

}
