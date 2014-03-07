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
package libomv.examples.TestClient.Commands.Directory;

import java.util.Map.Entry;

import libomv.AvatarManager.AgentNamesCallbackArgs;
import libomv.GroupManager.GroupProfileCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.TimeoutEvent;

public class Key2NameCommand extends Command
{
    private static final String usage = "Usage: key2name <agent uuid>";
    private TimeoutEvent<String> waitQuery = new TimeoutEvent<String>();

    public Key2NameCommand(TestClient testClient)
    {
        Name = "key2name";
        Description = "resolve a UUID to an avatar or group name. " + usage;
        Category = CommandCategory.Search;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 1)
            return usage;

        UUID key = UUID.parse(args[0].trim());
        if (key == null)
        {
            return "UUID " + args[0].trim() + " appears to be invalid. " + usage;
        }
        waitQuery.reset();
        
        Callback<GroupProfileCallbackArgs> groupCallback = new Groups_OnGroupProfile();
        Callback<AgentNamesCallbackArgs> agentCallback = new Avatars_OnAvatarNames();
        Client.Groups.OnGroupProfile.add(groupCallback);
        Client.Avatars.OnAgentNames.add(agentCallback);
        Client.Groups.RequestGroupProfile(key);
        Client.Avatars.RequestAvatarName(key, null); 
        String result = waitQuery.waitOne(10000);
        if (result == null)
        {
            result = "Timeout waiting for reply, this could mean the Key is not an avatar or a group.\n";
        }
        Client.Groups.OnGroupProfile.remove(groupCallback);
        Client.Avatars.OnAgentNames.remove(agentCallback);
        return result;
    }

    private class Groups_OnGroupProfile implements Callback<GroupProfileCallbackArgs>
    {
    	public boolean callback(GroupProfileCallbackArgs e)
    	{
            waitQuery.set("Group: " + e.getGroup().getName() + " " + e.getGroup().getID() + "\n");
            return true;
    	}
    }

    private class Avatars_OnAvatarNames implements Callback<AgentNamesCallbackArgs>
    {
    	public boolean callback(AgentNamesCallbackArgs e)
    	{
    		StringBuilder result = new StringBuilder();
    		for (Entry<UUID, String> kvp : e.getNames().entrySet())
    			result.append("Avatar: " + kvp.getValue() + " " + kvp.getKey() + "\n");
        	waitQuery.set(result.toString());
        	return true;
    	}
    }        
}