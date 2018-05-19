package libomv.model.group;

public class GroupAccountTransactions {
	public class TransactionEntry {
		public String Time;
		public String Item;
		public String User;
		public int Type;
		public int Amount;
	}

	public int IntervalDays;

	public int CurrentInterval;

	public String StartDate;

	public TransactionEntry[] Transactions;
}