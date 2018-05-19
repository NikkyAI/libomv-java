package libomv.model.directory;

/*
 * Query Flags used in many of the DirectoryManager methods to specify which
 * query to execute and how to return the results.
 *
 * Flags can be combined using the | (pipe) character, not all flags are
 * available in all queries
 */
public class DirFindFlags {
	// Query the People database
	public static final int People = 1 << 0;
	//
	public static final int Online = 1 << 1;
	// [Obsolete]
	// public static final int Places = 1 << 2;
	//
	public static final int Events = 1 << 3;
	// Query the Groups database
	public static final int Groups = 1 << 4;
	// Query the Events database
	public static final int DateEvents = 1 << 5;
	// Query the land holdings database for land owned by the currently
	// connected agent
	public static final int AgentOwned = 1 << 6;
	//
	public static final int ForSale = 1 << 7;
	// Query the land holdings database for land which is owned by a Group
	public static final int GroupOwned = 1 << 8;
	// [Obsolete]
	// public static final int Auction = 1 << 9;
	// Specifies the query should pre sort the results based upon traffic
	// when searching the Places database
	public static final int DwellSort = 1 << 10;
	//
	public static final int PgSimsOnly = 1 << 11;
	//
	public static final int PicturesOnly = 1 << 12;
	//
	public static final int PgEventsOnly = 1 << 13;
	//
	public static final int MatureSimsOnly = 1 << 14;
	// Specifies the query should pre sort the results in an ascending order
	// when searching the land sales database.
	// This flag is only used when searching the land sales database
	public static final int SortAsc = 1 << 15;
	// Specifies the query should pre sort the results using the SalePrice
	// field when searching the land sales database.
	// This flag is only used when searching the land sales database
	public static final int PricesSort = 1 << 16;
	// Specifies the query should pre sort the results by calculating the
	// average price/sq.m (SalePrice / Area) when searching the land sales
	// database.
	// This flag is only used when searching the land sales database
	public static final int PerMeterSort = 1 << 17;
	// Specifies the query should pre sort the results using the ParcelSize
	// field when searching the land sales database.
	// This flag is only used when searching the land sales database
	public static final int AreaSort = 1 << 18;
	// Specifies the query should pre sort the results using the Name field
	// when searching the land sales database.
	// This flag is only used when searching the land sales database
	public static final int NameSort = 1 << 19;
	// When set, only parcels less than the specified Price will be included
	// when searching the land sales database.
	// This flag is only used when searching the land sales database
	public static final int LimitByPrice = 1 << 20;
	// When set, only parcels greater than the specified Size will be
	// included when searching the land sales database.
	// This flag is only used when searching the land sales database
	public static final int LimitByArea = 1 << 21;
	//
	public static final int FilterMature = 1 << 22;
	//
	public static final int PGOnly = 1 << 23;
	// Include PG land in results. This flag is used when searching both the
	// Groups, Events and Land sales databases
	public static final int IncludePG = 1 << 24;
	// Include Mature land in results. This flag is used when searching both
	// the Groups, Events and Land sales databases
	public static final int IncludeMature = 1 << 25;
	// Include Adult land in results. This flag is used when searching both
	// the Groups, Events and Land sales databases
	public static final int IncludeAdult = 1 << 26;
	//
	public static final int AdultOnly = 1 << 27;

	public static final int setValue(int value) {
		return (value & _mask);
	}

	public static final int getValue(int value) {
		return (value & _mask);
	}

	private static final int _mask = 0x0FFFFFDB;
}