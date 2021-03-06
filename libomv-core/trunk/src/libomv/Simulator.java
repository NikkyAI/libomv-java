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
package libomv;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import libomv.ParcelManager.Parcel;
import libomv.Statistics.Type;
import libomv.TerrainManager.TerrainPatch;
import libomv.capabilities.CapsManager;
import libomv.packets.AgentPausePacket;
import libomv.packets.AgentResumePacket;
import libomv.packets.CloseCircuitPacket;
import libomv.packets.Packet;
import libomv.packets.PacketAckPacket;
import libomv.packets.PacketType;
import libomv.packets.StartPingCheckPacket;
import libomv.packets.UseCircuitCodePacket;
import libomv.primitives.Avatar;
import libomv.primitives.Primitive;
import libomv.types.PacketHeader;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

// Simulator is a wrapper for a network connection to a simulator and the
// Region class representing the block of land in the metaverse.
public class Simulator extends Thread
{
	/* Simulator (region) properties */
	// [Flags]
	public static class RegionFlags
	{
		/* No flags set */
		public static final long None = 0;
		/* Agents can take damage and be killed */
		public static final long AllowDamage = 1 << 0;
		/* Landmarks can be created here */
		public static final long AllowLandmark = 1 << 1;
		/* Home position can be set in this sim */
		public static final long AllowSetHome = 1 << 2;
		/* Home position is reset when an agent teleports away */
		public static final long ResetHomeOnTeleport = 1 << 3;
		/* Sun does not move */
		public static final long SunFixed = 1 << 4;
		/* Allows private parcels (ie. banlines) */
		public static final long AllowAccessOverride = 1 << 5;
		/* Disable heightmap alterations (agents can still plant foliage) */
		public static final long BlockTerraform = 1 << 6;
		/* Land cannot be released, sold, or purchased */
		public static final long BlockLandResell = 1 << 7;
		/* All content is wiped nightly */
		public static final long Sandbox = 1 << 8;
		/*
		 * Unknown: Related to the availability of an overview world map
		 * tile.(Think mainland images when zoomed out.)
		 */
		public static final long NullLayer = 1 << 9;
		/*
		 * Unknown: Related to region debug flags. Possibly to skip processing
		 * of agent interaction with world.
		 */
		public static final long SkipAgentAction = 1 << 10;
		/*
		 * Region does not update agent prim interest lists. Internal debugging
		 * option.
		 */
		public static final long SkipUpdateInterestList = 1 << 11;
		/* No collision detection for non-agent objects */
		public static final long SkipCollisions = 1 << 12;
		/* No scripts are ran */
		public static final long SkipScripts = 1 << 13;
		/* All physics processing is turned off */
		public static final long SkipPhysics = 1 << 14;
		/*
		 * Region can be seen from other regions on world map. (Legacy world map
		 * option?)
		 */
		public static final long ExternallyVisible = 1 << 15;
		/*
		 * Region can be seen from mainland on world map. (Legacy world map
		 * option?)
		 */
		public static final long MainlandVisible = 1 << 16;
		/* Agents not explicitly on the access list can visit the region. */
		public static final long PublicAllowed = 1 << 17;
		/*
		 * Traffic calculations are not run across entire region, overrides
		 * parcel settings.
		 */
		public static final long BlockDwell = 1 << 18;
		/* Flight is disabled (not currently enforced by the sim) */
		public static final long NoFly = 1 << 19;
		/* Allow direct (p2p) teleporting */
		public static final long AllowDirectTeleport = 1 << 20;
		/* Estate owner has temporarily disabled scripting */
		public static final long EstateSkipScripts = 1 << 21;
		/*
		 * Restricts the usage of the LSL llPushObject function, applies to
		 * whole region.
		 */
		public static final long RestrictPushObject = 1 << 22;
		/* Deny agents with no payment info on file */
		public static final long DenyAnonymous = 1 << 23;
		/* Deny agents with payment info on file */
		public static final long DenyIdentified = 1 << 24;
		/* Deny agents who have made a monetary transaction */
		public static final long DenyTransacted = 1 << 25;
		/*
		 * Parcels within the region may be joined or divided by anyone, not
		 * just estate owners/managers.
		 */
		public static final long AllowParcelChanges = 1 << 26;
		/*
		 * Abuse reports sent from within this region are sent to the estate
		 * owner defined email.
		 */
		public static final long AbuseEmailToEstateOwner = 1 << 27;
		/* Region is Voice Enabled */
		public static final long AllowVoice = 1 << 28;
		/*
		 * Removes the ability from parcel owners to set their parcels to show
		 * in search.
		 */
		public static final long BlockParcelSearch = 1 << 29;
		/* Deny agents who have not been age verified from entering the region. */
		public static final long DenyAgeUnverified = 1 << 30;

		public static long setValue(long value)
		{
			return value & _mask;
		}

		public static long getValue(long value)
		{
			return value;
		}

		private static final long _mask = 0xFFFFFFFFFL;
	}

	/* Region protocol flags */
	// [Flags]
    public static class RegionProtocols
    {
    	// Nothing special
    	public static final long None = 0;
    	// Region supports Server side Appearance
    	public static final long AgentAppearanceService = 1 << 0;
    	// Viewer supports Server side Appearance
    	public static final long SelfAppearanceSupport = 1 << 2;

    	public static long setValue(long value)
		{
			return value & _mask;
		}

		public static long getValue(long value)
		{
			return value;
		}

		private static final long _mask = 0x7FFFFFFFL;
    }
  	
  	/* Access level for a simulator */
	public static enum SimAccess
	{
		/* Minimum access level, no additional checks */
		Min(0),
		/* Trial accounts allowed */
		Trial(7),                 //          4 + 2 + 1
		/* PG rating */
		PG(13),                   //  8 +     4     + 1
		/* Mature rating */
		Mature(21),               // 16 +     4     + 1
		/* Adult rating */
		Adult(42),                // 32 + 8 + 4
		/* Simulator is offline */
		Down(0xFE),
		/* Simulator does not exist */
		NonExistent(0xFF);

		public static SimAccess setValue(int value)
		{
			for (SimAccess e : values())
			{
				if (e._value == value)
					return e;
			}
			return Min;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private SimAccess(int value)
		{
			_value = (byte) value;
		}
	}

