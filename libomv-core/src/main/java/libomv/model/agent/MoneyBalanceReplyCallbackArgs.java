package libomv.model.agent;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Contains the transaction summary when an item is purchased, money is
// given, or land is purchased
public class MoneyBalanceReplyCallbackArgs implements CallbackArgs {
	private final UUID m_TransactionID;
	private final boolean m_Success;
	private final int m_Balance;
	private final int m_MetersCredit;
	private final int m_MetersCommitted;
	private final String m_Description;
	private TransactionInfo m_TransactionInfo;

	// Get the ID of the transaction
	public UUID getTransactionID() {
		return m_TransactionID;
	}

	// True of the transaction was successful
	public boolean getSuccess() {
		return m_Success;
	}

	// Get the remaining currency balance
	public int getBalance() {
		return m_Balance;
	}

	// Get the meters credited
	public int getMetersCredit() {
		return m_MetersCredit;
	}

	// Get the meters comitted
	public int getMetersCommitted() {
		return m_MetersCommitted;
	}

	// Get the description of the transaction
	public String getDescription() {
		return m_Description;
	}

	// Detailed transaction information
	public TransactionInfo getTransactionInfo() {
		return m_TransactionInfo;
	}

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
	public MoneyBalanceReplyCallbackArgs(UUID transactionID, boolean transactionSuccess, int balance,
			int metersCredit, int metersCommitted, String description, TransactionInfo transactionInfo) {
		this.m_TransactionID = transactionID;
		this.m_Success = transactionSuccess;
		this.m_Balance = balance;
		this.m_MetersCredit = metersCredit;
		this.m_MetersCommitted = metersCommitted;
		this.m_Description = description;
		this.m_TransactionInfo = transactionInfo;
	}
}