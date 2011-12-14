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
package libomv.examples.TestClient.Commands.Agents;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Primitive;
import libomv.types.UUID;

public class TouchCommand extends Command
{
    public TouchCommand(TestClient testClient)
	{
		Name = "touch";
		Description = "Attempt to touch a prim with specified UUID";
        Category = CommandCategory.Objects;
	}
	
    @Override
	public String Execute(String[] args, UUID fromAgentID)
	{
        if (args.length != 1)
            return "Usage: touch UUID";
        UUID target = UUID.Parse(args[0]);
        
        if (target != null)
        {
        	synchronized (Client.Network.getCurrentSim())
        	{
    			for (Primitive prim : Client.Network.getCurrentSim().getObjectsPrimitives().values())
    			{
    				if (prim.ID.equals(target))
    				{
    	                try
						{
							Client.Self.Touch(prim.LocalID);
	    	                return "Touched prim " + prim.LocalID;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
    				}
				}
			}
        }
        return "Couldn't find a prim to touch with UUID " + args[0];
	}
}