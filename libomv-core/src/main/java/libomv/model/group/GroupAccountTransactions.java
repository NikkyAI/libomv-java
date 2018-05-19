package libomv.model.group;

public class GroupAccountTransactions {
	public class TransactionEntry {
		public String time;
		public String item;
		public String user;
		public int type;
		public int amount;
	}

	public int intervalDays;

	public int currentInterval;

	public String startDate;

	public TransactionEntry[] transactions;
}