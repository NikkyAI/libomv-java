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

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.packets.AgentPausePacket;
import libomv.packets.AgentResumePacket;
import libomv.types.UUID;

public class SleepCommand extends Command
{
    private static final String usage = "Usage: sleep <seconds>";
    private int sleepSerialNum = 1;
    
    public SleepCommand(TestClient testClient)
    {
        Name = "sleep";
        Description = "Uses AgentPause/AgentResume and sleeps for a given number of seconds. " + usage;
        Category = CommandCategory.TestClient;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        int seconds;
        
    	if (args.length != 1)
    	{
            return usage;
    	}
    	
        try
        {
    		seconds = Integer.valueOf(args[0]);
        }
        catch (NumberFormatException ex)
        {
        	return usage;
        }
         
        AgentPausePacket pause = new AgentPausePacket();
        pause.AgentData.AgentID = Client.Self.getAgentID();
        pause.AgentData.SessionID = Client.Self.getSessionID();
        pause.AgentData.SerialNum = sleepSerialNum++;

        Client.Network.sendPacket(pause);

        // Sleep
        Thread.sleep(seconds * 1000);

        AgentResumePacket resume = new AgentResumePacket();
        resume.AgentData.AgentID = Client.Self.getAgentID();
        resume.AgentData.SessionID = Client.Self.getSessionID();
        resume.AgentData.SerialNum = pause.AgentData.SerialNum;

        Client.Network.sendPacket(resume);

        return "Paused, slept for " + seconds + " second(s), and resumed";
    }
}
