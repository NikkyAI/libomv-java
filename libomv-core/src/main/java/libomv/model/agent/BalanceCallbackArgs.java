package libomv.model.agent;

/* The date received from an ImprovedInstantMessage */
public class BalanceCallbackArgs {
	private final int balance;
	private final int delta;
	private final boolean firstBalance;

	/**
	 * Construct a new instance of the BalanceCallbackArgs object
	 *
	 * @param balance
	 *            the InstantMessage object
	 */
	public BalanceCallbackArgs(int balance, int delta, boolean firstBalance) {
		this.balance = balance;
		this.delta = delta;
		this.firstBalance = firstBalance;
	}

	/* Get the balance value */
	public final int getBalance() {
		return balance;
	}

	/* Get the balance value */
	public final int getDelta() {
		return delta;
	}

	/* Get the balance value */
	public final boolean getFirst() {
		return firstBalance;
	}

}