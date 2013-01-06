package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Primitive;
import libomv.types.UUID;
import libomv.types.Vector3;

public class SitCommand extends Command
{
    public SitCommand(TestClient testClient)
	{
		Name = "sit";
		Description = "Attempt to sit on the closest prim";
        Category = CommandCategory.Movement;
	}
		
    public String execute(String[] args, UUID fromAgentID) throws Exception
	{
        Primitive closest = null;
	    double closestDistance = Double.MAX_VALUE;

        for (Primitive prim : Client.Network.getCurrentSim().getObjectsPrimitives().values())
        {
            float distance = Vector3.distance(Client.Self.getAgentPosition(), prim.Position);

            if (closest == null || distance < closestDistance)
            {
                closest = prim;
                closestDistance = distance;
            }
        }

        if (closest != null)
        {
            Client.Self.RequestSit(closest.ID, Vector3.Zero);
            Client.Self.Sit();

            return "Sat on " + closest.ID + " (" + closest.LocalID + "). Distance: " + closestDistance;
        }
        else
        {
            return "Couldn't find a nearby prim to sit on";
        }
	}
}
