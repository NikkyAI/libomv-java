package libomv.model.asset;

public class DelayedTransfer {
	public StatusCode status;
	public byte[] data;

	public DelayedTransfer(StatusCode status, byte[] data) {
		this.status = status;
		this.data = data;
	}
}