package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.TestClient;
import libomv.examples.TestClient.Command;
import libomv.types.UUID;

public class GoHomeCommand extends Command
{
	public GoHomeCommand(TestClient testClient)
    {
        Name = "gohome";
        Description = "Teleports home";
        Category = CommandCategory.Movement;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
		if ( Client.Self.GoHome() )
		{
			return "Teleport Home Succesful";
		}
		else
		{	
			return "Teleport Home Failed";
		}
    }

}
