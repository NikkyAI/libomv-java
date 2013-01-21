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
package libomv.examples.TestClient.Commands.Movement;

import libomv.examples.TestClient.TestClient;
import libomv.examples.TestClient.Command;
import libomv.primitives.Primitive;
import libomv.types.UUID;
import libomv.types.Vector3;

public class SitOnCommand extends Command
{
    private static final String usage = "Usage: siton <object uuid>";

    public SitOnCommand(TestClient testClient)
    {
        Name = "siton";
        Description = "Attempt to sit on a particular prim, with specified UUID. " + usage;
        Category = CommandCategory.Movement;
    }

    @Override
	public String execute(String[] args, UUID fromAgentID)
    {
        if (args.length != 1)
            return usage;
        
        UUID target = UUID.parse(args[0]);
        if (target != null)
        {
            Primitive targetPrim = Client.Network.getCurrentSim().getObjectsPrimitives().get(target);
            if (targetPrim != null)
            {
                try
				{
					Client.Self.RequestSit(targetPrim.ID, Vector3.Zero);
	                Client.Self.Sit();
				}
				catch (Exception e)
				{
	                return "Exception while trying to sit on prim " + targetPrim.ID + " (" + targetPrim.LocalID + ")";
				}
                return "Requested to sit on prim " + targetPrim.ID + " (" + targetPrim.LocalID + ")";
            }
            return "Couldn't find a prim to sit on with UUID " + args[0];
        }
        return usage;
    }
}

