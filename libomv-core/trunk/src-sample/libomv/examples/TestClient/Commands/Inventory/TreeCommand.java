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
package libomv.examples.TestClient.Commands.Inventory;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Primitive.Tree;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;

public class TreeCommand extends Command
{
    public TreeCommand(TestClient testClient)
	{
		Name = "tree";
		Description = "Rez a tree. Usage: tree [treename]";
        Category = CommandCategory.Objects;
	}

    @Override
	public String execute(String[] args, UUID fromAgentID)
	{
	    if (args.length == 1)
	    {
	        try
	        {
	            String treeName = args[0].trim();
	            Tree tree = Tree.valueOf(treeName);

	            Vector3 treePosition = Client.Self.getAgentPosition();
	            treePosition.Z += 3.0f;

	            Client.Objects.AddTree(Client.Network.getCurrentSim(), new Vector3(0.5f, 0.5f, 0.5f),
	                Quaternion.Identity, treePosition, tree, Client.GroupID, false);

	            return "Attempted to rez a " + treeName + " tree";
	        }
	        catch (Exception ex)
	        { }
	    }

	    StringBuilder usage = new StringBuilder("Usage: tree [");
	    for (Tree value : Tree.values())
	    {
	        usage.append(value.name() + ", ");
	    }
	    return usage.replace(usage.length() - 2, usage.length(), "]").toString();
	}
}