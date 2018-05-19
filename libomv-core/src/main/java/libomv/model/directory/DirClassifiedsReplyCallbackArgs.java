package libomv.model.directory;

import java.util.ArrayList;

import libomv.utils.CallbackArgs;

/** Contains the classified data returned from the data server */
public class DirClassifiedsReplyCallbackArgs implements CallbackArgs {
	private final ArrayList<Classified> classifieds;

	/**
	 * Construct a new instance of the DirClassifiedsReplyEventArgs class
	 *
	 * @param classifieds
	 *            A list of classified ad data returned from the data server
	 */
	public DirClassifiedsReplyCallbackArgs(ArrayList<Classified> classifieds) {
		this.classifieds = classifieds;
	}

	// A list containing Classified Ads returned by the data server
	public final ArrayList<Classified> getClassifieds() {
		return classifieds;
	}

}