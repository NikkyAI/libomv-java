/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.examples.TestClient.Commands.Land;

import java.util.List;

import libomv.GridManager.GridItemType;
import libomv.GridManager.GridLayerType;
import libomv.GridManager.MapItem;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class AgentLocationsCommand extends Command
{
    private static final String usage = "Usage: agentlocations <regionhandle>";

    public AgentLocationsCommand(TestClient testClient)
    {
        Name = "agentlocations";
        Description = "Downloads all of the agent locations in the current or a specified region. " + usage;
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
                return usage;        		
        	}
        }
        else
            return usage;

        List<MapItem> items = Client.Grid.MapItems(regionHandle, GridItemType.AgentLocations, GridLayerType.Objects, 1000 * 20);
        if (items != null)
        {
            StringBuilder ret = new StringBuilder();
            ret.append("Agent locations:\n");

            for (int i = 0; i < items.size(); i++)
            {
                MapItem location = items.get(i);

                ret.append(String.format("%d avatar(s) at %.1f/%.1f\n", location.AvatarCount, location.getLocalX(), location.getLocalY()));
            }
            return ret.toString();
        }
        else
        {
            return "Failed to fetch agent locations";
        }
    }
}
