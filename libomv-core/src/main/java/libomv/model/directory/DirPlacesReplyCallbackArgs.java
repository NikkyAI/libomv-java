package libomv.model.directory;

import java.util.ArrayList;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/** Contains the places data returned from the data server */
public class DirPlacesReplyCallbackArgs implements CallbackArgs {
	private final UUID queryID;
	private final ArrayList<DirectoryParcel> matchedParcels;

	/**
	 * Construct a new instance of the DirPlacesReplyEventArgs class
	 *
	 * @param queryID
	 *            The ID of the query returned by the data server. This will
	 *            correlate to the ID returned by the
	 *            <see cref="StartDirPlacesSearch"/> method
	 * @param matchedParcels
	 *            A list containing land data returned by the data server
	 */
	public DirPlacesReplyCallbackArgs(UUID queryID, ArrayList<DirectoryParcel> matchedParcels) {
		this.queryID = queryID;
		this.matchedParcels = matchedParcels;
	}

	// The ID returned by <see
	// cref="DirectoryManager.StartDirPlacesSearch"/>
	public final UUID getQueryID() {
		return queryID;
	}

	// A list containing Places data returned by the data server
	public final ArrayList<DirectoryParcel> getMatchedParcels() {
		return matchedParcels;
	}

}