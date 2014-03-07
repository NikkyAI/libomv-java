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

import libomv.AgentManager.TeleportCallbackArgs;
import libomv.AgentManager.TeleportStatus;
import libomv.examples.TestClient.TestClient;
import libomv.examples.TestClient.Command;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Helpers;

public class GotoCommand extends Command
{
    private static final String usage = "Usage: goto <sim name>/<x>/<y>/<z>";
    private String message = null;

    public GotoCommand(TestClient testClient)
    {
		Name = "goto";
		Description = "Teleport to a location (e.g. \"goto Hooper Islands/100/100/30\") " + usage;
        Category = CommandCategory.Movement;
    }

    @Override
	public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 1)
        {
            return usage;
        }

        String destination = Helpers.EmptyString;

        // Handle multi-word sim names by combining the arguments
        for (String arg : args)
        {
            destination += arg + " ";
        }
        destination = destination.trim();

        String[] tokens = destination.split("/");
        if (tokens.length != 4)
            return usage;

        String sim = tokens[0];
        float x, y, z;
        try
        {
        	 x = Float.valueOf(tokens[1]);
        	 y = Float.valueOf(tokens[2]);
        	 z = Float.valueOf(tokens[3]);
        }
        catch (NumberFormatException ex)
        {
            return usage;
        }

        Client.Self.OnTeleport.add(new Callback<TeleportCallbackArgs>()
        {
			@Override
			public boolean callback(TeleportCallbackArgs params)
			{
				if (params.getStatus() == TeleportStatus.Failed)
					message = params.getMessage();
				return true;
			}
        }, true);
        if (Client.Self.Teleport(sim, new Vector3(x, y, z)))
            return "Teleported to " + Client.Network.getCurrentSim().getName();
        else
            return "Teleport failed: " + message;
	}
}