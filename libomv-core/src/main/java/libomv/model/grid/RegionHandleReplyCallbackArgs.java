package libomv.model.grid;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class RegionHandleReplyCallbackArgs implements CallbackArgs {
	private final UUID regionID;
	// TODO was: private readonly ulong m_RegionHandle;
	private final long regionHandle;

	// TODO was: public RegionHandleReplyEventArgs(UUID regionID, ulong
	// regionHandle)
	public RegionHandleReplyCallbackArgs(UUID regionID, long regionHandle) {
		this.regionID = regionID;
		this.regionHandle = regionHandle;
	}

	public final UUID getRegionID() {
		return regionID;
	}

	// TODO was: public ulong getRegionHandle()
	public final long getRegionHandle() {
		return regionHandle;
	}

}