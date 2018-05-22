package libomv.model.estate;

// Used in the ReportType field of a LandStatRequest
public enum LandStatReportType {
	TopScripts, TopColliders;

	public static LandStatReportType setValue(int value) {
		return values()[value];
	}
}