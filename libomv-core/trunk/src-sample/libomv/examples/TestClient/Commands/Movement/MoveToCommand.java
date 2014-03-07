/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2014, Frederick Martian
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
package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class MoveToCommand extends Command
{
    private static final String usage = "Usage: moveto <x> <y> <z>";

    public MoveToCommand(TestClient client)
    {
        Name = "moveto";
        Description = "Moves the avatar to the specified global position using simulator autopilot. " + usage;
        Category = CommandCategory.Movement;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length != 3)
            return usage;

        double x, y, z;

        try
        {
        	x = Double.valueOf(args[0]);
            y = Double.valueOf(args[1]);
            z = Double.valueOf(args[2]);
        }
        catch (NumberFormatException ex)
        {
            return usage;
        }

        // Convert the local coordinates to global ones by adding the region handle parts to x and y
        int[] region = new int[2];
        Helpers.LongToUInts(Client.Network.getCurrentSim().getHandle(), region);
        x += region[0];
        y += region[1];

        Client.Self.AutoPilot(x, y, z);

        return String.format("Attempting to move to <%.1f,%.1f,%.1f>", x, y, z);

    }
}
