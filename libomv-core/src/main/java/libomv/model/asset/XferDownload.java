package libomv.model.asset;

import libomv.utils.Helpers;

public class XferDownload extends Transfer {
	public long xferID;
	public String filename = Helpers.EmptyString;
	public TransferError error = TransferError.None;

	public XferDownload() {
		super();
	}
}