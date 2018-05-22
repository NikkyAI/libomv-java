package libomv.model.agent;

/* */
public class TeleportFlags {
	/* No flags set, or teleport failed */
	public static final int Default = 0;
	/* Set when newbie leaves help island for first time */
	public static final int SetHomeToTarget = 1 << 0;
	/* */
	public static final int SetLastToTarget = 1 << 1;
	/* Via Lure */
	public static final int ViaLure = 1 << 2;
	/* Via Landmark */
	public static final int ViaLandmark = 1 << 3;
	/* Via Location */
	public static final int ViaLocation = 1 << 4;
	/* Via Home */
	public static final int ViaHome = 1 << 5;
	/* Via Telehub */
	public static final int ViaTelehub = 1 << 6;
	/* Via Login */
	public static final int ViaLogin = 1 << 7;
	/* Linden Summoned */
	public static final int ViaGodlikeLure = 1 << 8;
	/* Linden Forced me */
	public static final int Godlike = 1 << 9;
	/* */
	public static final int NineOneOne = 1 << 10;
	/* Agent Teleported Home via Script */
	public static final int DisableCancel = 1 << 11;
	/* */
	public static final int ViaRegionID = 1 << 12;
	/* */
	public static final int IsFlying = 1 << 13;
	/* */
	public static final int ResetHome = 1 << 14;
	/* forced to new location for example when avatar is banned or ejected */
	public static final int ForceRedirect = 1 << 15;
	/* Teleport Finished via a Lure */
	public static final int FinishedViaLure = 1 << 26;
	/* Finished, Sim Changed */
	public static final int FinishedViaNewSim = 1 << 28;
	/* Finished, Same Sim */
	public static final int FinishedViaSameSim = 1 << 29;

	private static final int MASK = 0x3400FFFF;

	private TeleportFlags() {
	}

	public static int setValue(int value) {
		return value & MASK;
	}

	public static int getValue(int value) {
		return value & MASK;
	}

}