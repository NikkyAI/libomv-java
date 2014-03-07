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
package libomv.examples.TestClient.Commands.System;

import java.util.ArrayList;

import libomv.DirectoryManager.AgentSearchData;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class SetMasterCommand extends Command
{
    private static final String usage = "Usage: setmaster [<agent name>|<agent uuid>]";

    public SetMasterCommand(TestClient testClient)
	{
		Name = "setmaster";
        Description = "Sets the master user who can IM to run commands. To clear the current master use without parameters. " + usage;
        Category = CommandCategory.TestClient;
	}

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
	{
        if (args.length < 1)
        {
        	Client.MasterKey = null;
        	Client.MasterName = null;
            return "Master cleared";
        }

        String master = Helpers.EmptyString;
		for (int ct = 0; ct < args.length;ct++)
			master = master + args[ct] + " ";
        master = master.trim();

        if (master.length() == 0)
            return usage;
        
        UUID uuid = UUID.parse(master);
        if (uuid == null)
        {
            ArrayList<AgentSearchData> uuids = Client.findFromAgentName(master, 10000);
            if (uuids != null && uuids.size() == 1)
            {
            	uuid = uuids.get(0).AgentID;
            	Client.MasterName = master;
            }
        }
        else
        {
        	Client.MasterName = null;
        }

        if (uuid != null)
        {
            Client.MasterKey = uuid;
        }
        else
        {
            return "Unable to obtain UUID for \"" + master + "\". Master unchanged.";
        }
        
        // Send an Online-only IM to the new master
        Client.Self.InstantMessage(Client.MasterKey, "You are now my master.  IM me with \"help\" for a command list.");

        return String.format("Master set to %s (%s)", master, uuid);
	}
}