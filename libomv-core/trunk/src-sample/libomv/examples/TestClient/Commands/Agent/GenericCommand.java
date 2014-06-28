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

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.packets.GenericMessagePacket;
import libomv.types.UUID;
import libomv.utils.Helpers;

/// <summary>
/// Sends a packet of type GenericMessage to the simulator.
/// </summary>
public class GenericCommand extends Command
{
    public GenericCommand(TestClient testClient)
    {
        Name = "sendgeneric";
        Description = "send a generic UDP message to the simulator.";
        Category = CommandCategory.Other;        
    }

    @Override
	public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 1)
            return "Usage: sendgeneric method_name [value1 value2 ...]";

        String methodName = args[0];

        GenericMessagePacket gmp = new GenericMessagePacket();

        gmp.AgentData.AgentID = Client.Self.getAgentID();
        gmp.AgentData.SessionID = Client.Self.getSessionID();
        gmp.AgentData.TransactionID = UUID.Zero;

        gmp.MethodData.setMethod(Helpers.StringToBytes(methodName));
        gmp.MethodData.Invoice = UUID.Zero;

        gmp.ParamList = new GenericMessagePacket.ParamListBlock[args.length - 1];

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++)
        {
            GenericMessagePacket.ParamListBlock paramBlock = gmp.new ParamListBlock();
            paramBlock.setParameter(Helpers.StringToBytes(args[i]));
            gmp.ParamList[i - 1] = paramBlock;
            sb.append(" ");
            sb.append(args[i]);
        }

        Client.Network.sendPacket(gmp);

        return String.format("Sent generic message with method %s, params %s}", methodName, sb);
    }
}
