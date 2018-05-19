package libomv.model.group;

import libomv.utils.HashMapInt;

public class GroupAccountDetails {
	public int intervalDays;

	public int currentInterval;

	public String startDate;

	// A list of description/amount pairs making up the account history
	//
	// public List<KeyValuePair<string, int>> HistoryItems;
	// Still needs to implement the GroupAccount Details Handler and define
	// the data type
	public HashMapInt<String> historyItems;
}