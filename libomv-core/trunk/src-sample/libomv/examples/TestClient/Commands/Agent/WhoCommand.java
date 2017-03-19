/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
package libomv.examples.TestClient.Commands.Agent;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Avatar;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class WhoCommand extends Command
{
    public WhoCommand(TestClient testClient)
	{
		Name = "who";
		Description = "Lists seen avatars in all currentlly connected simulators.";
        Category = CommandCategory.Other;
	}

    @Override
	public String execute(String[] args, UUID fromAgentID)
	{
		StringBuilder result = new StringBuilder();

        synchronized (Client.Network.getSimulators())
        {
            for (int i = 0; i < Client.Network.getSimulators().size(); i++)
            {
                for (Avatar av : Client.Network.getSimulators().get(i).getObjectsAvatars().values())
                {
                    result.append(Helpers.NewLine);
                    result.append(String.format("%s (Group: %s, Location: %s, UUID: %s LocalID: %s)",
                           av.getName(), av.getGroupName(), av.Position, av.ID, Helpers.LocalIDToString(av.LocalID)));
                }
            }
        }
        return result.toString();
	}
}