	public static enum SimStatType
	{
		Unknown,		// -1
		TimeDilation,   // 0
		SimFPS,
		PhysicsFPS,
		AgentUpdates,
		FrameMS,
		NetMS,
		OtherMS,
		PhysicsMS,
		AgentMS,
		ImageMS,
		ScriptMS,       // 10
		TotalPrim,
		ActivePrim,
		Agents,
		ChildAgents,
		ActiveScripts,
		ScriptInstructionsPerSecond,
		InPacketsPerSecond,
		OutPacketsPerSecond,
		PendingDownloads,
		PendingUploads, // 20
		VirtualSizeKB,
		ResidentSizeKB,
		PendingLocalUploads,
		UnAckedBytes,
		
		PhysicsPinnedTasks,
		PhysicsLODTasks,
		PhysicsStepMS,
		PhysicsShapeMS,
		PhysicsOtherMS,
		PhysicsMemory, // 30

		ScriptEPS,
		SimSpareTime,
		SimSleepTime,
		SimIOPumpTime,
		SimPctScriptsRun,    // 35
		SimRegionIdle,         // dataserver only
		SimRegionIdlePossible, // dataserver only
	    SimAIStepMsec,
		SimSkippedSilhouetteSteps,
		SimPctSteppedCharacters;   //40
		
		public static SimStatType setValue(int value)
		{
			if (value >= 0 && value < values().length - 1)
				return values()[value + 1];
			return Unknown;
		}

		public static int getValue(SimStatType type)
		{
			return type.ordinal();
		}
	}

	/* Simulator Statistics */
	public final class SimStats
	{
		/* Total number of packets sent by this simulator from this agent */
		public long SentPackets;
		/* Total number of packets received by this simulator to this agent */
		public long RecvPackets;
		/* Total number of bytes sent by this simulator to this agent */
		public long SentBytes;
		/* Total number of bytes received by this simulator to this agent */
		public long RecvBytes;
		/* Time in seconds agent has been connected to simulator */
		public long ConnectTime;
		/* Total number of packets that have been resent */
		public long ResentPackets;
		/* Total number of resent packets received */
		public long ReceivedResends;
		/* Total number of pings sent to this simulator by this agent */
		public long SentPings;
		/* Total number of ping replies sent to this agent by this simulator */
		public long ReceivedPongs;
		/*
		 * Incoming bytes per second
		 * 
		 * It would be nice to have this calculated on the fly, but this is far,
		 * far easier
		 */
		public int IncomingBPS;
		/*
		 * Outgoing bytes per second
		 * 
		 * It would be nice to have this claculated on the fly, but this is far,
		 * far easier
		 */
		public int OutgoingBPS;
		/* Time last ping was sent */
		public long LastPingSent;
		/*  */
		public long LastLag;
		/* ID of last Ping sent */
		public byte LastPingID;
		/*  */
		public long MissedPings;
		/* Current time dilation of this simulator */
		public float Dilation;
		/* Current Frames per second of simulator */
		public int FPS;
		/* Current Physics frames per second of simulator */
		public float PhysicsFPS;
		/*  */
		public float AgentUpdates;
		/*  */
		public float FrameTime;
		/*  */
		public float NetTime;
		/*  */
		public float PhysicsTime;
		/*  */
		public float ImageTime;
		/*  */
		public float ScriptTime;
		/*  */
		public float AgentTime;
		/*  */
		public float OtherTime;
		/* Total number of objects Simulator is simulating */
		public int Objects;
		/* Total number of Active (Scripted) objects running */
		public int ScriptedObjects;
		/* Number of agents currently in this simulator */
		public int Agents;
		/* Number of agents in neighbor simulators */
		public int ChildAgents;
		/* Number of Active scripts running in this simulator */
		public int ActiveScripts;
		/*  */
		public int LSLIPS;
		/*  */
		public int INPPS;
		/*  */
		public int OUTPPS;
		/* Number of downloads pending */
		public int PendingDownloads;
		/* Number of uploads pending */
		public int PendingUploads;
		/*  */
		public int VirtualSize;
		/*  */
		public int ResidentSize;
		/* Number of local uploads pending */
		public int PendingLocalUploads;
		/* Unacknowledged bytes in queue */
		public int UnackedBytes;

		public int PhysicsPinnedTasks;
		public int PhysicsLODTasks;
		public int PhysicsStepMS;
		public int PhysicsShapeMS;
		public int PhysicsOtherMS;
		public int PhysicsMemory;
		public int ScriptEPS;
		public int SimSpareTime;
		public int SimSleepTime;
		public int SimIOPumpTime;
		public int SimPctScriptsRun;
		public int SimAIStepMsec;
		public int SimSkippedSilhouetteSteps;
		public int SimPctSteppedCharacters;
	}

	public final class IncomingPacketIDCollection
	{
		private final int[] Items;
		private HashSet<Integer> hashSet;
		private int first = 0;
		private int next = 0;
		private int capacity;

		public IncomingPacketIDCollection(int capacity)
		{
			this.capacity = capacity;
			Items = new int[capacity];
			hashSet = new HashSet<Integer>();
		}

		public boolean tryEnqueue(int ack)
		{
			synchronized (hashSet)
			{
				if (hashSet.add(ack))
				{
					Items[next] = ack;
					next = (next + 1) % capacity;
					if (next == first)
					{
						hashSet.remove(Items[first]);
						first = (first + 1) % capacity;
					}
					return true;
				}
			}
			return false;
		}
	}

	private class BoundedLongArray
	{
		private long[] array;
		private int index;
		private int size;

		public BoundedLongArray(int capacity)
		{
			array = new long[capacity];
			index = size = 0;
		}

		public int size()
		{
			return size;
		}

		public boolean isFull()
		{
			return size >= array.length;
		}

		/*
		 * public long poll() { if (size > 0) { return array[(index - size--) %
		 * array.length]; } return 0; }
		 */
		/**
		 * Offer a new value to put in the queue
		 * 
		 * @param value
		 *            The value to put in the queue
		 * @return The previous value that was in the queue at that position
		 */
		public long offer(long value)
		{
			long old = array[index];
			array[index] = value;
			++index;
			index %= array.length;
			if (!isFull())
				size++;
			return old;
		}
	}

