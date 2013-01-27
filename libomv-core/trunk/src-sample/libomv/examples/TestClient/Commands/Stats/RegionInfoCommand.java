/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.examples.TestClient.Commands.Stats;

import libomv.Simulator;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class RegionInfoCommand extends Command
{
    private static final String usage = "Usage: regioninfo list            ; list all currently known regions\n" +
                                        "       regioninfo [<name>|<uuid>] ; output info for the region or the current region if no parameter";

    public RegionInfoCommand(TestClient testClient)
	{
		Name = "regioninfo";
		Description = "Prints out info about the current region. " + usage;
        Category = CommandCategory.Simulator;
	}

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
		if (args.length == 0)
		{
			return formatRegionInfo(Client.Network.getCurrentSim());
		}
		
		if (args[0].equalsIgnoreCase("list"))
		{
			StringBuilder output = new StringBuilder("Known regions:\n");
			for (Simulator sim : Client.Network.getSimulators())
			{
				output.append(String.format("%-30s UUID: %s\n", sim.getSimName(), sim.ID));
			}
			return output.toString();
		}

        String name = Helpers.EmptyString;
		UUID targetID = UUID.parse(args[0]);
        if (targetID == null)
        {
            for (int i = 0; i < args.length; i++)
            	name += args[i] + " ";
            name = name.trim();
        }

        for (Simulator sim : Client.Network.getSimulators())
		{
			if ((sim.ID != null && sim.ID.equals(targetID)) || (sim.getSimName() != null && sim.getSimName().equals(name)))
			{
				return formatRegionInfo(sim);
			}
		}
		return usage;	
    }
	
	private String formatRegionInfo(Simulator sim)
	{
        StringBuilder output = new StringBuilder();
        output.append("Region name: " + sim.getSimName() + "\n");
        output.append("UUID: " + sim.ID + "\n");

        int[] pos = new int[2];
        Helpers.LongToUInts(sim.getHandle(), pos);
        output.append(String.format("Handle: %d (X: %d Y: %d)", sim.getHandle(), pos[0] & 0xFFFFFFFFL, pos[1] & 0xFFFFFFFFL) + "\n");

        output.append("Access: " + sim.Access + "\n");
        output.append("Flags: 0x" + Long.toHexString(sim.Flags) + "\n");
        output.append("TerrainBase0: " + sim.TerrainBase0 + "\n");
        output.append("TerrainBase1: " + sim.TerrainBase1 + "\n");
        output.append("TerrainBase2: " + sim.TerrainBase2 + "\n");
        output.append("TerrainBase3: " + sim.TerrainBase3 + "\n");
        output.append("TerrainDetail0: " + sim.TerrainDetail0 + "\n");
        output.append("TerrainDetail1: " + sim.TerrainDetail1 + "\n");
        output.append("TerrainDetail2: " + sim.TerrainDetail2 + "\n");
        output.append("TerrainDetail3: " + sim.TerrainDetail3 + "\n");
        output.append("Water Height: " + sim.WaterHeight + "\n");
        output.append("Datacenter:" + sim.ColoLocation + "\n");
        output.append("CPU Ratio:" + sim.CPURatio + "\n");
        output.append("CPU Class:" + sim.CPUClass + "\n");
        output.append("Region SKU/Type:" + sim.ProductSku + " " + sim.ProductName + "\n");

        return output.toString();
    }
}
