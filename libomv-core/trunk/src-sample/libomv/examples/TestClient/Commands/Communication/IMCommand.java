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
package libomv.examples.TestClient.Commands.Communication;

import java.util.HashMap;
import java.util.Map.Entry;

import libomv.AvatarManager.AvatarPickerReplyCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

public class IMCommand extends Command
{
    private static final String usage = "Usage: im <firstname> <lastname> <message>";
    String ToAvatarName = Helpers.EmptyString;
    TimeoutEvent<UUID> NameSearchEvent = new TimeoutEvent<UUID>();
    HashMap<String, UUID> Name2Key = new HashMap<String, UUID>();

    public IMCommand(TestClient testClient)
    {
        testClient.Avatars.OnAvatarPickerReply.add(new Avatars_AvatarPickerReply());

        Name = "im";
        Description = "Instant message someone. " + usage;
        Category = CommandCategory.Communication;
    }
    
    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 3)
            return usage;

        ToAvatarName = args[0] + " " + args[1];

        // Build the message
        String message = Helpers.EmptyString;
        for (int ct = 2; ct < args.length; ct++)
            message += args[ct] + " ";
        message = message.trim();
        if (message.length() > 1023) message = message.substring(0, 1023);

        if (!Name2Key.containsKey(ToAvatarName.toLowerCase()))
        {
            // Send the Query
            Client.Avatars.RequestAvatarNameSearch(ToAvatarName, new UUID());

            UUID uuid = NameSearchEvent.waitOne(6000, true);
            if (uuid != null)
            {
                Name2Key.put(ToAvatarName.toLowerCase(), uuid);            	
            }
        }

        if (Name2Key.containsKey(ToAvatarName.toLowerCase()))
        {
            UUID id = Name2Key.get(ToAvatarName.toLowerCase());

            Client.Self.InstantMessage(id, message);
            return "Instant messaged " + ToAvatarName + " {" + id + "} with message: " + message;
        }
        return "Name lookup for " + ToAvatarName + " failed";
    }

    private class Avatars_AvatarPickerReply implements Callback<AvatarPickerReplyCallbackArgs>
    {
    	public boolean callback(AvatarPickerReplyCallbackArgs e)
    	{
            for (Entry<UUID, String> kvp : e.getAvatars().entrySet())
            {
                if (kvp.getValue().equalsIgnoreCase(ToAvatarName))
                {
                    NameSearchEvent.set(kvp.getKey());
                }
            }
            return false;
    	}
    }
}