	/* The reference to the client that this Simulator object is attached to */
	private GridClient _Client;

	public GridClient getClient()
	{
		return _Client;
	}

	/* A Unique Cache identifier for this simulator */
	public UUID ID = UUID.Zero;

	/* The capabilities for this simulator */
	private CapsManager _Caps = null;

	private long _Handle;

	public long getHandle()
	{
		return _Handle;
	}

	public void setHandle(long handle)
	{
		_Handle = handle;
	}

	/*
	 * The current sequence number for packets sent to this simulator. Must be
	 * interlocked before modifying. Only useful for applications manipulating
	 * sequence numbers
	 */
	private AtomicInteger _Sequence;

	/* Sequence numbers of packets we've received (for duplicate checking) */
	private IncomingPacketIDCollection _PacketArchive;
	/* ACKs that are queued up to be sent to the simulator */
	private TreeSet<Integer> _PendingAcks;
	/* Packets we sent out that need ACKs from the simulator */
	
	private TreeMap<Integer, NetworkManager.OutgoingPacket> _NeedAck; // int -> Packet
	/* Sequence number for pause/resume */
	private AtomicInteger _PauseSerial;

	public final TerrainPatch[] Terrain;

	public final Vector2[] WindSpeeds;

	// Provides access to an internal thread-safe dictionary containing parcel
	// information found in this simulator
	public HashMap<Integer, Parcel> Parcels = new HashMap<Integer, Parcel>();

	// simulator <> parcel LocalID Map
	private int[] _ParcelMap = new int[4096];
	private boolean _DownloadingParcelMap = false;

	// Provides access to an internal thread-safe multidimensional array
	// containing a x,y grid mapped to each 64x64 parcel's LocalID.
	public synchronized final int[] getParcelMap()
	{
		return _ParcelMap;
	}

	public synchronized final int getParcelMap(int x, int y)
	{
		if (x < 0 || x >= 64 || y < 0 || y >= 64)
			throw new IllegalArgumentException("Simulator.getParcelMap() parameters need to be in the range 0 - 63. x = " + x + "; y = " + y);
	    return _ParcelMap[y * 64 + x];
	}

	public synchronized final void setParcelMap(int x, int y, int value)
	{
		_ParcelMap[y * 64 + x] = value;
	}

	public synchronized final void clearParcelMap()
	{
		for (int y = 0; y < 64; y++)
		{
			for (int x = 0; x < 64; x++)
			{
				_ParcelMap[x * 64 + y] = 0;
			}
		}
	}

	// Provides access to an internal thread-safe multidimensional array
	// containing a x,y grid mapped to each 64x64 parcel's LocalID.
	public synchronized final boolean getDownloadingParcelMap()
	{
		return _DownloadingParcelMap;
	}

	public synchronized final void setDownloadingParcelMap(boolean value)
	{
		_DownloadingParcelMap = value;
	}

	/**
	 * Checks simulator parcel map to make sure it has downloaded all data
	 * successfully
	 * 
	 * @return true if map is full (contains no 0's)
	 */
	public final boolean IsParcelMapFull()
	{
		for (int y = 0; y < 64; y++)
		{
			for (int x = 0; x < 64; x++)
			{
				if (getParcelMap(y, x) == 0)
				{
					return false;
				}
			}
		}
		return true;
	}

    // Is it safe to send agent updates to this sim
	// AgentMovementComplete message received
	public boolean AgentMovementComplete;

	/*
	 * Statistics information for this simulator and the connection to the
	 * simulator, calculated by the simulator itself and the library
	 */
	public SimStats Statistics;

	/* The current version of software this simulator is running */
	public String SimVersion = "";

	/*
	 * A 64x64 grid of parcel coloring values. The values stored in this array
	 * are of the {@link ParcelArrayType} type
	 */
	public byte[] ParcelOverlay = new byte[4096];
	/*  */
	public int ParcelOverlaysReceived;
	/*  */
	public float TerrainHeightRange00;
	/*  */
	public float TerrainHeightRange01;
	/*  */
	public float TerrainHeightRange10;
	/*  */
	public float TerrainHeightRange11;
	/*  */
	public float TerrainStartHeight00;
	/*  */
	public float TerrainStartHeight01;
	/*  */
	public float TerrainStartHeight10;
	/*  */
	public float TerrainStartHeight11;
	/*  */
	public float WaterHeight;
	/*  */
	public UUID SimOwner = UUID.Zero;
	/*  */
	public UUID TerrainBase0 = UUID.Zero;
	/*  */
	public UUID TerrainBase1 = UUID.Zero;
	/*  */
	public UUID TerrainBase2 = UUID.Zero;
	/*  */
	public UUID TerrainBase3 = UUID.Zero;
	/*  */
	public UUID TerrainDetail0 = UUID.Zero;
	/*  */
	public UUID TerrainDetail1 = UUID.Zero;
	/*  */
	public UUID TerrainDetail2 = UUID.Zero;
	/*  */
	public UUID TerrainDetail3 = UUID.Zero;
	/* true if your agent has Estate Manager rights on this region */
	public boolean IsEstateManager;
	/*  */
	public long Flags; /* Simulator.RegionFlags */
	/*  */
	public SimAccess Access;
	/*  */
	public float BillableFactor;
	/* The regions Unique ID */
	public UUID RegionID = UUID.Zero;

	/*
	 * The physical data center the simulator is located Known values are:
	 * Dallas, SF
	 */
	public String ColoLocation;
	/*
	 * The CPU Class of the simulator Most full mainland/estate sims appear to
	 * be 5, Homesteads and Openspace appear to be 501
	 */
	public int CPUClass;
	/*
	 * The number of regions sharing the same CPU as this one "Full Sims" appear
	 * to be 1, Homesteads appear to be 4
	 */
	public int CPURatio;
	/*
	 * The billing product name Known values are: Mainland / Full Region (Sku:
	 * 023) Estate / Full Region (Sku: 024) Estate / Openspace (Sku: 027) Estate
	 * / Homestead (Sku: 029) Mainland / Homestead (Sku: 129) (Linden Owned)
	 * Mainland / Linden Homes (Sku: 131)
	 */
	public String ProductName;
	/*
	 * The billing product SKU Known values are: 023 Mainland / Full Region 024
	 * Estate / Full Region 027 Estate / Openspace 029 Estate / Homestead 129
	 * Mainland / Homestead (Linden Owned) 131 Linden Homes / Full Region
	 */
	public String ProductSku;

