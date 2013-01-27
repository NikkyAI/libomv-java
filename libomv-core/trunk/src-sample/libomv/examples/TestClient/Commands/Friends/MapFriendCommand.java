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
package libomv.examples.TestClient.Commands.Friends;

import libomv.FriendsManager.FriendFoundReplyCallbackArgs;
import libomv.FriendsManager.FriendInfo;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

public class MapFriendCommand extends Command
{
    private static final String usage = "Usage: mapfriend <agent uuid>|<agentname>";
    private TimeoutEvent<Boolean> WaitforFriend = new TimeoutEvent<Boolean>();

    public MapFriendCommand(TestClient testClient)
    {
        Name = "mapfriend";
        Description = "Show a friends location. " + usage;
        Category = CommandCategory.Friends;
    }

    @Override
	public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 1)
            return usage;

        UUID targetID = UUID.parse(args[0]);
        if (targetID == null)
        {
            String friendName = Helpers.EmptyString;
            for (int i = 0; i < args.length; i++)
            	friendName += args[i] + " ";
            friendName = friendName.trim();

            for (FriendInfo friend : Client.Friends.getFriendList().values())
        	{
        		if (friend.getName().equals(friendName))
        		{
        			targetID = friend.getID();
        			break;
        		}
        	}
        }
        if (targetID == null)
        {
        	return usage;
        }
        
        final StringBuilder sb = new StringBuilder();
        
        class FriendFoundReplay implements Callback<FriendFoundReplyCallbackArgs>
        {
			@Override
			public boolean callback(FriendFoundReplyCallbackArgs args)
			{
	            if (args.getRegionHandle() != 0)
	                sb.append("Found Friend " + args.getAgentID() + " in " + args.getRegionHandle() + " at " + args.getLocation().X + "/" + args.getLocation().X);
	            else
	                sb.append("Found Friend " + args.getAgentID() + " But they appear to be offline");

	            WaitforFriend.set(true);
				return true;
			}
        };

        Callback<FriendFoundReplyCallbackArgs> del = new FriendFoundReplay();
        Client.Friends.OnFriendFoundReply.add(del, true);
        WaitforFriend.reset();
        Client.Friends.MapFriend(targetID);
        if (WaitforFriend.waitOne(10000) == null)
        {
            sb.append("Timeout waiting for reply, Do you have mapping rights on " + targetID + "?");
        }
        Client.Friends.OnFriendFoundReply.remove(del);
        return sb.toString();
    }

}
