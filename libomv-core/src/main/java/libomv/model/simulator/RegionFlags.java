package libomv.model.simulator;

/* Simulator (region) properties */
// [Flags]
public class RegionFlags {
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