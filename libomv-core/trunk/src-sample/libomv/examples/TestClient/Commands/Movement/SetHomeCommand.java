package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class SetHomeCommand extends Command
{
	public SetHomeCommand(TestClient testClient)
    {
        Name = "sethome";
        Description = "Sets home to the current location.";
        Category = CommandCategory.Movement;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
		Client.Self.SetHome();
        return "Home set to " + Client.Network.getCurrentSim().getSimName() + " Pos: " + Client.Self.getAgentPosition().toString();
    }
}

