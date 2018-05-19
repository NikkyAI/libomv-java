package libomv.model.simulator;

/* Region protocol flags */
// [Flags]
public class RegionProtocols {
	// Nothing special
	public static final long None = 0;
	// Region supports Server side Appearance
	public static final long AgentAppearanceService = 1 << 0;
	// Viewer supports Server side Appearance
	public static final long SelfAppearanceSupport = 1 << 2;

	private static final long _mask = 0x7FFFFFFFL;

	private RegionProtocols() {
	}

	public static long setValue(long value) {
		return value & _mask;
	}

	public static long getValue(long value) {
		return value;
	}

}