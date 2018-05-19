package libomv.model.asset;

// #region Transfer Classes
public class DelayedTransfer {
	public StatusCode status;
	public byte[] data;

	public DelayedTransfer(StatusCode status, byte[] data) {
		this.status = status;
		this.data = data;
	}
}