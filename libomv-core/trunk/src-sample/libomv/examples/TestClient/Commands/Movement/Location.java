package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.TestClient;
import libomv.examples.TestClient.Command;
import libomv.types.UUID;

public class Location extends Command
{
    public Location(TestClient testClient)
	{
		Name = "location";
		Description = "Show current location of avatar.";
        Category = CommandCategory.Movement;
	}

	@Override
	public String Execute(String[] args, UUID fromAgentID)
	{
        return "CurrentSim: '" + Client.Network.getCurrentSim().toString() +
               "' Position: " + Client.Self.getSimRotation().toString();
	}
}
