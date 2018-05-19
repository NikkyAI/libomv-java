package libomv.model.estate;

// #region Enums
// Used in the ReportType field of a LandStatRequest
public enum LandStatReportType {
	TopScripts, TopColliders;

	public static LandStatReportType setValue(int value) {
		return values()[value];
	}
}