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
package libomv.examples.TestClient.Commands.Agent;

import java.util.HashMap;
import java.util.Map.Entry;

import libomv.Animations;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class PlayAnimationCommand extends Command
{        
    private HashMap<UUID, String> m_BuiltInAnimations = new HashMap<UUID, String>(Animations.toDictionary());
    public PlayAnimationCommand(TestClient testClient)
    {
        Name = "play";
        Description = "Attempts to play an animation";
        Category = CommandCategory.Appearance;                        
    }

    private String usage()
    {
        String usage = "Usage:\n" +
            "\tplay list - list the built in animations\n" +
            "\tplay show - show any currently playing animations\n" +
            "\tplay <uuid> - play an animation asset\n" +
            "\tplay <animation> - where <animation> is one of the values returned from \"play list\"\n";
        return usage;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {            
        StringBuilder result = new StringBuilder();
        if (args.length != 1)
            return usage();

        String arg = args[0].trim();
        UUID animationID = UUID.parse(arg);
        

        if (animationID != null)
        {
            Client.Self.AnimationStart(animationID, true);
        }
        else if (arg.toLowerCase().equals("list"))
        {
            for (String key : m_BuiltInAnimations.values())
            {
                result.append(key + "\n");
            }
        }
        else if (arg.toLowerCase().equals("show"))
        {
            for (Entry<UUID, Integer> e : Client.Self.SignaledAnimations.entrySet())
            {
                if (m_BuiltInAnimations.containsKey(e.getKey()))
                {
                    result.append("The " + m_BuiltInAnimations.get(e.getKey()) + " System Animation is being played, sequence is " + e.getValue());
                }
                else
                {
                    result.append("The " + e.getKey() + " Asset Animation is being played, sequence is " + e.getValue());
                }
            }                               
        }
        else if (m_BuiltInAnimations.containsValue(args[0].trim().toUpperCase()))
        {
            for (Entry<UUID, String> e : m_BuiltInAnimations.entrySet())
            {
                if (e.getValue().equals(arg.toUpperCase()))
                {
                    Client.Self.AnimationStart(e.getKey(), true);
                    break;
                }
            }
        }
        else
        {
            return usage();
        }
        return result.toString();
    }
}
