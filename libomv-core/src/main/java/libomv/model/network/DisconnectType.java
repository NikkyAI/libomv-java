package libomv.model.network;

/** Explains why a simulator or the grid disconnected from us */
public enum DisconnectType {
	/** The client requested the logout or simulator disconnect */
	ClientInitiated,
	/** The server notified us that it is disconnecting */
	ServerInitiated,
	/** Either a socket was closed or network traffic timed out */
	NetworkTimeout,
	/** The last active simulator shut down */
	SimShutdown
}