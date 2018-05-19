package libomv.model.asset;

import libomv.utils.Helpers;

public class XferDownload extends Transfer {
	public long XferID;
	public String Filename = Helpers.EmptyString;
	public TransferError Error = TransferError.None;

	public XferDownload() {
		super();
	}
}