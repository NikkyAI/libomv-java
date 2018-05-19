/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.io;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.DirLandReplyMessage;
import libomv.capabilities.CapsMessage.PlacesReplyMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.model.directory.AgentSearchData;
import libomv.model.directory.Classified;
import libomv.model.directory.ClassifiedCategories;
import libomv.model.directory.ClassifiedFlags;
import libomv.model.directory.ClassifiedQueryFlags;
import libomv.model.directory.DirClassifiedsReplyCallbackArgs;
import libomv.model.directory.DirEventsReplyCallbackArgs;
import libomv.model.directory.DirFindFlags;
import libomv.model.directory.DirGroupsReplyCallbackArgs;
import libomv.model.directory.DirLandReplyCallbackArgs;
import libomv.model.directory.DirPeopleReplyCallbackArgs;
import libomv.model.directory.DirPlacesReplyCallbackArgs;
import libomv.model.directory.DirectoryParcel;
import libomv.model.directory.EventCategories;
import libomv.model.directory.EventFlags;
import libomv.model.directory.EventInfo;
import libomv.model.directory.EventInfoReplyCallbackArgs;
import libomv.model.directory.EventsSearchData;
import libomv.model.directory.GroupSearchData;
import libomv.model.directory.PlacesFlags;
import libomv.model.directory.PlacesReplyCallbackArgs;
import libomv.model.directory.PlacesSearchData;
import libomv.model.parcel.ParcelCategory;
import libomv.model.Simulator;
import libomv.packets.DirClassifiedQueryPacket;
import libomv.packets.DirClassifiedReplyPacket;
import libomv.packets.DirEventsReplyPacket;
import libomv.packets.DirFindQueryPacket;
import libomv.packets.DirGroupsReplyPacket;
import libomv.packets.DirLandQueryPacket;
import libomv.packets.DirLandReplyPacket;
import libomv.packets.DirPeopleReplyPacket;
import libomv.packets.DirPlacesQueryPacket;
import libomv.packets.DirPlacesReplyPacket;
import libomv.packets.DirPlacesReplyPacket.QueryRepliesBlock;
import libomv.packets.EventInfoReplyPacket;
import libomv.packets.EventInfoRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PlacesQueryPacket;
import libomv.packets.PlacesReplyPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

/* Access to the data server which allows searching for land, events, people, etc */
public class DirectoryManager implements PacketCallback, CapsCallback {
	// /#region Enums

	public CallbackHandler<DirEventsReplyCallbackArgs> OnDirEvents = new CallbackHandler<DirEventsReplyCallbackArgs>();

	public CallbackHandler<EventInfoReplyCallbackArgs> OnEventInfo = new CallbackHandler<EventInfoReplyCallbackArgs>();

	public CallbackHandler<DirPlacesReplyCallbackArgs> OnDirPlaces = new CallbackHandler<DirPlacesReplyCallbackArgs>();

	public CallbackHandler<PlacesReplyCallbackArgs> OnPlaces = new CallbackHandler<PlacesReplyCallbackArgs>();

	public CallbackHandler<DirClassifiedsReplyCallbackArgs> OnDirClassifieds = new CallbackHandler<DirClassifiedsReplyCallbackArgs>();

	public CallbackHandler<DirGroupsReplyCallbackArgs> OnDirGroups = new CallbackHandler<DirGroupsReplyCallbackArgs>();

	public CallbackHandler<DirPeopleReplyCallbackArgs> OnDirPeople = new CallbackHandler<DirPeopleReplyCallbackArgs>();

	public CallbackHandler<DirLandReplyCallbackArgs> OnDirLand = new CallbackHandler<DirLandReplyCallbackArgs>();

	// /#region Private Members
	private GridClient _Client;

	// /#region Constructors

