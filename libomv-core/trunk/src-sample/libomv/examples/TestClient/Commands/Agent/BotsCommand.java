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

import java.util.HashMap;

import libomv.AvatarManager.ViewerEffectCallbackArgs;
import libomv.Simulator;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Avatar;
import libomv.types.UUID;
import libomv.utils.Callback;

public class BotsCommand extends Command
{
    private HashMap<UUID, Boolean> m_AgentList = new HashMap<UUID, Boolean>();

    public BotsCommand(TestClient testClient)
    {
        Name = "bots";
        Description = "detects avatars that appear to be bots. Usage: bots";
        Category = CommandCategory.Other;        
        testClient.Avatars.OnViewerEffect.add(new Avatars_ViewerEffect());
    }
    
    private class Avatars_ViewerEffect implements Callback<ViewerEffectCallbackArgs>
    {
    	public boolean callback(ViewerEffectCallbackArgs e)
    	{
            synchronized (m_AgentList)
            {
                m_AgentList.put(e.getSourceAvatar(), true);
            }
            return false;
    	}
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        StringBuilder result = new StringBuilder();

        synchronized (Client.Network.getSimulators())
        {
            for (Simulator sim : Client.Network.getSimulators())
            {
           		for (Avatar av : sim.getObjectsAvatars().values())
                {
          			synchronized (m_AgentList)
                    {
                        if (!m_AgentList.containsKey(av.ID))
                        {
                            result.append("\n" + av.getName() + " (Group: " + av.getGroupName() +", Location: " + av.Position + 
                            		      ", UUID: " + av.ID + " LocalID: " + av.LocalID + ") is Probably a bot");
                        }
                    }
                }
            }
        }
        return result.toString();
    }
}