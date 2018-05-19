package libomv.model.estate;

import java.util.HashMap;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

/**
 * Raised on LandStatReply when the report type is for "top Scripts"
 */
public class TopScriptsReplyCallbackArgs implements CallbackArgs {
	private final int objectCount;
	private final HashMap<UUID, EstateTask> tasks;

	/**
	 * Construct a new instance of the TopScriptsReplyEventArgs class
	 *
	 * @param objectCount
	 *            The number of returned items in LandStatReply
	 * @param tasks
	 *            Dictionary of Object UUIDs to tasks returned in LandStatReply
	 */
	public TopScriptsReplyCallbackArgs(int objectCount, HashMap<UUID, EstateTask> tasks) {
		this.objectCount = objectCount;
		this.tasks = tasks;
	}

	// The number of scripts returned in LandStatReply
	public int getObjectCount() {
		return objectCount;
	}

	// A Dictionary of Object UUIDs to tasks returned in LandStatReply
	public HashMap<UUID, EstateTask> getTasks() {
		return tasks;
	}

}