	/**
	 * Constructs a new instance of the DirectoryManager class
	 *
	 * @param client
	 *            An instance of GridClient
	 */
	public DirectoryManager(GridClient client) {
		_Client = client;

		_Client.Network.RegisterCallback(PacketType.DirClassifiedReply, this);
		// Deprecated, replies come in over capabilities
		_Client.Network.RegisterCallback(PacketType.DirLandReply, this);
		_Client.Network.RegisterCallback(CapsEventType.DirLandReply, this);
		_Client.Network.RegisterCallback(PacketType.DirPeopleReply, this);
		_Client.Network.RegisterCallback(PacketType.DirGroupsReply, this);
		// Deprecated as of viewer 1.2.3
		_Client.Network.RegisterCallback(PacketType.PlacesReply, this);
		_Client.Network.RegisterCallback(CapsEventType.PlacesReply, this);
		_Client.Network.RegisterCallback(PacketType.DirEventsReply, this);
		_Client.Network.RegisterCallback(PacketType.EventInfoReply, this);
		_Client.Network.RegisterCallback(PacketType.DirPlacesReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case DirClassifiedReply:
			HandleDirClassifiedReply(packet, simulator);
			break;
		// Deprecated, replies come in over capabilities
		case DirLandReply:
			HandleDirLandReply(packet, simulator);
			break;
		case DirPeopleReply:
			HandleDirPeopleReply(packet, simulator);
			break;
		case DirGroupsReply:
			HandleDirGroupsReply(packet, simulator);
			break;
		// Deprecated as of viewer 1.2.3
		case PlacesReply:
			HandlePlacesReply(packet, simulator);
			break;
		case DirEventsReply:
			HandleEventsReply(packet, simulator);
			break;
		case EventInfoReply:
			HandleEventInfoReply(packet, simulator);
			break;
		case DirPlacesReply:
			HandleDirPlacesReply(packet, simulator);
			break;
		default:
			break;
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case DirLandReply:
			HandleDirLandReply(message, simulator);
			break;
		case PlacesReply:
			HandlePlacesReply(message, simulator);
			break;
		default:
			break;
		}
	}

	// /#region Public Methods
	/**
	 * Query the data server for a list of classified ads containing the specified
	 * string. Defaults to searching for classified placed in any category, and
	 * includes PG, Adult and Mature results.
	 *
	 * Responses are sent 16 per response packet, there is no way to know how many
	 * results a query reply will contain however assuming the reply packets arrived
	 * ordered, a response with less than 16 entries would indicate all results have
	 * been received
	 *
	 * The <see cref="OnClassifiedReply"/> event is raised when a response is
	 * received from the simulator
	 *
	 * @param searchText
	 *            A string containing a list of keywords to search for
	 * @return A UUID to correlate the results when the
	 *         <see cref="OnClassifiedReply"/> event is raised
	 * @throws Exception
	 */
	public final UUID StartClassifiedSearch(String searchText) throws Exception {
		return StartClassifiedSearch(searchText, ClassifiedCategories.Any, ClassifiedQueryFlags.All);
	}

	/**
	 * Query the data server for a list of classified ads which contain specified
	 * keywords (Overload)
	 *
	 * The <see cref="OnClassifiedReply"/> event is raised when a response is
	 * received from the simulator
	 *
	 * @param searchText
	 *            A string containing a list of keywords to search for
	 * @param category
	 *            The category to search
	 * @param queryFlags
	 *            A set of flags which can be ORed to modify query options such as
	 *            classified maturity rating.
	 * @return A UUID to correlate the results when the
	 *         <see cref="OnClassifiedReply"/> event is raised
	 * @throws Exception
	 *
	 *             <example> Search classified ads containing the key words "foo"
	 *             and "bar" in the "Any" category that are either PG or Mature
	 *             <code>
	 *  UUID searchID = StartClassifiedSearch("foo bar", ClassifiedCategories.Any, ClassifiedQueryFlags.PG | ClassifiedQueryFlags.Mature);
	 *  </code> </example>
	 *
	 *             Responses are sent 16 at a time, there is no way to know how many
	 *             results a query reply will contain however assuming the reply
	 *             packets arrived ordered, a response with less than 16 entries
	 *             would indicate all results have been received
	 */
	public final UUID StartClassifiedSearch(String searchText, ClassifiedCategories category, byte queryFlags)
			throws Exception {
		DirClassifiedQueryPacket query = new DirClassifiedQueryPacket();
		UUID queryID = UUID.GenerateUUID();

		query.AgentData.AgentID = _Client.Self.getAgentID();
		query.AgentData.SessionID = _Client.Self.getSessionID();

		query.QueryData.Category = category.getValue();
		query.QueryData.QueryFlags = queryFlags;
		query.QueryData.QueryID = queryID;
		query.QueryData.setQueryText(Helpers.StringToBytes(searchText));

		_Client.Network.sendPacket(query);

		return queryID;
	}

	/**
	 * Starts search for places (Overloaded)
	 *
	 * The <see cref="OnDirPlacesReply"/> event is raised when a response is
	 * received from the simulator
	 *
	 * @param searchText
	 *            Search text
	 * @param queryStart
	 *            Each request is limited to 100 places being returned. To get the
	 *            first 100 result entries of a request use 0, from 100-199 use 1,
	 *            200-299 use 2, etc.
	 * @return A UUID to correlate the results when the
	 *         <see cref="OnDirPlacesReply"/> event is raised
	 * @throws Exception
	 */
	public final UUID StartDirPlacesSearch(String searchText, int queryStart) throws Exception {
		int flags = DirFindFlags.DwellSort | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartDirPlacesSearch(searchText, flags, ParcelCategory.Any, queryStart);
	}

