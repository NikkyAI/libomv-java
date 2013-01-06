package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class StandCommand extends Command
{
    public StandCommand(TestClient testClient)
    {
    	Name = "stand";
    	Description = "Stand";
        Category = CommandCategory.Movement;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        Client.Self.Stand();
	    return "Standing up.";  
    }
}
