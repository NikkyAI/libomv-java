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
package libomv.examples.TestClient.Commands.Communication;

import libomv.AgentManager.GroupChatJoinedCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

public class IMGroupCommand extends Command
{
    private static final String usage = "Usage: imgroup <group_uuid> <message>";
    TimeoutEvent<Boolean>WaitForSessionStart = new TimeoutEvent<Boolean>();

    public IMGroupCommand(TestClient testClient)
    {
        Name = "imgroup";
        Description = "Send an instant message to a group. " + usage;
        Category = CommandCategory.Communication;
    }
    
    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 2)
            return usage;

        UUID ToGroupID = UUID.parse(args[0]);
        if (ToGroupID == null)
        	return "Invalid group uuid. " + usage;

        // Build the message
        String message = Helpers.EmptyString;
        for (int ct = 2; ct < args.length; ct++)
            message += args[ct] + " ";
        message = message.trim();
        if (message.length() > 1023) message = message.substring(0, 1023);

        Callback<GroupChatJoinedCallbackArgs> handler = new Self_GroupChatJoined();
        Client.Self.OnGroupChatJoined.add(handler);

        if (!Client.Self.GroupChatSessions.containsKey(ToGroupID))
        {
            WaitForSessionStart.reset();
            Client.Self.RequestJoinGroupChat(ToGroupID);
        }
        else
        {
            WaitForSessionStart.set(true);
        }
        
        if (WaitForSessionStart.waitOne(20000, true))
        {
            Client.Self.InstantMessageGroup(ToGroupID, message);
        }
        else
        {
            return "Timeout waiting for group session start";
        }

        Client.Self.OnGroupChatJoined.remove(handler);
        return "Instant Messaged group " + ToGroupID + " with message: " + message;
    }

    private class Self_GroupChatJoined implements Callback<GroupChatJoinedCallbackArgs>
    {
    	public boolean callback(GroupChatJoinedCallbackArgs e)
    	{
            if (e.getSucess())
            {
            	System.out.println("Joined " + e.getSessionName() + " Group Chat Success!");
            }
            else
            {
            	System.out.println("Join Group Chat failed :(");
            }
            WaitForSessionStart.set(e.getSucess());
            return false;
    	}
    }
}

