package libomv.model.agent;

import libomv.types.UUID;

// Transaction detail sent with MoneyBalanceReply message
public class TransactionInfo {
	// Type of the transaction
	public int TransactionType; // FIXME: this should be an enum
	// UUID of the transaction source
	public UUID SourceID;
	// Is the transaction source a group
	public boolean IsSourceGroup;
	// UUID of the transaction destination
	public UUID DestID;
	// Is transaction destination a group
	public boolean IsDestGroup;
	// Transaction amount
	public int Amount;
	// Transaction description
	public String ItemDescription;
}