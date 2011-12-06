package libomv.examples.TestClient.Commands.System;

import libomv.examples.TestClient.ClientManager;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class WaitForLoginCommand extends Command
{
    public WaitForLoginCommand(TestClient testClient)
    {
        Name = "waitforlogin";
        Description = "Waits until all bots that are currently attempting to login have succeeded or failed";
        Category = CommandCategory.TestClient;
    }

	@Override
	public String Execute(String[] args, UUID fromAgentID)
	{
		try
		{
	        while (ClientManager.getInstance().PendingLogins > 0)
	        {
	            Thread.sleep(1000);
	            Logger.Log("Pending logins: " + ClientManager.getInstance().PendingLogins, LogLevel.Info);
	        }
		}
		catch (Exception ex) { }

        return "All pending logins have completed, currently tracking " + ClientManager.getInstance().Clients.size() + " bots";
	}

}
