package libomv.utils;

public abstract class CallbackHandler <T>
{
	protected abstract void callback(T params);

	public synchronized final void dispatch(T params)
	{
		callback(params);
		notifyAll();
	}
	
	public synchronized final boolean waitms(long timeout)
	{
		try
		{
			wait(timeout);
			return false;
		}
		catch (InterruptedException e)
		{
			return true;
		}
	}
}
