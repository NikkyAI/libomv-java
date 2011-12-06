package libomv.examples.TestClient.Commands.Agents;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Avatar;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class WhoCommand extends Command
{
    public WhoCommand(TestClient testClient)
	{
		Name = "who";
		Description = "Lists seen avatars.";
        Category = CommandCategory.Other;
	}

    @Override
	public String Execute(String[] args, UUID fromAgentID)
	{
		StringBuilder result = new StringBuilder();

        synchronized (Client.Network.getSimulators())
        {
            for (int i = 0; i < Client.Network.getSimulators().size(); i++)
            {
                for (Avatar av : Client.Network.getSimulators().get(i).getObjectsAvatars().values())
                {
                    result.append(Helpers.NewLine);
                    result.append(String.format("%s (Group: %s, Location: %s, UUID: %s LocalID: %s)",
                           av.getName(), av.getGroupName(), av.Position, av.ID, av.LocalID));
                }
            }
        }
        return result.toString();
	}
}
