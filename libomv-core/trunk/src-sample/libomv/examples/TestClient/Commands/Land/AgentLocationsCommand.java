package libomv.examples.TestClient.Commands.Land;

import java.util.List;

import libomv.GridManager.GridItemType;
import libomv.GridManager.GridLayerType;
import libomv.GridManager.MapAgentLocation;
import libomv.GridManager.MapItem;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class AgentLocationsCommand extends Command
{
    public AgentLocationsCommand(TestClient testClient)
    {
        Name = "agentlocations";
        Description = "Downloads all of the agent locations in a specified region. Usage: agentlocations [regionhandle]";
        Category = CommandCategory.Simulator;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        long regionHandle = 0;

        if (args.length == 0)
        {
            regionHandle = Client.Network.getCurrentSim().getHandle();
        }
        else if (args.length == 1)
        {
        	try
        	{
        		regionHandle = Long.valueOf(args[0]);
        	}
        	catch (NumberFormatException ex)
        	{
                return "Usage: agentlocations [regionhandle]";        		
        	}
        }
        else
            return "Usage: agentlocations [regionhandle]";

        List<MapItem> items = Client.Grid.MapItems(regionHandle, GridItemType.AgentLocations, GridLayerType.Objects, 1000 * 20);

        if (items != null)
        {
            StringBuilder ret = new StringBuilder();
            ret.append("Agent locations:\n");

            for (int i = 0; i < items.size(); i++)
            {
                MapAgentLocation location = (MapAgentLocation)items.get(i);

                ret.append(String.format("%d avatar(s) at %d,%d", location.AvatarCount, location.getLocalX(),
                    location.getLocalY()));
            }
            return ret.toString();
        }
        else
        {
            return "Failed to fetch agent locations";
        }
    }
}
