package libomv.model;

import java.util.HashSet;
import java.util.Map;

import libomv.model.Network.OutgoingPacket;
import libomv.packets.Packet;
import libomv.primitives.Avatar;
import libomv.types.UUID;

public interface Simulator extends Runnable {

	/* Access level for a simulator */
	public static enum SimAccess {
		/* Minimum access level, no additional checks */
		Min(0),
		/* Trial accounts allowed */
		Trial(7), // 4 + 2 + 1
		/* PG rating */
		PG(13), // 8 + 4 + 1
		/* Mature rating */
		Mature(21), // 16 + 4 + 1
		/* Adult rating */
		Adult(42), // 32 + 8 + 4
		/* Simulator is offline */
		Down(0xFE),
		/* Simulator does not exist */
		NonExistent(0xFF);

		public static SimAccess setValue(int value) {
			for (SimAccess e : values()) {
				if (e._value == value)
					return e;
			}
			return Min;
		}

		public byte getValue() {
			return _value;
		}

		private byte _value;

		private SimAccess(int value) {
			_value = (byte) value;
		}
	}

	/* Simulator (region) properties */
	// [Flags]
	public static class RegionFlags {
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
		 * Unknown: Related to the availability of an overview world map tile.(Think
		 * mainland images when zoomed out.)
		 */
		public static final long NullLayer = 1 << 9;
		/*
		 * Unknown: Related to region debug flags. Possibly to skip processing of agent
		 * interaction with world.
		 */
		public static final long SkipAgentAction = 1 << 10;
		/*
		 * Region does not update agent prim interest lists. Internal debugging option.
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
		 * Region can be seen from mainland on world map. (Legacy world map option?)
		 */
		public static final long MainlandVisible = 1 << 16;
		/* Agents not explicitly on the access list can visit the region. */
		public static final long PublicAllowed = 1 << 17;
		/*
		 * Traffic calculations are not run across entire region, overrides parcel
		 * settings.
		 */
		public static final long BlockDwell = 1 << 18;
		/* Flight is disabled (not currently enforced by the sim) */
		public static final long NoFly = 1 << 19;
		/* Allow direct (p2p) teleporting */
		public static final long AllowDirectTeleport = 1 << 20;
		/* Estate owner has temporarily disabled scripting */
		public static final long EstateSkipScripts = 1 << 21;
		/*
		 * Restricts the usage of the LSL llPushObject function, applies to whole
		 * region.
		 */
		public static final long RestrictPushObject = 1 << 22;
		/* Deny agents with no payment info on file */
		public static final long DenyAnonymous = 1 << 23;
		/* Deny agents with payment info on file */
		public static final long DenyIdentified = 1 << 24;
		/* Deny agents who have made a monetary transaction */
		public static final long DenyTransacted = 1 << 25;
		/*
		 * Parcels within the region may be joined or divided by anyone, not just estate
		 * owners/managers.
		 */
		public static final long AllowParcelChanges = 1 << 26;
		/*
		 * Abuse reports sent from within this region are sent to the estate owner
		 * defined email.
		 */
		public static final long AbuseEmailToEstateOwner = 1 << 27;
		/* Region is Voice Enabled */
		public static final long AllowVoice = 1 << 28;
		/*
		 * Removes the ability from parcel owners to set their parcels to show in
		 * search.
		 */
		public static final long BlockParcelSearch = 1 << 29;
		/* Deny agents who have not been age verified from entering the region. */
		public static final long DenyAgeUnverified = 1 << 30;

		public static long setValue(long value) {
			return value & _mask;
		}

		public static long getValue(long value) {
			return value;
		}

		private static final long _mask = 0xFFFFFFFFFL;
	}

	/* Region protocol flags */
	// [Flags]
	public static class RegionProtocols {
		// Nothing special
		public static final long None = 0;
		// Region supports Server side Appearance
		public static final long AgentAppearanceService = 1 << 0;
		// Viewer supports Server side Appearance
		public static final long SelfAppearanceSupport = 1 << 2;

		public static long setValue(long value) {
			return value & _mask;
		}

		public static long getValue(long value) {
			return value;
		}

		private static final long _mask = 0x7FFFFFFFL;
	}

	public static enum SimStatType {
		Unknown, // -1
		TimeDilation, // 0
		SimFPS, PhysicsFPS, AgentUpdates, FrameMS, NetMS, OtherMS, PhysicsMS, AgentMS, ImageMS, ScriptMS, // 10
		TotalPrim, ActivePrim, Agents, ChildAgents, ActiveScripts, ScriptInstructionsPerSecond, InPacketsPerSecond, OutPacketsPerSecond, PendingDownloads, PendingUploads, // 20
		VirtualSizeKB, ResidentSizeKB, PendingLocalUploads, UnAckedBytes,

		PhysicsPinnedTasks, PhysicsLODTasks, PhysicsStepMS, PhysicsShapeMS, PhysicsOtherMS, PhysicsMemory, // 30

		ScriptEPS, SimSpareTime, SimSleepTime, SimIOPumpTime, SimPctScriptsRun, // 35
		SimRegionIdle, // dataserver only
		SimRegionIdlePossible, // dataserver only
		SimAIStepMsec, SimSkippedSilhouetteSteps, SimPctSteppedCharacters; // 40

		public static SimStatType setValue(int value) {
			if (value >= 0 && value < values().length - 1)
				return values()[value + 1];
			return Unknown;
		}

		public static int getValue(SimStatType type) {
			return type.ordinal();
		}
	}

	/* Simulator Statistics */
	public final class SimStats {
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
		 * It would be nice to have this calculated on the fly, but this is far, far
		 * easier
		 */
		public int IncomingBPS;
		/*
		 * Outgoing bytes per second
		 *
		 * It would be nice to have this claculated on the fly, but this is far, far
		 * easier
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

	public final class IncomingPacketIDCollection {
		private final int[] Items;
		private HashSet<Integer> hashSet;
		private int first = 0;
		private int next = 0;
		private int capacity;

		public IncomingPacketIDCollection(int capacity) {
			this.capacity = capacity;
			Items = new int[capacity];
			hashSet = new HashSet<Integer>();
		}

		public boolean tryEnqueue(int ack) {
			synchronized (hashSet) {
				if (hashSet.add(ack)) {
					Items[next] = ack;
					next = (next + 1) % capacity;
					if (next == first) {
						hashSet.remove(Items[first]);
						first = (first + 1) % capacity;
					}
					return true;
				}
			}
			return false;
		}
	}

	public Avatar findAvatar(UUID uuid);

	public long getHandle();

	public Map<Integer, Parcel> getParcels();

	public String getSimName();

	public void sendPacket(Packet packet) throws Exception;

	public void sendPacketFinal(OutgoingPacket outgoingPacket);
}
