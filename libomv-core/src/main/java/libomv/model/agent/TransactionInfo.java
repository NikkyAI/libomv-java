package libomv.model.agent;

import libomv.types.UUID;

// Transaction detail sent with MoneyBalanceReply message
public class TransactionInfo {
	// Type of the transaction
	public int transactionType; // FIXME: this should be an enum
	// UUID of the transaction source
	public UUID sourceID;
	// Is the transaction source a group
	public boolean isSourceGroup;
	// UUID of the transaction destination
	public UUID destID;
	// Is transaction destination a group
	public boolean isDestGroup;
	// Transaction amount
	public int amount;
	// Transaction description
	public String itemDescription;
}