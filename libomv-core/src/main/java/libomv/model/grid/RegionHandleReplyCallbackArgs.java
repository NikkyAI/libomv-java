package libomv.model.grid;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class RegionHandleReplyCallbackArgs implements CallbackArgs {
	private final UUID m_RegionID;
	// TODO was: private readonly ulong m_RegionHandle;
	private final long m_RegionHandle;

	public final UUID getRegionID() {
		return m_RegionID;
	}

	// TODO was: public ulong getRegionHandle()
	public final long getRegionHandle() {
		return m_RegionHandle;
	}

	// TODO was: public RegionHandleReplyEventArgs(UUID regionID, ulong
	// regionHandle)
	public RegionHandleReplyCallbackArgs(UUID regionID, long regionHandle) {
		this.m_RegionID = regionID;
		this.m_RegionHandle = regionHandle;
	}
}