	/* Flags indicating which protocols this region supports */
   	public long Protocols;

	private DatagramSocket _Connection;
	// The IP address and port of the server.
	private InetSocketAddress ipEndPoint;
	
	private String simName;
	
	public void setSimName(String name)
	{
		simName = name;
		setName(name + " (" + ipEndPoint.getHostName() + ")");
	}

	public String getSimName()
	{
		return simName;
	}

	/* A non thread-safe dictionary containing avatars in a simulator */
	private HashMap<Integer, Avatar> _ObjectsAvatars = new HashMap<Integer, Avatar>();

	public HashMap<Integer, Avatar> getObjectsAvatars()
	{
		return _ObjectsAvatars;
	}

	public Avatar findAvatar(UUID id)
	{
		return findAvatar(id, false);
	}
	
	public Avatar findAvatar(UUID id, boolean remove)
	{
		if (!UUID.isZeroOrNull(id))
		{
			synchronized (_ObjectsAvatars)
			{
				Iterator<Entry<Integer, Avatar>> iter = _ObjectsAvatars.entrySet().iterator();
				while (iter.hasNext())
				{
					Entry<Integer, Avatar> e = iter.next();
					if (id.equals(e.getValue().ID))
					{
						if (remove)
							iter.remove();
						return e.getValue();
					}
				}
			}
		}
		return null;
	}
	
	public Avatar findAvatar(String name)
	{
		if (name != null && !name.isEmpty())
		{
			synchronized (_ObjectsAvatars)
			{
				for (Entry<Integer, Avatar> e : _ObjectsAvatars.entrySet())
				{
					if (name.equals(e.getValue().getName()))
					{
						return e.getValue();
					}
				}
			}
		}
		return null;
	}

	/* A non thread-safe dictionary containing primitives in a simulator */
	private HashMap<Integer, Primitive> _ObjectsPrimitives = new HashMap<Integer, Primitive>();

	public HashMap<Integer, Primitive> getObjectsPrimitives()
	{
		return _ObjectsPrimitives;
	}

	public Primitive findPrimitive(UUID id, boolean remove)
	{
		if (!UUID.isZeroOrNull(id))
		{
			synchronized (_ObjectsPrimitives)
			{
				Iterator<Entry<Integer, Primitive>> iter = _ObjectsPrimitives.entrySet().iterator();
				while (iter.hasNext())
				{
					Entry<Integer, Primitive> e = iter.next();
					if (id.equals(e.getValue().ID))
					{
						if (remove)
							iter.remove();
						return e.getValue();
					}
				}
			}
		}
		return null;
	}

	/* Coarse locations of avatars in this simulator */
	private HashMap<UUID, Vector3> _AvatarPositions = new HashMap<UUID, Vector3>();

	public HashMap<UUID, Vector3> getAvatarPositions()
	{
		return _AvatarPositions;
	}

	/* AvatarPositions key representing TrackAgent target */
	private UUID preyID = UUID.Zero;

	public final UUID getPreyID()
	{
		return preyID;
	}

	public final void setPreyID(UUID id)
	{
		preyID = id;
	}

	public InetSocketAddress getIPEndPoint()
	{
		return ipEndPoint;
	}

	// A boolean representing whether there is a working connection to the
	// simulator or not.
	private boolean _Connected;

	public boolean getConnected()
	{
		return _Connected;
	}

	/* Used internally to track sim disconnections, do not modify this variable. */
	private boolean _DisconnectCandidate = false;

	public boolean getDisconnectCandidate()
	{
		return _DisconnectCandidate;
	}

	public void setDisconnectCandidate(boolean val)
	{
		_DisconnectCandidate = val;
	}

	private boolean trackUtilization;
	private boolean throttleOutgoingPackets;
	
	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs>
	{
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params)
		{
			String key = params.getName();
			if (key == null)
			{
				trackUtilization = _Client.Settings.getBool(LibSettings.TRACK_UTILIZATION);
				throttleOutgoingPackets = _Client.Settings.getBool(LibSettings.THROTTLE_OUTGOING_PACKETS);
			}
			else if (key.equals(LibSettings.TRACK_UTILIZATION))
			{
				trackUtilization = params.getValue().AsBoolean();
			}
			else if (key.equals(LibSettings.THROTTLE_OUTGOING_PACKETS))
			{
				throttleOutgoingPackets = params.getValue().AsBoolean();
			}
			return false;
		}
	}

	private BoundedLongArray _InBytes;
	private BoundedLongArray _OutBytes;
	private Timer _StatsTimer;
	private Timer _AckTimer;
	private Timer _PingTimer;

	@Override
	public int hashCode()
	{
		return ((Long) getHandle()).hashCode();
	}

	public Simulator(GridClient client, InetAddress ip, short port, long handle) throws Exception
	{
		// Create an endpoint that we will be communicating with
		this(client, new InetSocketAddress(ip, port), handle);
	}

	public Simulator(GridClient client, InetSocketAddress endPoint, long handle) throws Exception
	{
		super("Simulator: " + endPoint.getHostName());
		_Client = client;

		_Client.Settings.OnSettingsUpdate.add(new SettingsUpdate());
		trackUtilization = _Client.Settings.getBool(LibSettings.TRACK_UTILIZATION);
		throttleOutgoingPackets = _Client.Settings.getBool(LibSettings.THROTTLE_OUTGOING_PACKETS);

		ipEndPoint = endPoint;
		_Connection = new DatagramSocket();
		_Connected = false;
		_DisconnectCandidate = false;

		_Handle = handle;

		_Sequence = new AtomicInteger();
		_PauseSerial = new AtomicInteger();

		Statistics = new SimStats();
		_InBytes = new BoundedLongArray(_Client.Settings.STATS_QUEUE_SIZE);
		_OutBytes = new BoundedLongArray(_Client.Settings.STATS_QUEUE_SIZE);

		// Initialize the dictionary for reliable packets waiting on ACKs from the server
		_NeedAck = new TreeMap<Integer, NetworkManager.OutgoingPacket>();

		// Initialize the lists of sequence numbers we've received so far
		_PacketArchive = new IncomingPacketIDCollection(_Client.Settings.getInt(LibSettings.PACKET_ARCHIVE_SIZE));
		_PendingAcks = new TreeSet<Integer>();

		if (client.Settings.getBool(LibSettings.STORE_LAND_PATCHES))
		{
			Terrain = new TerrainPatch[16 * 16];
			WindSpeeds = new Vector2[16 * 16];
		}
		else
		{
			Terrain = null;
			WindSpeeds = null;
		}
	}