	/**
	 * Queries the dataserver for parcels of land which are flagged to be shown in
	 * search
	 *
	 * The <see cref="OnDirPlacesReply"/> event is raised when a response is
	 * received from the simulator
	 *
	 * @param searchText
	 *            A string containing a list of keywords to search for separated by
	 *            a space character
	 * @param queryFlags
	 *            A set of flags which can be ORed to modify query options such as
	 *            classified maturity rating.
	 * @param category
	 *            The category to search
	 * @param queryStart
	 *            Each request is limited to 100 places being returned. To get the
	 *            first 100 result entries of a request use 0, from 100-199 use 1,
	 *            200-299 use 2, etc.
	 * @return A UUID to correlate the results when the
	 *         <see cref="OnDirPlacesReply"/> event is raised
	 * @throws Exception
	 *
	 *             <example> Search places containing the key words "foo" and "bar"
	 *             in the "Any" category that are either PG or Adult <code>
	 *  UUID searchID = StartDirPlacesSearch("foo bar", DirFindFlags.DwellSort | DirFindFlags.IncludePG | DirFindFlags.IncludeAdult, ParcelCategory.Any, 0);
	 *  </code> </example>
	 *
	 *             Additional information on the results can be obtained by using
	 *             the ParcelManager.InfoRequest method
	 */
	public final UUID StartDirPlacesSearch(String searchText, int queryFlags, ParcelCategory category, int queryStart)
			throws Exception {
		DirPlacesQueryPacket query = new DirPlacesQueryPacket();

		query.AgentData.AgentID = _Client.Self.getAgentID();
		query.AgentData.SessionID = _Client.Self.getSessionID();

		query.QueryData.Category = (byte) category.ordinal();
		query.QueryData.QueryFlags = queryFlags;

		query.QueryData.QueryID = UUID.GenerateUUID();
		query.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		query.QueryData.QueryStart = queryStart;
		query.QueryData.setSimName(Helpers.StringToBytes(Helpers.EmptyString));

		_Client.Network.sendPacket(query);

		return query.QueryData.QueryID;

	}

	/**
	 * Starts a search for land sales using the directory
	 *
	 * The <see cref="OnDirLandReply"/> event is raised when a response is received
	 * from the simulator
	 *
	 * @param typeFlags
	 *            What type of land to search for. Auction, estate, mainland, "first
	 *            land", etc
	 * @throws Exception
	 *
	 *             The OnDirLandReply event handler must be registered before
	 *             calling this function. There is no way to determine how many
	 *             results will be returned, or how many times the callback will be
	 *             fired other than you won't get more than 100 total parcels from
	 *             each query.
	 */
	public final void StartLandSearch(byte typeFlags) throws Exception {
		int flags = DirFindFlags.SortAsc | DirFindFlags.PerMeterSort;
		StartLandSearch(flags, typeFlags, 0, 0, 0);
	}

	/**
	 * Starts a search for land sales using the directory
	 *
	 * The {@link OnDirLandReply} event is raised when a response is received from
	 * the simulator
	 *
	 * @param typeFlags
	 *            What type of land to search for. Auction, estate, mainland, "first
	 *            land", etc
	 * @param priceLimit
	 *            Maximum price to search for
	 * @param areaLimit
	 *            Maximum area to search for
	 * @param queryStart
	 *            Each request is limited to 100 parcels being returned. To get the
	 *            first 100 parcels of a request use 0, from 100-199 use 1, 200-299
	 *            use 2, etc. The OnDirLandReply event handler must be registered
	 *            before calling this function. There is no way to determine how
	 *            many results will be returned, or how many times the callback will
	 *            be fired other than you won't get more than 100 total parcels from
	 *            each query.
	 * @throws Exception
	 */
	public final void StartLandSearch(byte typeFlags, int priceLimit, int areaLimit, int queryStart) throws Exception {
		int flags = DirFindFlags.SortAsc | DirFindFlags.PerMeterSort | DirFindFlags.LimitByPrice
				| DirFindFlags.LimitByArea;
		StartLandSearch(flags, typeFlags, priceLimit, areaLimit, queryStart);
	}

