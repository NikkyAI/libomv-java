package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.TestClient;
import libomv.examples.TestClient.Command;
import libomv.primitives.Primitive;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.RefObject;

public class SitOn extends Command
{
    public SitOn(TestClient testClient)
    {
        Name = "siton";
        Description = "Attempt to sit on a particular prim, with specified UUID";
        Category = CommandCategory.Movement;
    }

    @Override
	public String Execute(String[] args, UUID fromAgentID)
    {
        if (args.length != 1)
            return "Usage: siton UUID";

        RefObject<UUID> target = new RefObject<UUID>(null);
        if (UUID.TryParse(args[0], target))
        {
            Primitive targetPrim = Client.Network.getCurrentSim().getObjectsPrimitives().get(target.argvalue);
            if (targetPrim != null)
            {
                try
				{
					Client.Self.RequestSit(targetPrim.ID, Vector3.Zero);
	                Client.Self.Sit();
				}
				catch (Exception e)
				{
	                return "Exception while trying to sit on prim " + targetPrim.ID.toString() + " (" + targetPrim.LocalID + ")";
				}
                return "Requested to sit on prim " + targetPrim.ID.toString() + " (" + targetPrim.LocalID + ")";
            }
        }
        return "Couldn't find a prim to sit on with UUID " + args[0];
    }
}