	/**
	 * Attempt to connect to this simulator
	 * 
	 * @param moveToSim
	 *            Whether to move our agent in to this sim or not
	 * @return True if the connection succeeded or connection status is unknown,
	 *         false if there was a failure
	 * @throws Exception
	 */
	public final boolean connect(boolean moveToSim) throws Exception
	{
		if (_Connected)
		{
			useCircuitCode();
			if (moveToSim)
			{
				_Client.Self.CompleteAgentMovement(this);
			}
			return true;
		}

		if (_AckTimer == null)
		{
			_AckTimer = new Timer("Simulator Acknowledge");
		}
		_AckTimer.schedule(new AckTimer_Elapsed(), LibSettings.NETWORK_TICK_INTERVAL);

		// Timer for recording simulator connection statistics
  		if (LibSettings.OUTPUT_TIMING_STATS && _StatsTimer == null)
		{
			_StatsTimer = new Timer("Simulator Statistics");
			_StatsTimer.scheduleAtFixedRate(new StatsTimer_Elapsed(), 1000, 1000);
		}

		// Timer for periodically pinging the simulator
		if (_PingTimer == null && _Client.Settings.SEND_PINGS)
		{
			_PingTimer = new Timer("Simulator Pings");
			_PingTimer.scheduleAtFixedRate(new PingTimer_Elapsed(), LibSettings.PING_INTERVAL, LibSettings.PING_INTERVAL);
		}

		Logger.Log("Connecting to " + ipEndPoint.toString(), LogLevel.Info, _Client);

		// runs background thread to read from DatagramSocket
		start();

		Statistics.ConnectTime = System.currentTimeMillis();

		Logger.Log("Waiting for connection", LogLevel.Debug, _Client);
		while (true)
		{
			if (_Connected)
			{
				Logger.Log(String.format("Connected! Waited %d ms", System.currentTimeMillis() - Statistics.ConnectTime),
						   LogLevel.Debug, _Client);
				break;
			}
			else if (System.currentTimeMillis() - Statistics.ConnectTime > _Client.Settings.LOGIN_TIMEOUT)
			{
				Logger.Log("Giving up on waiting for RegionHandshake for " + this.toString(), LogLevel.Error, _Client);

				// Remove the simulator from the list, not useful if we haven't received the RegionHandshake
				synchronized (_Client.Network.getSimulators())
				{
					_Client.Network.getSimulators().remove(this);
				}
				return false;
			}
			Thread.sleep(10);
		}

		try
		{
			// Initiate connection
			useCircuitCode();

			// Move our agent in to the sim to complete the connection
			if (moveToSim)
			{
				_Client.Self.CompleteAgentMovement(this);
			}

			if (_Client.Settings.getBool(LibSettings.SEND_AGENT_THROTTLE))
			{
				_Client.Throttle.Set(this);
			}

			if (_Client.Settings.getBool(LibSettings.SEND_AGENT_UPDATES))
			{
				_Client.Self.SendMovementUpdate(true, this);
			}
			return true;
		}
		catch (Exception ex)
		{
			Logger.Log("Failed to update our status", LogLevel.Error, _Client, ex);
		}
		return false;
	}

	/* Initiates connection to the simulator */
	public final void useCircuitCode() throws Exception
	{
		// Send the UseCircuitCode packet to initiate the connection
		UseCircuitCodePacket use = new UseCircuitCodePacket();
		use.CircuitCode.Code = _Client.Network.getCircuitCode();
		use.CircuitCode.ID = _Client.Self.getAgentID();
		use.CircuitCode.SessionID = _Client.Self.getSessionID();

		// Send the initial packet out
		sendPacket(use);
	}

	public final void setSeedCaps(String seedcaps) throws InterruptedException, IOException
	{
		if (_Caps != null)
		{
			if (_Caps.getSeedCapsURI().equals(seedcaps))
				return;

			Logger.Log("Unexpected change of seed capability", LogLevel.Warning, _Client);
			_Caps.disconnect(true);
			_Caps = null;
		}

		if (_Client.Settings.getBool(LibSettings.ENABLE_CAPS))
		{
			// Connect to the new CAPS system
			if (seedcaps == null || seedcaps.isEmpty())
			{
				Logger.Log("Setting up a sim without a valid capabilities server!", LogLevel.Error, _Client);
			}
			else
			{
				_Caps = new CapsManager(this, seedcaps);
			}
		}
	}

	public void disconnect(boolean sendCloseCircuit) throws Exception
	{
		if (_Connected)
		{
			_Connected = false;

			// Destroy the timers
			if (_AckTimer != null)
			{
				_AckTimer.cancel();
				_AckTimer = null;
			}

			if (_StatsTimer != null)
			{
				_StatsTimer.cancel();
				_StatsTimer = null;
			}

			if (_PingTimer != null)
			{
				_PingTimer.cancel();
				_PingTimer = null;
			}

			// Kill the current CAPS system
			if (_Caps != null)
			{
				_Caps.disconnect(true);
				_Caps = null;
			}

			if (sendCloseCircuit)
			{
				// Send the CloseCircuit notice
				CloseCircuitPacket close = new CloseCircuitPacket();

				try
				{
					ByteBuffer data = close.ToBytes();
					_Connection.send(new DatagramPacket(data.array(), data.position()));
					Thread.sleep(50);
				}
				catch (IOException ex)
				{
					// There's a high probability of this failing if the network
					// is disconnected, so don't even bother logging the error
				}
			}

			try
			{
				// Shut the socket communication down
				_Connection.close();
			}
			catch (Exception ex)
			{
				Logger.Log(ex.toString(), LogLevel.Error, _Client, ex);
			}
		}
	}

