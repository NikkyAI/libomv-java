package libomv.utils;

public abstract class CallbackHandler <T>
{
	public abstract void callback(T params);

	private final Integer timer = 0;
	public final void dispatch(T params)
	{
		callback(params);
		this.timer.notifyAll();
	}
	
	public final boolean waitms(long timeout)
	{
		try
		{
			this.timer.wait(timeout);
			return false;
		}
		catch (InterruptedException e)
		{
			return true;
		}
	}
}
