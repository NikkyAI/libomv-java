package libomv.model.simulator;

public enum SimStatType {
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