	/*
	 * Instructs the simulator to stop sending update (and possibly other)
	 * packets
	 */
	public final void pauseUpdates() throws Exception
	{
		AgentPausePacket pause = new AgentPausePacket();
		pause.AgentData.AgentID = _Client.Self.getAgentID();
		pause.AgentData.SessionID = _Client.Self.getSessionID();
		pause.AgentData.SerialNum = _PauseSerial.getAndIncrement();

		sendPacket(pause);
	}

	/* Instructs the simulator to resume sending update packets (unpause) */
	public final void resumeUpdates() throws Exception
	{
		AgentResumePacket resume = new AgentResumePacket();
		resume.AgentData.AgentID = _Client.Self.getAgentID();
		resume.AgentData.SessionID = _Client.Self.getSessionID();
		resume.AgentData.SerialNum = _PauseSerial.get();

		sendPacket(resume);
	}

	/**
	 * Retrieve the terrain height at a given coordinate
	 * 
	 * @param x
	 *            Sim X coordinate, valid range is from 0 to 255
	 * @param y
	 *            Sim Y coordinate, valid range is from 0 to 255
	 * @param height
	 *            The terrain height at the given point if the lookup was
	 *            successful, otherwise 0.0f
	 * @return True if the lookup was successful, otherwise false
	 */
	public final float TerrainHeightAtPoint(int x, int y)
	{
		if (Terrain != null && x >= 0 && x < 256 && y >= 0 && y < 256)
		{
			int patchX = x / 16;
			int patchY = y / 16;
			x = x % 16;
			y = y % 16;

			TerrainPatch patch = Terrain[patchY * 16 + patchX];
			if (patch != null)
			{
				return patch.Data[y * 16 + x];
			}
		}
		return Float.NaN;
	}

	private final void sendPing() throws Exception
	{
		int oldestUnacked = 0;

		// Get the oldest NeedAck value, the first entry in the sorted dictionary
		synchronized (_NeedAck)
		{
			if (!_NeedAck.isEmpty())
			{
				oldestUnacked = _NeedAck.firstKey();
			}
		}

		// if (oldestUnacked != 0)
		//     Logger.DebugLog("Sending ping with oldestUnacked=" + oldestUnacked);

		StartPingCheckPacket ping = new StartPingCheckPacket();
		ping.PingID.PingID = Statistics.LastPingID++;
		ping.PingID.OldestUnacked = oldestUnacked;
		ping.getHeader().setReliable(false);
		sendPacket(ping);
		Statistics.LastPingSent = System.currentTimeMillis();
	}

	public URI getCapabilityURI(String capability)
	{
		if (_Caps != null)
			return _Caps.capabilityURI(capability);
		return null;
	}

	public boolean getIsEventQueueRunning()
	{
		if (_Caps != null)
			return _Caps.isRunning();
		return false;
	}

	private void DumpBuffer(byte[] byteBuffer, int numBytes, String head, int level)
	{
		Logger.Log(head + numBytes, level, _Client);
		StringBuffer dump = new StringBuffer(numBytes * 2);
		for (int i = 0; i < numBytes; i++)
		{
			byte value = byteBuffer[i];
			dump.append(Integer.toHexString(value & 0xFF));
			dump.append(" ");
		}
		Logger.Log(dump, level, _Client);

	}