	/**
	 * Send a request to the data server for land sales listings
	 *
	 * @param findFlags
	 *            Flags sent to specify query options
	 *
	 *            Available flags: Specify the parcel rating with one or more of the
	 *            following: IncludePG IncludeMature IncludeAdult
	 *
	 *            Specify the field to pre sort the results with ONLY ONE of the
	 *            following: PerMeterSort NameSort AreaSort PricesSort
	 *
	 *            Specify the order the results are returned in, if not specified
	 *            the results are pre sorted in a Descending Order SortAsc
	 *
	 *            Specify additional filters to limit the results with one or both
	 *            of the following: LimitByPrice LimitByArea
	 *
	 *            Flags can be combined by separating them with the or (|) operator
	 *
	 *            Additional details can be found in <see cref="DirFindFlags"/>
	 *
	 * @param typeFlags
	 *            What type of land to search for. Auction, Estate or Mainland
	 * @param priceLimit
	 *            Maximum price to search for when the DirFindFlags.LimitByPrice
	 *            flag is specified in findFlags
	 * @param areaLimit
	 *            Maximum area to search for when the DirFindFlags.LimitByArea flag
	 *            is specified in findFlags
	 * @param queryStart
	 *            Each request is limited to 100 parcels being returned. To get the
	 *            first 100 parcels of a request use 0, from 100-199 use 100,
	 *            200-299 use 200, etc.
	 * @throws Exception
	 *
	 *             The {@link OnDirLandReply} event will be raised with the response
	 *             from the simulator
	 *
	 *             There is no way to determine how many results will be returned,
	 *             or how many times the callback will be fired other than you won't
	 *             get more than 100 total parcels from each reply.
	 *
	 *             Any land set for sale to either anybody or specific to the
	 *             connected agent will be included in the results if the land is
	 *             included in the query. <example> <code>
	 *  // request all mainland, any maturity rating that is larger than 512 sq.m
	 *  int flags = DirFindFlags.SortAsc.ordinal() | DirFindFlags.PerMeterSort.ordinal() |
	 *              DirFindFlags.LimitByArea.ordinal() | DirFindFlags.IncludePG.ordinal() |
	 *              DirFindFlags.IncludeMature.ordinal() | DirFindFlags.IncludeAdult.ordinal();
	 *  StartLandSearch(DirFindFlags.convert(flags), SearchTypeFlags.Mainland, 0, 512, 0);
	 *  </code></example>
	 */
	public final void StartLandSearch(int findFlags, byte typeFlags, int priceLimit, int areaLimit, int queryStart)
			throws Exception {
		DirLandQueryPacket query = new DirLandQueryPacket();
		query.AgentData.AgentID = _Client.Self.getAgentID();
		query.AgentData.SessionID = _Client.Self.getSessionID();
		query.QueryData.Area = areaLimit;
		query.QueryData.Price = priceLimit;
		query.QueryData.QueryStart = queryStart;
		query.QueryData.SearchType = typeFlags;
		query.QueryData.QueryFlags = findFlags;
		query.QueryData.QueryID = UUID.GenerateUUID();

		_Client.Network.sendPacket(query);
	}

	/**
	 * Search for Groups
	 *
	 * @param searchText
	 *            The name or portion of the name of the group you wish to search
	 *            for
	 * @param queryStart
	 *            Start from the match number
	 * @return
	 * @throws Exception
	 */
	public final UUID StartGroupSearch(String searchText, int queryStart) throws Exception {
		int flags = DirFindFlags.Groups | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartGroupSearch(searchText, queryStart, flags);
	}

	/**
	 * Search for Groups
	 *
	 * @param searchText
	 *            The name or portion of the name of the group you wish to search
	 *            for
	 * @param queryStart
	 *            Start from the match number
	 * @param flags
	 *            Search flags
	 * @return
	 * @throws Exception
	 */
	public final UUID StartGroupSearch(String searchText, int queryStart, int flags) throws Exception {
		DirFindQueryPacket find = new DirFindQueryPacket();
		find.AgentData.AgentID = _Client.Self.getAgentID();
		find.AgentData.SessionID = _Client.Self.getSessionID();
		find.QueryData.QueryFlags = flags;
		find.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		find.QueryData.QueryID = UUID.GenerateUUID();
		find.QueryData.QueryStart = queryStart;

		_Client.Network.sendPacket(find);

		return find.QueryData.QueryID;
	}

	/**
	 * Search the People directory for other avatars
	 *
	 * @param searchText
	 *            The name or portion of the name of the avatar you wish to search
	 *            for
	 * @param queryStart
	 * @return
	 * @throws Exception
	 */
	public final UUID StartPeopleSearch(String searchText, int queryStart) throws Exception {
		UUID uuid = UUID.GenerateUUID();
		StartPeopleSearch(searchText, queryStart, uuid);
		return uuid;
	}

