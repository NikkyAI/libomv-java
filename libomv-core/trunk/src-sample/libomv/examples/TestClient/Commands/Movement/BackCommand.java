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
package libomv.examples.TestClient.Commands.Movement;

import libomv.AgentManager;
import libomv.AgentManager.AgentFlags;
import libomv.AgentManager.AgentMovement;
import libomv.AgentManager.AgentState;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class BackCommand extends Command
{
    public BackCommand(TestClient client)
    {
        Name = "back";
        Description = "Sends the move back command to the server for a single packet or a given number of seconds. Usage: back [seconds]";
        Category = CommandCategory.Movement;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length > 1)
            return "Usage: back [seconds]";

        AgentMovement Movement = Client.Self.getMovement();
        if (args.length == 0)
        {
            Movement.SendManualUpdate(AgentManager.ControlFlags.AGENT_CONTROL_AT_NEG, Movement.Camera.getPosition(),
                Movement.Camera.getAtAxis(), Movement.Camera.getLeftAxis(), Movement.Camera.getUpAxis(),
                Movement.BodyRotation, Movement.HeadRotation, Movement.Camera.Far, AgentFlags.None, AgentState.None, true);
        }
        else
        {
            // Parse the number of seconds
            long duration = 0;
            try
            {
            	duration = Long.valueOf(args[0]) * 1000;
            }
            catch (NumberFormatException ex)
            {}
            if (duration == 0)
                return "Usage: back [seconds]";

            long start = System.currentTimeMillis();

            Movement.setAtNeg(true);

            while (System.currentTimeMillis() - start < duration)
            {
                // The movement timer will do this automatically, but we do it here as an example
                // and to make sure updates are being sent out fast enough
                Movement.SendUpdate(false);
                Thread.sleep(100);
            }
            Movement.setAtNeg(false);
        }
        return "Moved backward";
    }
}
