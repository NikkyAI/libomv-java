package libomv.model.simulator;

/* Simulator Statistics */
public final class SimStats {
	/* Total number of packets sent by this simulator from this agent */
	public long sentPackets;
	/* Total number of packets received by this simulator to this agent */
	public long recvPackets;
	/* Total number of bytes sent by this simulator to this agent */
	public long sentBytes;
	/* Total number of bytes received by this simulator to this agent */
	public long recvBytes;
	/* Time in seconds agent has been connected to simulator */
	public long connectTime;
	/* Total number of packets that have been resent */
	public long resentPackets;
	/* Total number of resent packets received */
	public long receivedResends;
	/* Total number of pings sent to this simulator by this agent */
	public long sentPings;
	/* Total number of ping replies sent to this agent by this simulator */
	public long receivedPongs;
	/*
	 * Incoming bytes per second
	 *
	 * It would be nice to have this calculated on the fly, but this is far, far
	 * easier
	 */
	public int incomingBPS;
	/*
	 * Outgoing bytes per second
	 *
	 * It would be nice to have this claculated on the fly, but this is far, far
	 * easier
	 */
	public int outgoingBPS;
	/* Time last ping was sent */
	public long lastPingSent;
	/*  */
	public long lastLag;
	/* ID of last Ping sent */
	public byte lastPingID;
	/*  */
	public long missedPings;
	/* Current time dilation of this simulator */
	public float dilation;
	/* Current Frames per second of simulator */
	public int fps;
	/* Current Physics frames per second of simulator */
	public float physicsFPS;
	/*  */
	public float agentUpdates;
	/*  */
	public float frameTime;
	/*  */
	public float netTime;
	/*  */
	public float physicsTime;
	/*  */
	public float imageTime;
	/*  */
	public float scriptTime;
	/*  */
	public float agentTime;
	/*  */
	public float otherTime;
	/* Total number of objects Simulator is simulating */
	public int objects;
	/* Total number of Active (Scripted) objects running */
	public int scriptedObjects;
	/* Number of agents currently in this simulator */
	public int agents;
	/* Number of agents in neighbor simulators */
	public int childAgents;
	/* Number of Active scripts running in this simulator */
	public int activeScripts;
	/*  */
	public int lslIPS;
	/*  */
	public int inPPS;
	/*  */
	public int outPPS;
	/* Number of downloads pending */
	public int pendingDownloads;
	/* Number of uploads pending */
	public int pendingUploads;
	/*  */
	public int virtualSize;
	/*  */
	public int residentSize;
	/* Number of local uploads pending */
	public int pendingLocalUploads;
	/* Unacknowledged bytes in queue */
	public int unackedBytes;

	public int physicsPinnedTasks;
	public int physicsLODTasks;
	public int physicsStepMS;
	public int physicsShapeMS;
	public int physicsOtherMS;
	public int physicsMemory;
	public int scriptEPS;
	public int simSpareTime;
	public int simSleepTime;
	public int simIOPumpTime;
	public int simPctScriptsRun;
	public int simAIStepMsec;
	public int simSkippedSilhouetteSteps;
	public int simPctSteppedCharacters;
}