	/**
	 * Search the People directory for other avatars
	 *
	 * @param searchText
	 *            The name or portion of the name of the avatar you wish to search
	 *            for
	 * @param queryStart
	 * @return
	 * @throws Exception
	 */
	public final void StartPeopleSearch(String searchText, int queryStart, UUID uuid) throws Exception {
		DirFindQueryPacket find = new DirFindQueryPacket();
		find.AgentData.AgentID = _Client.Self.getAgentID();
		find.AgentData.SessionID = _Client.Self.getSessionID();
		find.QueryData.QueryFlags = DirFindFlags.People;
		find.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		find.QueryData.QueryID = uuid;
		find.QueryData.QueryStart = queryStart;

		_Client.Network.sendPacket(find);
	}

	/**
	 * Search Places for parcels of land you personally own
	 *
	 * @return
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch() throws Exception {
		return StartPlacesSearch(DirFindFlags.AgentOwned, ParcelCategory.Any, Helpers.EmptyString, Helpers.EmptyString,
				UUID.Zero, UUID.GenerateUUID());
	}

	/**
	 * Searches Places for land owned by the specified group
	 *
	 * @param groupID
	 *            ID of the group you want to recieve land list for (You must be a
	 *            member of the group)
	 * @return Transaction (Query) ID which can be associated with results from your
	 *         request.
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch(UUID groupID) throws Exception {
		return StartPlacesSearch(DirFindFlags.GroupOwned, ParcelCategory.Any, Helpers.EmptyString, Helpers.EmptyString,
				groupID, UUID.GenerateUUID());
	}

	/**
	 * Search the Places directory for parcels that are listed in search and contain
	 * the specified keywords
	 *
	 * @param searchText
	 *            A string containing the keywords to search for
	 * @return Transaction (Query) ID which can be associated with results from your
	 *         request.
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch(String searchText) throws Exception {
		int flags = DirFindFlags.DwellSort | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartPlacesSearch(flags, ParcelCategory.Any, searchText, Helpers.EmptyString, UUID.Zero, new UUID());
	}

	/**
	 * Search Places - All Options
	 *
	 * @param findFlags
	 *            One of the Values from the DirFindFlags struct, ie: AgentOwned,
	 *            GroupOwned, etc.
	 * @param searchCategory
	 *            One of the values from the SearchCategory Struct, ie: Any, Linden,
	 *            Newcomer
	 * @param searchText
	 *            A string containing a list of keywords to search for separated by
	 *            a space character
	 * @param simulatorName
	 *            String Simulator Name to search in
	 * @param groupID
	 *            LLUID of group you want to recieve results for
	 * @param transactionID
	 *            Transaction (Query) ID which can be associated with results from
	 *            your request.
	 * @return Transaction (Query) ID which can be associated with results from your
	 *         request.
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch(int findFlags, ParcelCategory searchCategory, String searchText,
			String simulatorName, UUID groupID, UUID transactionID) throws Exception {
		PlacesQueryPacket find = new PlacesQueryPacket();
		find.AgentData.AgentID = _Client.Self.getAgentID();
		find.AgentData.SessionID = _Client.Self.getSessionID();
		find.AgentData.QueryID = groupID;

		find.TransactionID = transactionID;

		find.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		find.QueryData.QueryFlags = findFlags;
		find.QueryData.Category = (byte) searchCategory.ordinal();
		find.QueryData.setSimName(Helpers.StringToBytes(simulatorName));

		_Client.Network.sendPacket(find);
		return transactionID;
	}

	/**
	 * Search All Events with specifid searchText in all categories, includes PG,
	 * Mature and Adult
	 *
	 * @param searchText
	 *            A string containing a list of keywords to search for separated by
	 *            a space character
	 * @param queryStart
	 *            Each request is limited to 100 entries being returned. To get the
	 *            first group of entries of a request use 0, from 100-199 use 100,
	 *            200-299 use 200, etc.
	 * @return UUID of query to correlate results in callback.
	 * @throws Exception
	 */
	// TODO ORIGINAL LINE: public UUID StartEventsSearch(string searchText, uint
	// queryStart)
	public final UUID StartEventsSearch(String searchText, int queryStart) throws Exception {
		int flags = DirFindFlags.DateEvents | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartEventsSearch(searchText, flags, "u", queryStart, EventCategories.All);
	}

