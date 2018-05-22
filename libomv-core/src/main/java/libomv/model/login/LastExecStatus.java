package libomv.model.login;

// Status of the last application run.
// Used for error reporting to the grid login service for statistical purposes.
public enum LastExecStatus {
	// Application exited normally
	Normal,
	// Application froze
	Froze,
	// Application detected error and exited abnormally
	ForcedCrash,
	// Other crash
	OtherCrash,
	// Application froze during logout
	LogoutFroze,
	// Application crashed during logout
	LogoutCrash;
}
