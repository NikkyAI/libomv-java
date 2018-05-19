package libomv.model.asset;

// #region Transfer Classes
public class DelayedTransfer {
	public StatusCode Status;
	public byte[] Data;

	public DelayedTransfer(StatusCode status, byte[] data) {
		this.Status = status;
		this.Data = data;
	}
}