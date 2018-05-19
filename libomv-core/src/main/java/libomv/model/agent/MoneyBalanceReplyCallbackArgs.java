package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Contains the transaction summary when an item is purchased, money is
// given, or land is purchased
public class MoneyBalanceReplyCallbackArgs implements CallbackArgs {
	private final UUID transactionID;
	private final boolean success;
	private final int balance;
	private final int metersCredit;
	private final int metersCommitted;
	private final String description;
	private TransactionInfo transactionInfo;

	/**
	 * Construct a new instance of the MoneyBalanceReplyEventArgs object
	 *
	 * @param transactionID
	 *            The ID of the transaction
	 * @param transactionSuccess
	 *            True of the transaction was successful
	 * @param balance
	 *            The current currency balance
	 * @param metersCredit
	 *            The meters credited
	 * @param metersCommitted
	 *            The meters comitted
	 * @param description
	 *            A brief description of the transaction
	 * @param transactionInfo
	 *            Transaction info
	 */
	public MoneyBalanceReplyCallbackArgs(UUID transactionID, boolean transactionSuccess, int balance, int metersCredit,
			int metersCommitted, String description, TransactionInfo transactionInfo) {
		this.transactionID = transactionID;
		this.success = transactionSuccess;
		this.balance = balance;
		this.metersCredit = metersCredit;
		this.metersCommitted = metersCommitted;
		this.description = description;
		this.transactionInfo = transactionInfo;
	}

	// Get the ID of the transaction
	public UUID getTransactionID() {
		return transactionID;
	}

	// True of the transaction was successful
	public boolean getSuccess() {
		return success;
	}

	// Get the remaining currency balance
	public int getBalance() {
		return balance;
	}

	// Get the meters credited
	public int getMetersCredit() {
		return metersCredit;
	}

	// Get the meters comitted
	public int getMetersCommitted() {
		return metersCommitted;
	}

	// Get the description of the transaction
	public String getDescription() {
		return description;
	}

	// Detailed transaction information
	public TransactionInfo getTransactionInfo() {
		return transactionInfo;
	}

}