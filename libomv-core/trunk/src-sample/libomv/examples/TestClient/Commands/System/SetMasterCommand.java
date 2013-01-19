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
package libomv.examples.TestClient.Commands.System;

import libomv.DirectoryManager.DirPeopleReplyCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

public class SetMasterCommand extends Command
{
    private TimeoutEvent<UUID> keyResolution = new TimeoutEvent<UUID>();
    private UUID query = UUID.Zero;

    public SetMasterCommand(TestClient testClient)
	{
		Name = "setmaster";
        Description = "Sets the user name of the master user. The master user can IM to run commands. Usage: setmaster [uuid|name]";
        Category = CommandCategory.TestClient;
	}

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
	{
        if (args.length < 1)
        {
        	Client.MasterKey = null;
        }

        String master = Helpers.EmptyString;
		for (int ct = 0; ct < args.length;ct++)
			master = master + args[ct] + " ";
        master = master.trim();

        if (master.length() == 0)
            return "Usage: setmaster [uuid|name]";
        
        UUID uuid = UUID.parse(master);
        if (uuid == null)
        {
            keyResolution.reset();
            Callback<DirPeopleReplyCallbackArgs> callback = new KeyResolveHandler();
            Client.Directory.OnDirPeople.add(callback, true);

            query = Client.Directory.StartPeopleSearch(master, 0);
            uuid = keyResolution.waitOne(60000);
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

        return String.format("Master set to %s (%s)", master, uuid.toString());
	}

    private class KeyResolveHandler implements Callback<DirPeopleReplyCallbackArgs>
    {
    	public boolean callback(DirPeopleReplyCallbackArgs e)
    	{
            if (query.equals(e.getQueryID()))
            {
            	keyResolution.set(e.getMatchedPeople().get(0).AgentID);
            	query = UUID.Zero;
            }
            return true;
        }
    }
}