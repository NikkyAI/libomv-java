package libomv.model;

public interface Texture {

	// The current status of a texture request as it moves through the pipeline
	// or final result of a texture request.
	public enum TextureRequestState {
		// The initial state given to a request. Requests in this state are
		// waiting for an available slot in the pipeline
		Pending,
		// A request that has been added to the pipeline and the request packet
		// has been sent to the simulator
		Started,
		// A request that has received one or more packets back from the
		// simulator
		Progress,
		// A request that has received all packets back from the simulator
		Finished,
		// A request that has taken longer than {@link
		// Settings.PIPELINE_REQUEST_TIMEOUT} to download OR the initial
		// packet containing the packet information was never received
		Timeout,
		// The texture request was aborted by request of the agent
		Aborted,
		// The simulator replied to the request that it was not able to find the
		// requested texture
		NotFound
	}

}