	/**
	 * Search Events
	 *
	 * @param searchText
	 *            A string containing a list of keywords to search for separated by
	 *            a space character
	 * @param queryFlags
	 *            One or more of the following flags: DateEvents, IncludePG,
	 *            IncludeMature, IncludeAdult from the <see cref="DirFindFlags"/>
	 *            Enum Multiple flags can be combined by separating the flags with
	 *            the or (|) operator
	 * @param eventDay
	 *            "u" for in-progress and upcoming events, -or- number of days
	 *            since/until event is scheduled For example "0" = Today, "1" =
	 *            tomorrow, "2" = following day, "-1" = yesterday, etc.
	 * @param queryStart
	 *            Each request is limited to 100 entries being returned. To get the
	 *            first group of entries of a request use 0, from 100-199 use 100,
	 *            200-299 use 200, etc.
	 * @param category
	 *            EventCategory event is listed under.
	 * @return UUID of query to correlate results in callback.
	 * @throws Exception
	 */
	// TODO ORIGINAL LINE: public UUID StartEventsSearch(string searchText,
	// DirFindFlags queryFlags, string eventDay, uint queryStart,
	// EventCategories category)
	public final UUID StartEventsSearch(String searchText, int queryFlags, String eventDay, int queryStart,
			EventCategories category) throws Exception, Exception {
		DirFindQueryPacket find = new DirFindQueryPacket();
		find.AgentData.AgentID = _Client.Self.getAgentID();
		find.AgentData.SessionID = _Client.Self.getSessionID();

		find.QueryData.QueryID = UUID.GenerateUUID();
		find.QueryData.setQueryText(Helpers.StringToBytes(eventDay + "|" + category.ordinal() + "|" + searchText));
		find.QueryData.QueryFlags = queryFlags;
		find.QueryData.QueryStart = queryStart;

		_Client.Network.sendPacket(find);
		return find.QueryData.QueryID;
	}

	/**
	 * Requests Event Details
	 *
	 * @param eventID
	 *            ID of Event returned from the <see cref="StartEventsSearch"/>
	 *            method
	 * @throws Exception
	 */
	// TODO ORIGINAL LINE: public void EventInfoRequest(uint eventID)
	public final void EventInfoRequest(int eventID) throws Exception {
		EventInfoRequestPacket find = new EventInfoRequestPacket();
		find.AgentData.AgentID = _Client.Self.getAgentID();
		find.AgentData.SessionID = _Client.Self.getSessionID();

		find.EventID = eventID;

		_Client.Network.sendPacket(find);
	}

	// /#region Blocking Functions

	/**
	 * deprecated: Use the async {@link StartPeopleSearch} method instead
	 */
	@Deprecated
	public final ArrayList<AgentSearchData> PeopleSearch(DirFindFlags findFlags, String searchText, int queryStart,
			int timeoutMS) throws Exception {
		class DirPeopleCallbackHandler implements Callback<DirPeopleReplyCallbackArgs> {
			private ArrayList<AgentSearchData> people = null;
			private UUID uuid;

			public ArrayList<AgentSearchData> getPeople() {
				return people;
			}

			@Override
			public boolean callback(DirPeopleReplyCallbackArgs e) {
				if (uuid == e.getQueryID()) {
					people = e.getMatchedPeople();
				}
				return false;
			}

			public DirPeopleCallbackHandler(UUID uuid) {
				this.uuid = uuid;
			}
		}

		DirPeopleCallbackHandler callback = new DirPeopleCallbackHandler(UUID.GenerateUUID());

		OnDirPeople.add(callback);
		StartPeopleSearch(searchText, queryStart);
		callback.wait(timeoutMS);
		OnDirPeople.remove(callback);

		return callback.getPeople();
	}

