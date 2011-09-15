 package libomv.utils;

import java.util.HashMap;
import java.util.Map.Entry;

import libomv.utils.CallbackHandler;

public class CallbackHandlerQueue <T> {
	private HashMap<CallbackHandler<T>, Boolean> callbackHandlers = null;

	public int count()
	{
		if (callbackHandlers == null)
			return 0;

		return callbackHandlers.size();
	}
	
	public boolean add(CallbackHandler<T> handler)
	{
		return add(handler, false);
	}
		
	/**
	 * Add a callback handler to the list of handlers
	 * 
	 * @param handler The callback handler to add to the list
	 * @param autoremove When true the callback handler is automatically removed when invoked
	 * @return True when the callback handler replaced an earlier instance of itself, false otherwise
	 */
	public boolean add(CallbackHandler<T> handler, boolean autoremove)
	{
	    if (callbackHandlers == null)
		    callbackHandlers = new HashMap<CallbackHandler<T>,  Boolean>();

	    synchronized (callbackHandlers)
	    {
		    return (callbackHandlers.put(handler, autoremove) != null);
	    }
	}

	/**
	 * Remove a callback handler from the list of handlers
	 * 
	 * @param handler The callback handler to add to the list
	 * @param autoremove When true the callback handler is automatically removed when invoked
	 * @return True when the callback handler was removed, false when it didn't exist
	 */
	public boolean remove(CallbackHandler<T> handler)
	{
		if (callbackHandlers == null)
			return false;

		synchronized (callbackHandlers)
	    {
		    return (callbackHandlers.remove(handler) != null);
	    }
	}

	/**
	 * Dispatches a callback to all registered handlers
	 * 
	 * @param args The argument class to pass to the callback handlers
	 * @return The number of callback handlers that got invoked
	 */
	public int dispatch(T args)
	{
		int count = 0;
		
		if (callbackHandlers != null)
		{
		    synchronized (callbackHandlers)
		    {
		        for (Entry<CallbackHandler<T>, Boolean> entry : callbackHandlers.entrySet())
		        {
		        	CallbackHandler<T> handler = entry.getKey();

		        	handler.dispatch(args);
		    	    if (entry.getValue())
		    	    	remove(handler);
		    	    count++;
		        }
		    }
		}
		return count;
	}
}
