/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
//import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.http.nio.concurrent.FutureCallback;

import libomv.Simulator;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.client.capabilities.CapsClient;
import libomv.packets.CoarseLocationUpdatePacket;
import libomv.packets.MapBlockReplyPacket;
import libomv.packets.MapBlockRequestPacket;
import libomv.packets.MapItemReplyPacket;
import libomv.packets.MapItemRequestPacket;
import libomv.packets.MapLayerReplyPacket;
import libomv.packets.MapLayerRequestPacket;
import libomv.packets.MapNameRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RegionHandleRequestPacket;
import libomv.packets.RegionIDAndHandleReplyPacket;
import libomv.packets.SimulatorViewerTimeMessagePacket;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.PacketCallback;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.CallbackArgs;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

// Manages grid-wide tasks such as the world map
public class GridManager implements PacketCallback
{
	/* Map layer request type */
	public enum GridLayerType
	{
		/* Objects and terrain are shown */
		Objects,
		/* Only the terrain is shown, no objects */
		Terrain,
		/* Overlay showing land for sale and for auction */
		LandForSale
	}

	/* Type of grid item, such as telehub, event, populator location, etc. */
	public enum GridItemType
	{
		Nothing, Telehub, PgEvent, MatureEvent, Popular, Unused1, AgentLocations, LandForSale, Classified, AdultEvent, AdultLandForSale;

		public static GridItemType convert(int value)
		{
			GridItemType values[] = GridItemType.values();

			for (int i = 0; i < values.length; i++)
				if (values[i].ordinal() == value)
					return values[i];
			return null;
		}
	}

	public final class GridLayer
	{
		public int Bottom;
		public int Left;
		public int Top;
		public int Right;
		public UUID ImageID;

		public boolean ContainsRegion(int x, int y)
		{
			return (x >= Left && x <= Right && y >= Bottom && y <= Top);
		}
	}

	/* Class for regions on the world map */
	public class GridRegion
	{
		// Sim X position on World Map
		public int X;
		// Sim Y position on World Map
		public int Y;
		// Sim Name (NOTE: In lowercase!)
		public String Name;
		//
		public byte Access;
		// Various flags for the region (presumably things like PG/Mature)
		public int RegionFlags;
		// Sim's defined Water Height
		public byte WaterHeight;
		//
		public byte Agents;
		// UUID of the World Map image
		public UUID MapImageID;
		// Used for teleporting
		public long RegionHandle;

		// Constructor
		public GridRegion()
		{
		}

		public GridRegion(String name)
		{
			Name = name;
		}

		@Override
		public String toString()
		{
			return String.format("%s (%d/%d), Handle: %d, MapImage: %s, Access: %d, Flags: 0x%8x", Name, X, Y,
					RegionHandle, MapImageID.toString(), Access, RegionFlags);
		}

		@Override
		public int hashCode()
		{
			return ((Integer) X).hashCode() ^ ((Integer) Y).hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof GridRegion)
			{
				return equals((GridRegion) obj);
			}
			return false;
		}