	// /#region Packet Handlers

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The simulator from which this packet originates
	 * @throws UnsupportedEncodingException
	 */
	private void HandleDirClassifiedReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnDirClassifieds.count() > 0) {
			DirClassifiedReplyPacket reply = (DirClassifiedReplyPacket) packet;
			ArrayList<Classified> classifieds = new ArrayList<Classified>();

			for (DirClassifiedReplyPacket.QueryRepliesBlock block : reply.QueryReplies) {
				Classified classified = new Classified();

				classified.CreationDate = Helpers.UnixTimeToDateTime(block.CreationDate);
				classified.ExpirationDate = Helpers.UnixTimeToDateTime(block.ExpirationDate);
				classified.Flags = ClassifiedFlags.setValue(block.ClassifiedFlags);
				classified.ID = block.ClassifiedID;
				classified.Name = Helpers.BytesToString(block.getName());
				classified.Price = block.PriceForListing;

				classifieds.add(classified);
			}
			OnDirClassifieds.dispatch(new DirClassifiedsReplyCallbackArgs(classifieds));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws UnsupportedEncodingException
	 */
	private void HandleDirLandReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnDirLand.count() > 0) {
			ArrayList<DirectoryParcel> parcelsForSale = new ArrayList<DirectoryParcel>();
			DirLandReplyPacket reply = (DirLandReplyPacket) packet;

			for (DirLandReplyPacket.QueryRepliesBlock block : reply.QueryReplies) {
				DirectoryParcel dirParcel = new DirectoryParcel();

				dirParcel.ActualArea = block.ActualArea;
				dirParcel.ID = block.ParcelID;
				dirParcel.Name = Helpers.BytesToString(block.getName());
				dirParcel.SalePrice = block.SalePrice;
				dirParcel.Auction = block.Auction;
				dirParcel.ForSale = block.ForSale;

				parcelsForSale.add(dirParcel);
			}
			OnDirLand.dispatch(new DirLandReplyCallbackArgs(parcelsForSale));
		}
	}

	/**
	 * Process an incoming <see cref="DirLandReplyMessage"/> event message
	 *
	 * @param message
	 *            The <see cref="DirLandReplyMessage"/> event message containing the
	 *            data
	 * @param simulator
	 *            The simulator the message originated from
	 */
	private void HandleDirLandReply(IMessage message, Simulator simulator) {
		if (OnDirLand.count() > 0) {
			ArrayList<DirectoryParcel> parcelsForSale = new ArrayList<DirectoryParcel>();
			DirLandReplyMessage reply = (DirLandReplyMessage) message;
			for (DirLandReplyMessage.QueryReply block : reply.queryReplies) {
				DirectoryParcel dirParcel = new DirectoryParcel();

				dirParcel.ActualArea = block.actualArea;
				dirParcel.ID = block.parcelID;
				dirParcel.Name = block.name;
				dirParcel.SalePrice = block.salePrice;
				dirParcel.Auction = block.auction;
				dirParcel.ForSale = block.forSale;

				parcelsForSale.add(dirParcel);
			}
			OnDirLand.dispatch(new DirLandReplyCallbackArgs(parcelsForSale));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	private void HandleDirPeopleReply(Packet packet, Simulator simulator) throws Exception {
		if (OnDirPeople.count() > 0) {
			DirPeopleReplyPacket peopleReply = (DirPeopleReplyPacket) ((packet instanceof DirPeopleReplyPacket) ? packet
					: null);
			ArrayList<AgentSearchData> matches = new ArrayList<AgentSearchData>(peopleReply.QueryReplies.length);

			for (DirPeopleReplyPacket.QueryRepliesBlock reply : peopleReply.QueryReplies) {
				AgentSearchData searchData = new AgentSearchData();
				searchData.Online = reply.Online;
				searchData.FirstName = Helpers.BytesToString(reply.getFirstName());
				searchData.LastName = Helpers.BytesToString(reply.getLastName());
				searchData.AgentID = reply.AgentID;
				matches.add(searchData);
			}
			OnDirPeople.dispatch(new DirPeopleReplyCallbackArgs(peopleReply.QueryID, matches));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	private void HandleDirGroupsReply(Packet packet, Simulator simulator) throws Exception {
		if (OnDirGroups.count() > 0) {
			DirGroupsReplyPacket groupsReply = (DirGroupsReplyPacket) packet;
			ArrayList<GroupSearchData> matches = new ArrayList<GroupSearchData>(groupsReply.QueryReplies.length);
			for (DirGroupsReplyPacket.QueryRepliesBlock reply : groupsReply.QueryReplies) {
				GroupSearchData groupsData = new GroupSearchData();
				groupsData.GroupID = reply.GroupID;
				groupsData.GroupName = Helpers.BytesToString(reply.getGroupName());
				groupsData.Members = reply.Members;
				matches.add(groupsData);
			}
			OnDirGroups.dispatch(new DirGroupsReplyCallbackArgs(groupsReply.QueryID, matches));
		}
	}

	/**
	 * Process an incoming <see cref="PlacesReplyMessage"/> event message
	 *
	 * @param message
	 *            The <see cref="PlacesReplyMessage"/> event message containing the
	 *            data
	 * @param simulator
	 *            The simulator the message originated from
	 */
	private void HandlePlacesReply(IMessage message, Simulator simulator) {
		if (OnPlaces.count() > 0) {
			ArrayList<PlacesSearchData> places = new ArrayList<PlacesSearchData>();
			PlacesReplyMessage replyMessage = (PlacesReplyMessage) message;
			for (PlacesReplyMessage.QueryData block : replyMessage.queryDataBlocks) {
				PlacesSearchData place = new PlacesSearchData();
				place.ActualArea = block.actualArea;
				place.BillableArea = block.billableArea;
				place.Desc = block.description;
				place.Dwell = block.dwell;
				place.Flags = PlacesFlags.setValue(block.flags);
				place.GlobalX = block.globalX;
				place.GlobalY = block.globalY;
				place.GlobalZ = block.globalZ;
				place.Name = block.name;
				place.OwnerID = block.ownerID;
				place.Price = block.price;
				place.SimName = block.simName;
				place.SnapshotID = block.snapShotID;
				place.SKU = block.productSku;
				places.add(place);
			}
			OnPlaces.dispatch(new PlacesReplyCallbackArgs(replyMessage.queryID, places));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	private void HandlePlacesReply(Packet packet, Simulator simulator) throws Exception {
		if (OnPlaces.count() > 0) {
			PlacesReplyPacket placesReply = (PlacesReplyPacket) ((packet instanceof PlacesReplyPacket) ? packet : null);
			ArrayList<PlacesSearchData> places = new ArrayList<PlacesSearchData>();

			for (PlacesReplyPacket.QueryDataBlock block : placesReply.QueryData) {
				PlacesSearchData place = new PlacesSearchData();
				place.OwnerID = block.OwnerID;
				place.Name = Helpers.BytesToString(block.getName());
				place.Desc = Helpers.BytesToString(block.getDesc());
				place.ActualArea = block.ActualArea;
				place.BillableArea = block.BillableArea;
				place.Flags = PlacesFlags.setValue(block.Flags);
				place.GlobalX = block.GlobalX;
				place.GlobalY = block.GlobalY;
				place.GlobalZ = block.GlobalZ;
				place.SimName = Helpers.BytesToString(block.getSimName());
				place.SnapshotID = block.SnapshotID;
				place.Dwell = block.Dwell;
				place.Price = block.Price;

				places.add(place);
			}
			OnPlaces.dispatch(new PlacesReplyCallbackArgs(placesReply.TransactionID, places));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	private void HandleEventsReply(Packet packet, Simulator simulator) throws Exception {
		if (OnDirEvents.count() > 0) {
			DirEventsReplyPacket eventsReply = (DirEventsReplyPacket) packet;
			ArrayList<EventsSearchData> matches = new ArrayList<EventsSearchData>(eventsReply.QueryReplies.length);

			for (DirEventsReplyPacket.QueryRepliesBlock reply : eventsReply.QueryReplies) {
				EventsSearchData eventsData = new EventsSearchData();
				eventsData.Owner = reply.OwnerID;
				eventsData.Name = Helpers.BytesToString(reply.getName());
				eventsData.ID = reply.EventID;
				eventsData.Date = Helpers.BytesToString(reply.getDate());
				eventsData.Time = reply.UnixTime;
				eventsData.Flags = EventFlags.setValue(reply.EventFlags);
				matches.add(eventsData);
			}
			OnDirEvents.dispatch(new DirEventsReplyCallbackArgs(eventsReply.QueryID, matches));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	private void HandleEventInfoReply(Packet packet, Simulator simulator) throws Exception {
		if (OnEventInfo.count() > 0) {
			EventInfoReplyPacket eventReply = (EventInfoReplyPacket) packet;
			EventInfo evinfo = new EventInfo();
			evinfo.ID = eventReply.EventData.EventID;
			evinfo.Name = Helpers.BytesToString(eventReply.EventData.getName());
			evinfo.Desc = Helpers.BytesToString(eventReply.EventData.getDesc());
			evinfo.Amount = eventReply.EventData.Amount;
			evinfo.Category = EventCategories.valueOf(Helpers.BytesToString(eventReply.EventData.getCategory()));
			evinfo.Cover = eventReply.EventData.Cover;
			evinfo.Creator = new UUID(eventReply.EventData.getCreator());
			evinfo.Date = Helpers.BytesToString(eventReply.EventData.getDate());
			evinfo.DateUTC = eventReply.EventData.DateUTC;
			evinfo.Duration = eventReply.EventData.Duration;
			evinfo.Flags = EventFlags.setValue(eventReply.EventData.EventFlags);
			evinfo.SimName = Helpers.BytesToString(eventReply.EventData.getSimName());
			evinfo.GlobalPos = eventReply.EventData.GlobalPos;

			OnEventInfo.dispatch(new EventInfoReplyCallbackArgs(evinfo));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private void HandleDirPlacesReply(Packet packet, Simulator simulator) throws Exception {
		if (OnDirPlaces.count() > 0) {
			DirPlacesReplyPacket reply = (DirPlacesReplyPacket) packet;
			ArrayList<DirectoryParcel> result = new ArrayList<DirectoryParcel>();

			for (QueryRepliesBlock block : reply.QueryReplies) {
				DirectoryParcel p = new DirectoryParcel();

				p.ID = block.ParcelID;
				p.Name = Helpers.BytesToString(block.getName());
				p.Dwell = block.Dwell;
				p.Auction = block.Auction;
				p.ForSale = block.ForSale;

				result.add(p);
			}

			OnDirPlaces.dispatch(new DirPlacesReplyCallbackArgs(reply.QueryID[0], result));
		}
	}
}
