package libomv.model;

import java.util.ArrayList;
import java.util.Date;

import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.CallbackArgs;
import libomv.utils.Helpers;

public interface Directory {

	/* Classified Ad categories */
	public enum ClassifiedCategories {
		// Classified is listed in the Any category
		Any,
		// Classified is shopping related
		Shopping,
		// Classified is
		LandRental,
		//
		PropertyRental,
		//
		SpecialAttraction,
		//
		NewProducts,
		//
		Employment,
		//
		Wanted,
		//
		Service,
		//
		Personal;

		public static ClassifiedCategories setValue(int value) {
			return values()[value];
		}

		public static byte getValue(ClassifiedCategories value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

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

	/*
	 * Query Flags used in many of the DirectoryManager methods to specify which
	 * query to execute and how to return the results.
	 *
	 * Flags can be combined using the | (pipe) character, not all flags are
	 * available in all queries
	 */
	public static class DirFindFlags {
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

	/* Land types to search dataserver for */
	public static class SearchTypeFlags {
		// Search Auction, Mainland and Estate
		public static final byte Any = -1;
		// Land which is currently up for auction
		public static final byte Auction = 1 << 1;
		// Land available to new landowners (formerly the FirstLand program)
		// [Obsolete]
		// public static final byte Newbie = 1 << 2;
		// Parcels which are on the mainland (Linden owned) continents
		public static final byte Mainland = 1 << 3;
		// Parcels which are on privately owned simulators
		public static final byte Estate = 1 << 4;

		public static final byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static final int getValue(byte value) {
			return (value & _mask);
		}

		private static final byte _mask = 0x1B;
	}

	/* The content rating of the event */
	public enum EventFlags {
		// Event is PG
		PG,
		// Event is Mature
		Mature,
		// Event is Adult
		Adult;

		public static EventFlags setValue(int value) {
			return values()[value];
		}

		public static byte getValue(EventFlags value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/*
	 * Classified Ad Options
	 *
	 * There appear to be two formats the flags are packed in. This set of flags is
	 * for the newer style
	 */
	public static class ClassifiedFlags {
		//
		public static final byte None = 1 << 0;
		//
		public static final byte Mature = 1 << 1;
		//
		public static final byte Enabled = 1 << 2;
		// Deprecated
		// public static final byte HasPrice = 1 << 3;
		//
		public static final byte UpdateTime = 1 << 4;
		//
		public static final byte AutoRenew = 1 << 5;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x37;
	}

	/* Classified ad query options */
	public static class ClassifiedQueryFlags {
		// Include PG ads in results
		public static final byte PG = 1 << 2;
		// Include Mature ads in results
		public static final byte Mature = 1 << 3;
		// Include Adult ads in results
		public static final byte Adult = 1 << 6;
		// Include all ads in results
		public static final byte All = PG | Mature | Adult;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x4C;
	}

	/* The For Sale flag in PlacesReplyData */
	public enum PlacesFlags {
		// Parcel is not listed for sale
		NotForSale(0),
		// Parcel is For Sale
		ForSale(128);

		public static PlacesFlags setValue(int value) {
			for (PlacesFlags e : values())
				if (e._value == value)
					return e;
			return NotForSale;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		PlacesFlags(int value) {
			this._value = (byte) value;
		}
	}

	// /#region Structs

	/** A classified ad on the grid */
	public final class Classified {
		/**
		 * UUID for this ad, useful for looking up detailed information about it
		 */
		public UUID ID;
		/** The title of this classified ad */
		public String Name;
		/** Flags that show certain options applied to the classified */
		public byte Flags;
		/** Creation date of the ad */
		public Date CreationDate;
		/** Expiration date of the ad */
		public Date ExpirationDate;
		/** Price that was paid for this ad */
		public int Price;

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	/**
	 * A parcel retrieved from the dataserver such as results from the "For-Sale"
	 * listings or "Places" Search
	 */
	public final class DirectoryParcel {
		/**
		 * The unique dataserver parcel ID This id is used to obtain additional
		 * information from the entry by using the
		 * <see cref="ParcelManager.InfoRequest"/> method
		 */
		public UUID ID;
		/** A string containing the name of the parcel */
		public String Name;
		/**
		 * The size of the parcel This field is not returned for Places searches
		 */
		public int ActualArea;
		/**
		 * The price of the parcel This field is not returned for Places searches
		 */
		public int SalePrice;
		/** If True, this parcel is flagged to be auctioned */
		public boolean Auction;
		/** If true, this parcel is currently set for sale */
		public boolean ForSale;
		/** Parcel traffic */
		public float Dwell;

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	/** An Avatar returned from the dataserver */
	public final class AgentSearchData {
		/**
		 * Online status of agent This field appears to be obsolete and always returns
		 * false
		 */
		public boolean Online;
		/** The agents first name */
		public String FirstName;
		/** The agents last name */
		public String LastName;
		/** The agents <see cref="UUID"/> */
		public UUID AgentID;

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	/** Response to a "Groups" Search */
	public final class GroupSearchData {
		/** The Group ID */
		public UUID GroupID;
		/** The name of the group */
		public String GroupName;
		/** The current number of members */
		public int Members;

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	/**
	 * Parcel information returned from a <see cref="StartPlacesSearch"/> request
	 *
	 * Represents one of the following:
	 *
	 * A parcel of land on the grid that has its Show In Search flag set A parcel of
	 * land owned by the agent making the request A parcel of land owned by a group
	 * the agent making the request is a member of
	 *
	 * In a request for Group Land, the First record will contain an empty record
	 *
	 * Note: This is not the same as searching the land for sale data source
	 */
	public final class PlacesSearchData {
		/** The ID of the Agent of Group that owns the parcel */
		public UUID OwnerID;
		/** The name */
		public String Name;
		/** The description */
		public String Desc;
		/** The Size of the parcel */
		public int ActualArea;
		/**
		 * The billable Size of the parcel, for mainland parcels this will match the
		 * ActualArea field. For Group owned land this will be 10 percent smaller than
		 * the ActualArea. For Estate land this will always be 0
		 */
		public int BillableArea;
		/** Indicates the ForSale status of the parcel */
		public PlacesFlags Flags;
		/** The Gridwide X position */
		public float GlobalX;
		/** The Gridwide Y position */
		public float GlobalY;
		/** The Z position of the parcel, or 0 if no landing point set */
		public float GlobalZ;
		/** The name of the Region the parcel is located in */
		public String SimName;
		/** The Asset ID of the parcels Snapshot texture */
		public UUID SnapshotID;
		/** The calculated visitor traffic */
		public float Dwell;
		/**
		 * The billing product SKU
		 *
		 * Known values are: <list> <item><term>023</term><description>Mainland / Full
		 * Region</description></item> <item><term>024</term><description>Estate / Full
		 * Region</description></item> <item><term>027</term><description>Estate /
		 * Openspace</description></item> <item><term>029</term><description>Estate /
		 * Homestead</description></item> <item><term>129</term><description>Mainland /
		 * Homestead (Linden Owned)</description></item> </list>
		 */
		public String SKU;
		/** No longer used, will always be 0 */
		public int Price;

		/**
		 * Get a SL URL for the parcel
		 *
		 * @return A string, containing a standard SLURL
		 */
		public String toSLurl() {
			float[] values = new float[2];
			Helpers.GlobalPosToRegionHandle(this.GlobalX, this.GlobalY, values);
			return "secondlife://" + this.SimName + "/" + values[0] + "/" + values[1] + "/" + this.GlobalZ;
		}

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	/** An "Event" Listing summary */
	public final class EventsSearchData {
		/** The ID of the event creator */
		public UUID Owner;
		/** The name of the event */
		public String Name;
		/** The events ID */
		public int ID; /* TODO was uint */
		/** A string containing the short date/time the event will begin */
		public String Date;
		/** The event start time in Unixtime (seconds since epoch) */
		public int Time;
		/** The events maturity rating */
		public EventFlags Flags;

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	/** The details of an "Event" */
	public final class EventInfo {
		/** The events ID */
		public int ID; /* TODO was uint */
		/** The ID of the event creator */
		public UUID Creator;
		/** The name of the event */
		public String Name;
		/** The category */
		public EventCategories Category;
		/** The events description */
		public String Desc;
		/** The short date/time the event will begin */
		public String Date;
		/** The event start time in Unixtime (seconds since epoch) UTC adjusted */
		public int DateUTC; /* TODO was uint */
		/** The length of the event in minutes */
		public int Duration; /* TODO was uint */
		/** 0 if no cover charge applies */
		public int Cover; /* TODO was uint */
		/** The cover charge amount in L$ if applicable */
		public int Amount; /* TODO was uint */
		/** The name of the region where the event is being held */
		public String SimName;
		/** The gridwide location of the event */
		public Vector3d GlobalPos;
		/** The maturity rating */
		public EventFlags Flags;

		/**
		 * Get a SL URL for the parcel where the event is hosted
		 *
		 * @return A string, containing a standard SLURL
		 */
		public String toSLurl() {
			float[] values = new float[2];
			Helpers.GlobalPosToRegionHandle((float) this.GlobalPos.X, (float) this.GlobalPos.Y, values);
			return "secondlife://" + this.SimName + "/" + values[0] + "/" + values[1] + "/" + this.GlobalPos.Z;
		}

		/**
		 * Print the struct data as a string
		 *
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString() {
			return Helpers.StructToString(this);
		}
	}

	// /#region callback handlers
	/** Contains the "Event" detail data returned from the data server */
	public class DirEventsReplyCallbackArgs implements CallbackArgs {
		private final UUID m_QueryID;

		/** The ID returned by <see cref="DirectoryManager.StartEventsSearch"/> */
		public final UUID getQueryID() {
			return m_QueryID;
		}

		private final ArrayList<EventsSearchData> m_matchedEvents;

		/** A list of "Events" returned by the data server */
		public final ArrayList<EventsSearchData> getMatchedEvents() {
			return m_matchedEvents;
		}

		/**
		 * Construct a new instance of the DirEventsReplyEventArgs class
		 *
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the
		 *            <see cref="StartEventsSearch"/> method
		 * @param matchedEvents
		 *            A list containing the "Events" returned by the search query
		 */
		public DirEventsReplyCallbackArgs(UUID queryID, ArrayList<EventsSearchData> matchedEvents) {
			this.m_QueryID = queryID;
			this.m_matchedEvents = matchedEvents;
		}
	}

	/**
	 * Contains the Event data returned from the data server from an
	 * EventInfoRequest
	 */
	public class EventInfoReplyCallbackArgs implements CallbackArgs {
		private final EventInfo m_MatchedEvent;

		/** A single EventInfo object containing the details of an event */
		public final EventInfo getMatchedEvent() {
			return m_MatchedEvent;
		}

		/**
		 * Construct a new instance of the EventInfoReplyEventArgs class
		 *
		 * @param matchedEvent
		 *            A single EventInfo object containing the details of an event
		 */
		public EventInfoReplyCallbackArgs(EventInfo matchedEvent) {
			this.m_MatchedEvent = matchedEvent;
		}
	}

	/** Contains the places data returned from the data server */
	public class DirPlacesReplyCallbackArgs implements CallbackArgs {
		private final UUID m_QueryID;

		// The ID returned by <see
		// cref="DirectoryManager.StartDirPlacesSearch"/>
		public final UUID getQueryID() {
			return m_QueryID;
		}

		private final ArrayList<DirectoryParcel> m_MatchedParcels;

		// A list containing Places data returned by the data server
		public final ArrayList<DirectoryParcel> getMatchedParcels() {
			return m_MatchedParcels;
		}

		/**
		 * Construct a new instance of the DirPlacesReplyEventArgs class
		 *
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the
		 *            <see cref="StartDirPlacesSearch"/> method
		 * @param matchedParcels
		 *            A list containing land data returned by the data server
		 */
		public DirPlacesReplyCallbackArgs(UUID queryID, ArrayList<DirectoryParcel> matchedParcels) {
			this.m_QueryID = queryID;
			this.m_MatchedParcels = matchedParcels;
		}
	}

	/** Contains the "Event" list data returned from the data server */
	public class PlacesReplyCallbackArgs implements CallbackArgs {
		private final UUID m_QueryID;

		// The ID returned by <see cref="DirectoryManager.StartPlacesSearch"/>
		public final UUID getQueryID() {
			return m_QueryID;
		}

		private final ArrayList<PlacesSearchData> m_MatchedPlaces;

		// A list of "Places" returned by the data server
		public final ArrayList<PlacesSearchData> getMatchedPlaces() {
			return m_MatchedPlaces;
		}

		/**
		 * Construct a new instance of PlacesReplyEventArgs class
		 *
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the
		 *            <see cref="StartPlacesSearch"/> method
		 * @param matchedPlaces
		 *            A list containing the "Places" returned by the data server query
		 */
		public PlacesReplyCallbackArgs(UUID queryID, ArrayList<PlacesSearchData> matchedPlaces) {
			this.m_QueryID = queryID;
			this.m_MatchedPlaces = matchedPlaces;
		}
	}

	/** Contains the classified data returned from the data server */
	public class DirClassifiedsReplyCallbackArgs implements CallbackArgs {
		private final ArrayList<Classified> m_Classifieds;

		// A list containing Classified Ads returned by the data server
		public final ArrayList<Classified> getClassifieds() {
			return m_Classifieds;
		}

		/**
		 * Construct a new instance of the DirClassifiedsReplyEventArgs class
		 *
		 * @param classifieds
		 *            A list of classified ad data returned from the data server
		 */
		public DirClassifiedsReplyCallbackArgs(ArrayList<Classified> classifieds) {
			this.m_Classifieds = classifieds;
		}
	}

	/** Contains the group data returned from the data server */
	public class DirGroupsReplyCallbackArgs implements CallbackArgs {
		private final UUID m_QueryID;

		// The ID returned by <see cref="DirectoryManager.StartGroupSearch"/>
		public final UUID getQueryID() {
			return m_QueryID;
		}

		private final ArrayList<GroupSearchData> m_matchedGroups;

		// A list containing Groups data returned by the data server
		public final ArrayList<GroupSearchData> getMatchedGroups() {
			return m_matchedGroups;
		}

		/**
		 * Construct a new instance of the DirGroupsReplyEventArgs class
		 *
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the <see cref="StartGroupSearch"/>
		 *            method
		 * @param matchedGroups
		 *            A list of groups data returned by the data server
		 */
		public DirGroupsReplyCallbackArgs(UUID queryID, ArrayList<GroupSearchData> matchedGroups) {
			this.m_QueryID = queryID;
			this.m_matchedGroups = matchedGroups;
		}
	}

	/** Contains the people data returned from the data server */
	public class DirPeopleReplyCallbackArgs implements CallbackArgs {
		private final UUID m_QueryID;

		// The ID returned by <see cref="DirectoryManager.StartPeopleSearch"/>
		public final UUID getQueryID() {
			return m_QueryID;
		}

		private final ArrayList<AgentSearchData> m_MatchedPeople;

		// A list containing People data returned by the data server
		public final ArrayList<AgentSearchData> getMatchedPeople() {
			return m_MatchedPeople;
		}

		/**
		 * Construct a new instance of the DirPeopleReplyEventArgs class
		 *
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the
		 *            <see cref="StartPeopleSearch"/> method
		 * @param matchedPeople
		 *            A list of people data returned by the data server
		 */
		public DirPeopleReplyCallbackArgs(UUID queryID, ArrayList<AgentSearchData> matchedPeople) {
			this.m_QueryID = queryID;
			this.m_MatchedPeople = matchedPeople;
		}
	}

	/** Contains the land sales data returned from the data server */
	public class DirLandReplyCallbackArgs implements CallbackArgs {
		private final ArrayList<DirectoryParcel> m_DirParcels;

		// A list containing land forsale data returned by the data server
		public final ArrayList<DirectoryParcel> getDirParcels() {
			return m_DirParcels;
		}

		/**
		 * Construct a new instance of the DirLandReplyEventArgs class
		 *
		 * @param dirParcels
		 *            A list of parcels for sale returned by the data server
		 */
		public DirLandReplyCallbackArgs(ArrayList<DirectoryParcel> dirParcels) {
			this.m_DirParcels = dirParcels;
		}
	}

}
