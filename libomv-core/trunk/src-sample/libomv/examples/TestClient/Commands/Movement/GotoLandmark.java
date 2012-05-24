package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.TestClient;
import libomv.examples.TestClient.Command;
import libomv.types.UUID;
import libomv.utils.RefObject;

public class GotoLandmark extends Command
{
    public GotoLandmark(TestClient testClient)
    {
        Name = "goto_landmark";
        Description = "Teleports to a Landmark. Usage: goto_landmark [UUID]";
        Category = CommandCategory.Movement;
    }

    @Override
	public String Execute(String[] args, UUID fromAgentID)
    {
        if (args.length < 1)
        {
            return "Usage: goto_landmark [UUID]";
        }

        RefObject<UUID> landmark = new RefObject<UUID>(null);
        if (UUID.TryParse(args[0], landmark))
        {
        	System.out.println("Teleporting to " + landmark.argvalue.toString());

            try
			{
				if (Client.Self.Teleport(landmark.argvalue))
				{
				    return "Teleport Succesful";
				}
			}
			catch (Exception e)
			{
                return "Exception while trying to teleport to " + landmark.argvalue.toString();
			}
			return "Teleport Failed";
        }
		return "Invalid LLUID";
    }
}
