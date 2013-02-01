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

import libomv.FriendsManager.FriendInfo;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

// Shows a list of friends
public class FriendsCommand extends Command
{        
    public FriendsCommand(TestClient testClient)
    {
        Name = "friends";
        Description = "List avatar friends. Usage: friends";
        Category = CommandCategory.Friends;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        // initialize a StringBuilder object used to return the results
        StringBuilder sb = new StringBuilder(Client.Self.getName() + " has ");

        // Only iterate the Friends dictionary if we actually have friends!
        int count = Client.Friends.getFriendList().size();
        if (count > 0)
        {
            // iterate over the InternalDictionary using a delegate to populate
            // our StringBuilder output string
            sb.append(count + (count == 1 ? " friend:\n" : " friends:\n"));
            for (FriendInfo friend : Client.Friends.getFriendList().values())
            {
                // append the name of the friend to our output
                sb.append(friend.getID() + ", " + friend.getName() + "\n");
            }
        }
        else
        {
            // we have no friends :(
            sb.append("no friends\n");   
        }

        // return the result
        return sb.toString();
    }
}