		private boolean equals(GridRegion region)
		{
			return (this.X == region.X && this.Y == region.Y);
		}
	}

	/** Map Items */
	public class MapItem
	{
		private UUID ID;
		private String Name;
		private Vector3 GlobalPos;

		/* Represents an agent or group of agents location */ 
		public int AvatarCount;

		/* For adult and normal land for sale */
		public int Size;
		public int Price;
		
		public boolean isInfoHub;

		/* For evnts */
		public Date DateTime;

		public final UUID getUUID()
		{
			return ID;
		}
		
		public final String getName()
		{
			return Name;
		}
		
		/* Get the Local X position of the item */
		public final float getLocalX()
		{
			return GlobalPos.X % 256;
		}

		/* Get the Local Y position of the item */
		public final float getLocalY()
		{
			return GlobalPos.Y % 256;
		}

		public final Vector3 getGlobalPosition()
		{
			return GlobalPos;
		}
		
		public final void setEvelation(float z)
		{
			GlobalPos.Z = z;
		}
		
		/* Get the Handle of the region */
		public final long getRegionHandle()
		{
			return Helpers.IntsToLong((int)(GlobalPos.X - (GlobalPos.X % 256)), (int)(GlobalPos.Y - (GlobalPos.Y % 256)));
		}
		
		public MapItem(float x, float y, UUID id, String name)
		{
			GlobalPos = new Vector3(x, y, 40);
			ID = id;
			Name = name;
		}
	}

	/* Unknown */
	public final float getSunPhase()
	{
		return sunPhase;
	}

	/* Current direction of the sun */
	public final Vector3 getSunDirection()
	{
		return sunDirection;
	}

	/* Current angular velocity of the sun */
	public final Vector3 getSunAngVelocity()
	{
		return sunAngVelocity;
	}

	/* Current world time */
	public final Date getWorldTime()
	{
		return getWorldTime();
	}
	
    /* Microseconds since the start of SL 4-hour day */
    public long getTimeOfDay()
    {
    	return timeOfDay;
    }

	// A dictionary of all the regions, indexed by region name
	public HashMap<String, GridRegion> Regions;
	// A dictionary of all the regions, indexed by region handle
	private HashMap<Long, GridRegion> RegionsByHandle = new HashMap<Long, GridRegion>();

	// Current direction of the sun
	private float sunPhase;
	private Vector3 sunDirection;
	private Vector3 sunAngVelocity;
	private long timeOfDay;

	private GridClient _Client;

	public CallbackHandler<GridLayerCallbackArgs> OnGridLayer = new CallbackHandler<GridLayerCallbackArgs>();
	public CallbackHandler<GridItemsCallbackArgs> OnGridItems = new CallbackHandler<GridItemsCallbackArgs>();
	public CallbackHandler<GridRegionCallbackArgs> OnGridRegion = new CallbackHandler<GridRegionCallbackArgs>();
	public CallbackHandler<RegionHandleReplyCallbackArgs> OnRegionHandleReply = new CallbackHandler<RegionHandleReplyCallbackArgs>();
	public CallbackHandler<CoarseLocationUpdateCallbackArgs> OnCoarseLocationUpdate = new CallbackHandler<CoarseLocationUpdateCallbackArgs>();

	// Constructor
	// <param name="client">Instance of ClientManager to associate with this
	// GridManager instance</param>
	public GridManager(GridClient client)
	{
		_Client = client;
		Regions = new HashMap<String, GridRegion>();
		sunDirection = new Vector3(0.0f);

		_Client.Network.RegisterCallback(PacketType.MapLayerReply, this);
		_Client.Network.RegisterCallback(PacketType.MapBlockReply, this);
		_Client.Network.RegisterCallback(PacketType.MapItemReply, this);
		_Client.Network.RegisterCallback(PacketType.SimulatorViewerTimeMessage, this);
		_Client.Network.RegisterCallback(PacketType.CoarseLocationUpdate, this);
		_Client.Network.RegisterCallback(PacketType.RegionIDAndHandleReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case MapLayerReply:
				HandleMapLayerReply(packet, simulator);
				break;
			case MapBlockReply:
				HandleMapBlockReply(packet, simulator);
				break;
			case MapItemReply:
				HandleMapItemReply(packet, simulator);
				break;
			case SimulatorViewerTimeMessage:
				HandleSimulatorViewerTimeMessage(packet, simulator);
				break;
			case CoarseLocationUpdate:
				HandleCoarseLocation(packet, simulator);
				break;
			case RegionIDAndHandleReply:
				HandleRegionHandleReply(packet, simulator);
				break;
			default:
				break;
		}
	}

	public final void RequestMapLayer(GridLayerType layer)
	{
		final class MapLayerCallback implements FutureCallback<OSD>
		{
			@Override
			public void completed(OSD result)
			{
				OSDMap body = (OSDMap) result;
				OSDArray layerData = (OSDArray) body.get("LayerData");

				if (OnGridLayer.count() > 0)
				{
					for (int i = 0; i < layerData.size(); i++)
					{
						OSDMap thisLayerData = (OSDMap) layerData.get(i);

						GridLayer layer = new GridLayer();
						layer.Bottom = thisLayerData.get("Bottom").AsInteger();
						layer.Left = thisLayerData.get("Left").AsInteger();
						layer.Top = thisLayerData.get("Top").AsInteger();
						layer.Right = thisLayerData.get("Right").AsInteger();
						layer.ImageID = thisLayerData.get("ImageID").AsUUID();

						OnGridLayer.dispatch(new GridLayerCallbackArgs(layer));
					}
				}
				if (body.containsKey("MapBlocks"))
				{
					// TODO: At one point this will become activated
					Logger.Log("Got MapBlocks through CAPS, please finish this function!", LogLevel.Error);
				}
			}

			@Override
			public void failed(Exception ex)
			{
                Logger.Log("MapLayerReplyHandler error: " + ex.getMessage() + ": " + ex.getStackTrace(), LogLevel.Error, _Client);
			}

			@Override
			public void cancelled()
			{
			}
		}

		URI url = _Client.Network.getCapabilityURI(CapsEventType.MapLayer.toString());
		if (url != null)
		{
			OSDMap body = new OSDMap();
			body.put("Flags", OSD.FromInteger(layer.ordinal()));

			try
			{
				CapsClient request = new CapsClient(_Client, CapsEventType.MapLayer.toString());
				request.executeHttpPost(url, body, OSDFormat.Xml, new MapLayerCallback(), _Client.Settings.CAPS_TIMEOUT);
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * If the client does not have data on this region already, request the
	 * region data for it
	 * 
	 * @param regionName
	 *            The name of the region
	 * @param layer
	 *            The type of layer
	 * @throws Exception
	 */
	public final void RequestMapRegion(String regionName, GridLayerType layer) throws Exception
	{
		MapNameRequestPacket request = new MapNameRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.AgentData.Flags = (layer != null) ? layer.ordinal() : 0;
		request.AgentData.EstateID = 0; // Filled in on the sim
		request.AgentData.Godlike = false; // Filled in on the sim
		request.NameData.setName(Helpers.StringToBytes(regionName));

		_Client.Network.sendPacket(request);
	}

	/**
	 * Request a map block
	 * 
	 * @param layer
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @param returnNonExistent
	 * @throws Exception
	 */
	public final void RequestMapBlocks(GridLayerType layer, int minX, int minY, int maxX, int maxY,
			boolean returnNonExistent) throws Exception
	{
		MapBlockRequestPacket request = new MapBlockRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.AgentData.Flags = layer.ordinal();
		request.AgentData.Flags |= returnNonExistent ? 0x10000 : 0;
		request.AgentData.EstateID = 0; // Filled in at the simulator
		request.AgentData.Godlike = false; // Filled in at the simulator

		request.PositionData.MinX = (short) (minX & 0xFFFF);
		request.PositionData.MinY = (short) (minY & 0xFFFF);
		request.PositionData.MaxX = (short) (maxX & 0xFFFF);
		request.PositionData.MaxY = (short) (maxY & 0xFFFF);

		_Client.Network.sendPacket(request);
	}

	// Fire off packet for Estate/Island sim data request.
	public void RequestMapLayerOld() throws Exception
	{
		MapLayerRequestPacket request = new MapLayerRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.AgentData.Flags = 0;
		request.AgentData.EstateID = 0;
		request.AgentData.Godlike = false;

		_Client.Network.sendPacket(request);
	}

    /**
     * 
     *
     * @param regionHandle
     * @param item
     * @param layer
     * @param timeoutMS
     * @throws Exception 
     * @returns
     */
    public List<MapItem> MapItems(long regionHandle, GridItemType item, GridLayerType layer, int timeoutMS) throws Exception
    {
        final TimeoutEvent<List<MapItem>> itemsEvent = new TimeoutEvent<List<MapItem>>();

        class GridItemsCallback implements Callback<GridItemsCallbackArgs>
        {
            public boolean callback(GridItemsCallbackArgs args)
            {
                if (args.getType().equals(GridItemType.AgentLocations))
                {
                    itemsEvent.set(args.getItems());
                }
                return false;
            }
        }

        Callback<GridItemsCallbackArgs> callback = new GridItemsCallback();
        OnGridItems.add(callback);

        RequestMapItems(regionHandle, item, layer);
        List<MapItem> itemList = itemsEvent.waitOne(timeoutMS);

        OnGridItems.remove(callback);

        return itemList;
    }

    /**
	 * 
	 * @param regionHandle
	 * @param regionHandle
	 * @param item
	 * @param layer
	 * @throws Exception
	 */
	public final void RequestMapItems(long regionHandle, GridItemType item, GridLayerType layer) throws Exception
	{
		MapItemRequestPacket request = new MapItemRequestPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.AgentData.Flags = layer.ordinal();
		request.AgentData.Godlike = false; // Filled in on the sim
		request.AgentData.EstateID = 0; // Filled in on the sim

		request.RequestData.ItemType = item.ordinal();
		request.RequestData.RegionHandle = regionHandle;

		_Client.Network.sendPacket(request);
	}

	/* Request data for all mainland (Linden managed) simulators */
	public final void RequestMainlandSims(GridLayerType layer) throws Exception
	{
		RequestMapBlocks(layer, 0, 0, 65535, 65535, false);
	}

	/**
	 * Request the region handle for the specified region UUID
	 * 
	 * @param regionID
	 *            UUID of the region to look up
	 * @throws Exception
	 */
	public final void RequestRegionHandle(UUID regionID) throws Exception
	{
		RegionHandleRequestPacket request = new RegionHandleRequestPacket();
		request.RegionID = regionID;
		_Client.Network.sendPacket(request);
	}

	/**
	 * Get grid region information using the region name, this function will
	 * block until it can find the region or gives up Example: regiondata =
	 * GetGridRegion("Ahern");
	 * 
	 * @param name
	 *            Name of sim you're looking for
	 * @param layer
	 *            Layer that you are requesting
	 * @return A GridRegion for the sim you're looking for if successful,
	 *         otherwise null
	 * @throws Exception
	 */
	public final GridRegion GetGridRegion(String name, GridLayerType type) throws Exception
	{
		if (name == null || name.isEmpty())
		{
			Logger.Log("GetGridRegion called with a null or empty region name", LogLevel.Error);
			return null;
		}

		GridRegion region = Regions.get(name);
		if (region != null)
		{
			return region;
		}

		final class OnGridRegionCallback implements Callback<GridRegionCallbackArgs>
		{
			private String Name;

			@Override
			public boolean callback(GridRegionCallbackArgs args)
			{
				if (args.getRegion().Name.equals(Name))
				{
					synchronized (Name) { Name.notifyAll(); }
				}
				return false;
			}

			public OnGridRegionCallback(String name)
			{
				Name = name;
			}
		}

		Callback<GridRegionCallbackArgs> callback = new OnGridRegionCallback(name);
		OnGridRegion.add(callback);
		RequestMapRegion(name, type);
		synchronized (name)
		{
			name.wait(_Client.Settings.MAP_REQUEST_TIMEOUT);
		}
		OnGridRegion.remove(callback);

		region = Regions.get(name);
		if (region == null)
		{
			Logger.Log("Couldn't find region " + name, LogLevel.Warning);
		}
		return region;
	}

	public void BeginGetGridRegion(String name, GridLayerType type, Callback<GridRegionCallbackArgs> grc) throws Exception
	{
		GridRegion region = Regions.get(name);
		if (region != null)
		{
			grc.callback(new GridRegionCallbackArgs(region));
		}
		else
		{
			OnGridRegion.add(grc);
			RequestMapRegion(name, type);
		}
	}

	public void RemoveGridRegionCallback(Callback<GridRegionCallbackArgs> grc)
	{
		OnGridRegion.remove(grc);
	}

    /**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The simulator from which this was received
	 * @param packet
	 *            The packet data
	 */
	private void HandleMapLayerReply(Packet packet, Simulator simulator) throws Exception
	{
		MapLayerReplyPacket map = (MapLayerReplyPacket) packet;

		if (OnGridLayer.count() > 0)
		{
			for (int i = 0; i < map.LayerData.length; i++)
			{
				GridLayer layer = new GridLayer();
				;
				layer.Bottom = map.LayerData[i].Bottom;
				layer.Left = map.LayerData[i].Left;
				layer.Top = map.LayerData[i].Top;
				layer.Right = map.LayerData[i].Right;
				layer.ImageID = map.LayerData[i].ImageID;

				OnGridLayer.dispatch(new GridLayerCallbackArgs(layer));
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The simulator from which this was received
	 * @param packet
	 *            The packet data
	 */
	private void HandleMapBlockReply(Packet packet, Simulator simulator) throws Exception
	{
		MapBlockReplyPacket map = (MapBlockReplyPacket) packet;

		for (MapBlockReplyPacket.DataBlock block : map.Data)
		{
            if (block.X != 0 || block.Y != 0)
            {
            	GridRegion region = new GridRegion(Helpers.BytesToString(block.getName()));

            	region.X = block.X;
            	region.Y = block.Y;
                region.Name = Helpers.BytesToString(block.getName());
                // RegionFlags seems to always be zero here?
                region.RegionFlags = block.RegionFlags;
            	region.WaterHeight = block.WaterHeight;
            	region.Agents = block.Agents;
            	region.Access = block.Access;
            	region.MapImageID = block.MapImageID;
            	region.RegionHandle = Helpers.IntsToLong(region.X * 256, region.Y * 256);

            	synchronized (Regions)
            	{
            		if (region.Name != null)
            			Regions.put(region.Name, region);
            		RegionsByHandle.put(region.RegionHandle, region);
            	}

            	if (OnGridRegion.count() > 0)
            	{
            		OnGridRegion.dispatch(new GridRegionCallbackArgs(region));
            	}
            }
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The simulator from which this was received
	 * @param packet
	 *            The packet data
	 * @throws Exception
	 */
	private void HandleMapItemReply(Packet packet, Simulator simulator) throws Exception
	{
		if (OnGridItems.count() > 0)
		{
			MapItemReplyPacket reply = (MapItemReplyPacket) packet;
			GridItemType type = GridItemType.convert(reply.ItemType);
			ArrayList<MapItem> items = new ArrayList<MapItem>();

			for (int i = 0; i < reply.Data.length; i++)
			{
				String name = Helpers.BytesToString(reply.Data[i].getName());
				MapItem item = new MapItem(reply.Data[i].X & 0xFFFFFFFFL, reply.Data[i].Y & 0xFFFFFFFFL, reply.Data[i].ID, name);

				switch (type)
				{
					case Telehub:
						item.isInfoHub = reply.Data[i].Extra2 != 0;
						items.add(item);
						break;
					case AgentLocations:
						item.AvatarCount = reply.Data[i].Extra;
						items.add(item);
						break;
					case LandForSale:
					case AdultLandForSale:
						item.Size = reply.Data[i].Extra;
						item.Price = reply.Data[i].Extra2;
						items.add(item);
						break;
					case PgEvent:
					case MatureEvent:
					case AdultEvent:
						item.DateTime = Helpers.UnixTimeToDateTime(reply.Data[i].Extra);
						item.setEvelation(reply.Data[i].Extra2);
						items.add(item);
						break;
					case Classified:
						// DEPRECATED: not used anymore
						Logger.Log("FIXME: Classified MapItem", LogLevel.Error, _Client);
						break;
					case Popular:
						// FIXME:
						Logger.Log("FIXME: Popular MapItem", LogLevel.Error, _Client);
						break;
					default:
						Logger.Log("Unknown map item type " + type, LogLevel.Warning, _Client);
						break;
				}
			}
			OnGridItems.dispatch(new GridItemsCallbackArgs(type, items));
		}
	}

	/**
	 * Get sim time from the appropriate packet
	 * 
	 * @param packet
	 * @param simulator
	 */
	private final void HandleSimulatorViewerTimeMessage(Packet packet, Simulator simulator)
	{
		SimulatorViewerTimeMessagePacket time = (SimulatorViewerTimeMessagePacket) packet;

		sunPhase = time.TimeInfo.SunPhase;
		sunDirection = time.TimeInfo.SunDirection;
		sunAngVelocity = time.TimeInfo.SunAngVelocity;
        timeOfDay = time.TimeInfo.UsecSinceStart;

		/*
		 * TODO: Does anyone have a use for the time stuff?
		 * time.TimeInfo.SecPerDay; time.TimeInfo.SecPerYear;
		 * time.TimeInfo.UsecSinceStart;
		 */
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The simulator from which this was received
	 * @param packet
	 *            The packet data
	 */
	private final void HandleCoarseLocation(Packet packet, Simulator simulator)
	{
		CoarseLocationUpdatePacket coarse = (CoarseLocationUpdatePacket) packet;

		// populate a dictionary from the packet, for local use
		HashMap<UUID, Vector3> coarseEntries = new HashMap<UUID, Vector3>();
		for (int i = 0; i < coarse.AgentID.length; i++)
		{
			if (coarse.Location.length > i)
			{
				coarseEntries.put(coarse.AgentID[i], new Vector3(coarse.Location[i].X, coarse.Location[i].Y,
						coarse.Location[i].Z * 4));
			}

			// the friend we are tracking on radar
			if (i == coarse.Index.Prey)
			{
				simulator.setPreyID(coarse.AgentID[i]);
			}
		}

		HashMap<UUID, Vector3> positions;
		ArrayList<UUID> removedEntries = new ArrayList<UUID>();
		ArrayList<UUID> newEntries = new ArrayList<UUID>();

		synchronized (positions = simulator.getAvatarPositions())
		{
			// find stale entries (people who left the sim)
			for (UUID findID : positions.keySet())
			{
				if (!coarseEntries.containsKey(findID))
					removedEntries.add(findID);
			}

			// remove stale entries
			for (UUID trackedID : removedEntries)
			{
				positions.remove(trackedID);
			}

			// add or update tracked info, and record who is new
			for (Entry<UUID, Vector3> entry : coarseEntries.entrySet())
			{
				if (!positions.containsKey(entry.getKey()))
				{
					newEntries.add(entry.getKey());
				}
				positions.put(entry.getKey(), entry.getValue());
			}
		}

		if (OnCoarseLocationUpdate != null)
		{
			OnCoarseLocationUpdate.dispatch(new CoarseLocationUpdateCallbackArgs(simulator, newEntries, removedEntries));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The simulator from which this was received
	 * @param packet
	 *            The packet data
	 */
	private final void HandleRegionHandleReply(Packet packet, Simulator simulator)
	{
		if (OnRegionHandleReply != null)
		{
			RegionIDAndHandleReplyPacket reply = (RegionIDAndHandleReplyPacket) packet;
			OnRegionHandleReply.dispatch(new RegionHandleReplyCallbackArgs(reply.ReplyBlock.RegionID,
					reply.ReplyBlock.RegionHandle));
		}
	}

	// /#region EventArgs classes

	public class CoarseLocationUpdateCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final ArrayList<UUID> m_NewEntries;
		private final ArrayList<UUID> m_RemovedEntries;

		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		public final ArrayList<UUID> getNewEntries()
		{
			return m_NewEntries;
		}

		public final ArrayList<UUID> getRemovedEntries()
		{
			return m_RemovedEntries;
		}

		public CoarseLocationUpdateCallbackArgs(Simulator simulator, ArrayList<UUID> newEntries,
				ArrayList<UUID> removedEntries)
		{
			this.m_Simulator = simulator;
			this.m_NewEntries = newEntries;
			this.m_RemovedEntries = removedEntries;
		}
	}

	public class GridRegionCallbackArgs implements CallbackArgs
	{
		private final GridRegion m_Region;

		public final GridRegion getRegion()
		{
			return m_Region;
		}

		public GridRegionCallbackArgs(GridRegion region)
		{
			this.m_Region = region;
		}
	}

	public class GridLayerCallbackArgs implements CallbackArgs
	{
		private final GridLayer m_Layer;

		public final GridLayer getLayer()
		{
			return m_Layer;
		}

		public GridLayerCallbackArgs(GridLayer layer)
		{
			this.m_Layer = layer;
		}
	}

	public class GridItemsCallbackArgs implements CallbackArgs
	{
		private final GridItemType m_Type;
		private final List<MapItem> m_Items;

		public final GridItemType getType()
		{
			return m_Type;
		}

		public final List<MapItem> getItems()
		{
			return m_Items;
		}

		public GridItemsCallbackArgs(GridItemType type, List<MapItem> items)
		{
			this.m_Type = type;
			this.m_Items = items;
		}
	}

	public class RegionHandleReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_RegionID;
		// TODO was: private readonly ulong m_RegionHandle;
		private final long m_RegionHandle;

		public final UUID getRegionID()
		{
			return m_RegionID;
		}

		// TODO was: public ulong getRegionHandle()
		public final long getRegionHandle()
		{
			return m_RegionHandle;
		}

		// TODO was: public RegionHandleReplyEventArgs(UUID regionID, ulong
		// regionHandle)
		public RegionHandleReplyCallbackArgs(UUID regionID, long regionHandle)
		{
			this.m_RegionID = regionID;
			this.m_RegionHandle = regionHandle;
		}
	}
}
