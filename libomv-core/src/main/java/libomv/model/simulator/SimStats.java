package libomv.model.simulator;

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