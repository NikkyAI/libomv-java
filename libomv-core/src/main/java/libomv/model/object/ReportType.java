package libomv.model.object;

public enum ReportType {
	// No report
	None,
	// Unknown report type
	Unknown,
	// Bug report
	Bug,
	// Complaint report
	Complaint,
	// Customer service report
	CustomerServiceRequest;

	public static ReportType setValue(int value) {
		if (value >= 0 && value < values().length)
			return values()[value];
		return null;
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}