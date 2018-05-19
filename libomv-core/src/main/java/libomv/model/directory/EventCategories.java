package libomv.model.directory;

// Event Categories
public enum EventCategories {
	//
	All(0),
	//
	Discussion(18),
	//
	Sports(19),
	//
	LiveMusic(20),
	//
	Commercial(22),
	//
	Nightlife(23),
	//
	Games(24),
	//
	Pageants(25),
	//
	Education(26),
	//
	Arts(27),
	//
	Charity(28),
	//
	Miscellaneous(29);

	public int value;

	EventCategories(int val) {
		this.value = val;
	}
}