	@Override
	public void run()
	{
		try
		{
			_Connection.connect(ipEndPoint);
		}
		catch (SocketException e)
		{
			Logger.Log("Failed to startup the UDP socket", Logger.LogLevel.Error, _Client);
			return;
		}
		byte[] RecvBuffer = new byte[4096];
		DatagramPacket p = new DatagramPacket(RecvBuffer, RecvBuffer.length);
		boolean logRawPackets = _Client.Settings.getBool(LibSettings.LOG_RAW_PACKET_BYTES);
		_Connected = true;

		while (true)
		{
			try
			{
				_Connection.receive(p);
				int numBytes = p.getLength();
				Packet packet = null;

				// Update the disconnect flag so this sim doesn't time out
				_DisconnectCandidate = false;

				synchronized (RecvBuffer)
				{
					byte[] byteBuffer = RecvBuffer;

					// Retrieve the incoming packet
					try
					{
						if (logRawPackets)
						{
							DumpBuffer(byteBuffer, numBytes, "<=============== Received packet, length = ", LogLevel.Debug);
						}

						if ((RecvBuffer[0] & PacketHeader.MSG_ZEROCODED) != 0)
						{
							int bodylen = numBytes;
							if ((RecvBuffer[0] & PacketHeader.MSG_APPENDED_ACKS) != 0)
							{
								bodylen -= (RecvBuffer[numBytes - 1] * 4 + 1);
							}
							byteBuffer = new byte[numBytes <= 1000 ? 4000 : numBytes * 4];
							numBytes = ZeroDecode(RecvBuffer, numBytes, bodylen, byteBuffer);
							if (logRawPackets)
							{
								DumpBuffer(byteBuffer, numBytes, "<==========Zero-Decoded packet, length=", LogLevel.Debug);
							}
						}

						packet = Packet.BuildPacket(ByteBuffer.wrap(byteBuffer, 0, numBytes));
						if (logRawPackets)
						{
							Logger.Log("Decoded packet " + packet.getClass().getName(), Logger.LogLevel.Debug, _Client);
						}
					}
					catch (IOException ex)
					{
						Logger.Log(ipEndPoint.toString() + " socket is closed, shutting down " + getName(), LogLevel.Info, _Client, ex);

						_Connected = false;
						_Client.Network.disconnectSim(this, true);
						return;
					}
					catch (BufferUnderflowException ex)
					{
						DumpBuffer(byteBuffer, numBytes, "<=========== Buffer Underflow in packet, length = ", LogLevel.Debug);
					}
				}

				if (packet == null)
				{
					DumpBuffer(RecvBuffer, numBytes, "<=========== Couldn't build a message from the incoming data, length = ", LogLevel.Warning);
					continue;
				}

				Statistics.RecvBytes += numBytes;
				Statistics.RecvPackets++;

				if (packet.getHeader().getResent())
				{
					Statistics.ReceivedResends++;
				}

				// Handle appended ACKs
				if (packet.getHeader().getAppendedAcks() && packet.getHeader().AckList != null)
				{
					synchronized (_NeedAck)
					{
						for (int ack : packet.getHeader().AckList)
						{
							if (_NeedAck.remove(ack) == null)
							{
								Logger.Log(String.format("Appended ACK for a packet (%d) we didn't send: %s", ack,
										packet.getClass().getName()), LogLevel.Warning, _Client);
							}
						}
					}
				}
				// Handle PacketAck packets
				if (packet.getType() == PacketType.PacketAck)
				{
					PacketAckPacket ackPacket = (PacketAckPacket) packet;

					synchronized (_NeedAck)
					{
						for (int ID : ackPacket.ID)
						{
							if (_NeedAck.remove(ID) == null)
							{
								Logger.Log(String.format("ACK for a packet (%d) we didn't send: %s", ID,
										packet.getClass().getName()), LogLevel.Warning, _Client);
							}
						}
					}
				}

				// Add this packet to the list of ACKs that need to be sent out
				int sequence = packet.getHeader().getSequence();
				synchronized (_PendingAcks)
				{
					_PendingAcks.add(sequence);
				}

				// Send out ACKs if we have a lot of them
				if (_PendingAcks.size() >= _Client.Settings.MAX_PENDING_ACKS)
				{
					sendPendingAcks();
				}

				/*
				 * Track the sequence number for this packet if it's marked as reliable
				 */
				if (packet.getHeader().getReliable() && !_PacketArchive.tryEnqueue(sequence))
				{
					if (packet.getHeader().getResent())
					{
						Logger.DebugLog(String.format("Received a resend of already processed packet #%d, type: %s, from %s",
								                       sequence, packet.getType(), getName()), _Client);
					}
					else
					{
						Logger.Log(String.format("Received a duplicate (not marked as resend) of packet #%d, type: %s for %s from %s", 
								                 sequence, packet.getType(), _Client.Self.getName(), getName()), LogLevel.Warning, _Client);
					}
					// Avoid firing a callback twice for the same packet
					continue;
				}

				// Let the network manager distribute the packet to the callbacks
				_Client.Network.DistributePacket(this, packet);

				if (trackUtilization)
				{
					_Client.Stats.updateNetStats(packet.getType().toString(), Type.Packet, 0, numBytes);
				}
			}
			catch (IOException ex)
			{
				Logger.Log(ipEndPoint.toString() + " socket is closed, shutting down " + getName(), LogLevel.Info, _Client);

				_Connected = false;
				return;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void sendPacket(Packet packet) throws Exception
	{
		if (packet.hasVariableBlocks && packet.getLength() > Packet.MTU)
		{
			ByteBuffer[] datas;
			try
			{
				datas = packet.ToBytesMultiple();
			}
			catch (NullPointerException ex)
			{
				Logger.Log(
						"Failed to serialize " + packet.getType()
								+ " packet to one or more payloads due to a missing block or field. StackTrace: "
								+ ex.getStackTrace(), LogLevel.Error);
				return;
			}
			int packetCount = datas.length;

			if (packetCount > 1)
			{
				Logger.DebugLog("Split " + packet.getType() + " packet into " + packetCount + " packets");
			}

			for (int i = 0; i < packetCount; i++)
			{
				sendPacketData(datas[i], packet.getType(), packet.getHeader().getZerocoded());
			}
		}
		else
		{
			ByteBuffer data = packet.ToBytes();
			sendPacketData(data, packet.getType(), packet.getHeader().getZerocoded());
		}
	}

	private final void sendPacketData(ByteBuffer data, PacketType type, boolean doZerocode) throws InterruptedException
	{
		// Zerocode if needed
		if (doZerocode)
		{
			byte[] zeroBuffer = new byte[2000];
			int bytes = PacketHeader.zeroEncode(data, zeroBuffer);
			if (bytes <= data.capacity())
			{
				data = ByteBuffer.wrap(zeroBuffer, 0, bytes);
				data.order(ByteOrder.LITTLE_ENDIAN);
			}
			else
			{
				// Zero encoding actually grew the buffer beyond the original size
				data.put(0, (byte) (data.get(0) & PacketHeader.MSG_ZEROCODED));
				data.position(0);
			}
		}

		// #region Queue or Send
		NetworkManager.OutgoingPacket outgoingPacket = _Client.Network.new OutgoingPacket(this, type, data);

		// Send ACK and logout packets directly, everything else goes through
		// the queue
		if (!throttleOutgoingPackets || type == PacketType.PacketAck || type == PacketType.LogoutRequest)
		{
			sendPacketFinal(outgoingPacket);
		}
		else
		{
			_Client.Network.QueuePacket(outgoingPacket);
		}
		// #endregion Queue or Send

		// #region Stats Tracking
		if (trackUtilization)
		{
			_Client.Stats.updateNetStats(type.toString(), Type.Packet, data.capacity(), 0);
		}
	}

	/* Sends out pending acknowledgements */
	private void sendPendingAcks()
	{
		synchronized (_PendingAcks)
		{
			if (_PendingAcks.size() > 0)
			{
				PacketAckPacket acks = new PacketAckPacket();
				acks.ID = new int[_PendingAcks.size()];
				acks.getHeader().setReliable(false);
				int i = 0;
				Iterator<Integer> iter = _PendingAcks.iterator();
				while (iter.hasNext())
				{
					acks.ID[i++] = iter.next();
				}
				_PendingAcks.clear();

				try
				{
					sendPacket(acks);
				}
				catch (Exception ex)
				{
					Logger.Log("Exception when sending Ack packet", Logger.LogLevel.Error, _Client, ex);
				}
			}
		}
	}

	/**
	 * Resend unacknowledged packets
	 */
	private void resendUnacked()
	{
		if (_NeedAck.size() > 0)
		{
			ArrayList<NetworkManager.OutgoingPacket> array;

			synchronized (_NeedAck)
			{
				// Create a temporary copy of the outgoing packets array to iterate over
				array = new ArrayList<NetworkManager.OutgoingPacket>(_NeedAck.size());
				array.addAll(_NeedAck.values());
			}

			long now = System.currentTimeMillis();

			// Resend packets
			for (int i = 0; i < array.size(); i++)
			{
				NetworkManager.OutgoingPacket outgoing = array.get(i);

				if (outgoing.TickCount != 0 && now - outgoing.TickCount > _Client.Settings.RESEND_TIMEOUT)
				{
					if (outgoing.ResendCount < _Client.Settings.MAX_RESEND_COUNT)
					{
						if (_Client.Settings.LOG_RESENDS)
						{
							Logger.DebugLog(String.format("Resending %s packet #%d, %d ms have passed",
									outgoing.Type, outgoing.SequenceNumber, now - outgoing.TickCount), _Client);
						}

						// The TickCount will be set to the current time when
						// the packet is actually sent out again
						outgoing.TickCount = 0;

						// Set the resent flag
						outgoing.Buffer.array()[0] |= PacketHeader.MSG_RESENT;

						// Stats tracking
						outgoing.ResendCount++;
						Statistics.ResentPackets++;

						sendPacketFinal(outgoing);
					}
					else
					{
						Logger.DebugLog(String.format("Dropping packet #%d after %d failed attempts",
								outgoing.SequenceNumber, outgoing.ResendCount, _Client));

						synchronized (_NeedAck)
						{
							_NeedAck.remove(outgoing.SequenceNumber);
						}
					}
				}
			}
		}
	}

	public final void sendPacketFinal(NetworkManager.OutgoingPacket outgoingPacket)
	{
		ByteBuffer buffer = outgoingPacket.Buffer;
		byte[] bytes = buffer.array();
		byte flags = buffer.get(0);
		boolean isResend = (flags & PacketHeader.MSG_RESENT) != 0;
		boolean isReliable = (flags & PacketHeader.MSG_RELIABLE) != 0;

		// Keep track of when this packet was sent out (right now)
		outgoingPacket.TickCount = System.currentTimeMillis();

		// #region ACK Appending
		int dataLength = buffer.limit();

		// Keep appending ACKs until there is no room left in the packet or
		// there are no more ACKs to append
		int ackCount = 0;
		synchronized (_PendingAcks)
		{
			while (dataLength + 5 < buffer.capacity() && !_PendingAcks.isEmpty())
			{
				dataLength += Helpers.UInt32ToBytesB(_PendingAcks.pollFirst(), bytes, dataLength);
				++ackCount;
			}
		}

		if (ackCount > 0)
		{
			// Set the last byte of the packet equal to the number of appended
			// ACKs
			bytes[dataLength++] = (byte) ackCount;
			// Set the appended ACKs flag on this packet
			bytes[0] = (byte) (flags | PacketHeader.MSG_APPENDED_ACKS);
			// Increase the byte buffer limit to the new length
			buffer.limit(dataLength);
		}
		// #endregion ACK Appending

		if (!isResend)
		{
			// Not a resend, assign a new sequence number
			outgoingPacket.SequenceNumber = _Sequence.incrementAndGet();
			Helpers.UInt32ToBytesB(outgoingPacket.SequenceNumber, bytes, 1);

			if (isReliable)
			{
				// Add this packet to the list of ACK responses we are waiting
				// on from the server
				synchronized (_NeedAck)
				{
					_NeedAck.put(outgoingPacket.SequenceNumber, outgoingPacket);
				}
			}
		}

		try
		{
			_Connection.send(new DatagramPacket(buffer.array(), dataLength));
		}
		catch (IOException ex)
		{
			Logger.Log(ex.toString(), LogLevel.Error, _Client, ex);
		}

		// Stats tracking
		Statistics.SentBytes += dataLength;
		Statistics.SentPackets++;
		_Client.Network.RaisePacketSentCallback(buffer.array(), dataLength, this);
	}

	/* #region timer callbacks */
	private class AckTimer_Elapsed extends TimerTask
	{
		@Override
		public void run()
		{
			if (!_Connected)
			{
				return;
			}

			sendPendingAcks();
			resendUnacked();

			_AckTimer.schedule(new AckTimer_Elapsed(), LibSettings.NETWORK_TICK_INTERVAL);
		}
	}

	private class StatsTimer_Elapsed extends TimerTask
	{
		@Override
		public void run()
		{
			boolean full = _InBytes.isFull();
			long recv = Statistics.RecvBytes;
			long sent = Statistics.SentBytes;
			long old_in = _InBytes.offer(recv);
			long old_out = _OutBytes.offer(sent);

			if (full)
			{
				Statistics.IncomingBPS = (int) (recv - old_in) / _InBytes.size();
				Statistics.OutgoingBPS = (int) (sent - old_out) / _OutBytes.size();
				Logger.Log(getName() + ", Incoming: " + Statistics.IncomingBPS + " bps, Out: " + Statistics.OutgoingBPS
						+ " bps, Lag: " + Statistics.LastLag + " ms, Pings: " + Statistics.ReceivedPongs + "/"
						+ Statistics.SentPings, LogLevel.Debug, _Client);
			}
		}
	}

	private class PingTimer_Elapsed extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				sendPing();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			Statistics.SentPings++;
		}
	}

	/**
	 * Decode a zerocoded byte array, used to decompress packets marked with the
	 * zerocoded flag Any time a zero is encountered, the next byte is a count
	 * of how many zeroes to expand. One zero is encoded with 0x00 0x01, two
	 * zeroes is 0x00 0x02, three zeroes is 0x00 0x03, etc. The first six bytes
	 * puls and extra bytes are copied directly to the output buffer.
	 * 
	 * @param src
	 *            The byte array to decode
	 * @param srclen
	 *            The length of the input byte array
	 * @param bodylen
	 *            The length of the byte array to decode
	 * @param dest
	 *            The output byte array to decode to
	 * @return The length of the output buffer
	 * @throws Exception
	 */
	private static int ZeroDecode(byte[] src, int srclen, int bodylen, byte[] dest) throws Exception
	{
		int i, destlen = 6 + src[5];

		/* Copy the first 6 + extra header bytes as they are never compressed */
		System.arraycopy(src, 0, dest, 0, destlen);
		for (i = destlen; i < bodylen; i++)
		{
			if (src[i] == 0x00)
			{
				for (byte j = 0; j < src[i + 1]; j++)
				{
					dest[destlen++] = 0x00;
				}
				i++;
			}
			else
			{
				dest[destlen++] = src[i];
			}
		}

		if (srclen > bodylen)
		{
			/* Copy the appended ack data as they are never compressed */
			System.arraycopy(src, bodylen, dest, destlen, srclen - bodylen);
			destlen += srclen - bodylen;
		}
		return destlen;
	}
}
