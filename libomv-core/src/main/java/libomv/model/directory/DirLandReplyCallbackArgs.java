package libomv.model.directory;

import java.util.ArrayList;

import libomv.utils.CallbackArgs;

/** Contains the land sales data returned from the data server */
public class DirLandReplyCallbackArgs implements CallbackArgs {
	private final ArrayList<DirectoryParcel> m_DirParcels;

	// A list containing land forsale data returned by the data server
	public final ArrayList<DirectoryParcel> getDirParcels() {
		return m_DirParcels;
	}

	/**
	 * Construct a new instance of the DirLandReplyEventArgs class
	 *
	 * @param dirParcels
	 *            A list of parcels for sale returned by the data server
	 */
	public DirLandReplyCallbackArgs(ArrayList<DirectoryParcel> dirParcels) {
		this.m_DirParcels = dirParcels;
	}
}