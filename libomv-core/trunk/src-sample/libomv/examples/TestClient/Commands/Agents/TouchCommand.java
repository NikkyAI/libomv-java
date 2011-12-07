package libomv.examples.TestClient.Commands.Agents;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Primitive;
import libomv.types.UUID;

public class TouchCommand extends Command
{
    public TouchCommand(TestClient testClient)
	{
		Name = "touch";
		Description = "Attempt to touch a prim with specified UUID";
        Category = CommandCategory.Objects;
	}
	
    @Override
	public String Execute(String[] args, UUID fromAgentID)
	{
        if (args.length != 1)
            return "Usage: touch UUID";
        UUID target = UUID.Parse(args[0]);
        
        if (target != null)
        {
        	synchronized (Client.Network.getCurrentSim())
        	{
    			for (Primitive prim : Client.Network.getCurrentSim().getObjectsPrimitives().values())
    			{
    				if (prim.ID.equals(target))
    				{
    	                try
						{
							Client.Self.Touch(prim.LocalID);
	    	                return "Touched prim " + prim.LocalID;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
    				}
				}
			}
        }
        return "Couldn't find a prim to touch with UUID " + args[0];
	}
}
