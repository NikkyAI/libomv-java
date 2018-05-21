package libomv.model.directory;

import java.util.List;

import libomv.utils.CallbackArgs;

/** Contains the land sales data returned from the data server */
public class DirLandReplyCallbackArgs implements CallbackArgs {
	private final List<DirectoryParcel> dirParcels;

	/**
	 * Construct a new instance of the DirLandReplyEventArgs class
	 *
	 * @param dirParcels
	 *            A list of parcels for sale returned by the data server
	 */
	public DirLandReplyCallbackArgs(List<DirectoryParcel> dirParcels) {
		this.dirParcels = dirParcels;
	}

	// A list containing land forsale data returned by the data server
	public final List<DirectoryParcel> getDirParcels() {
		return dirParcels;
	}

}