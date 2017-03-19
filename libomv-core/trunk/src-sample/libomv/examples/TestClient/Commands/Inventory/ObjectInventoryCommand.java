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
package libomv.examples.TestClient.Commands.Inventory;

import java.util.List;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode;
import libomv.primitives.Primitive;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class ObjectInventoryCommand extends Command
{
    private static final String usage = "Usage: objectinventory <object uuid>";

    public ObjectInventoryCommand(TestClient testClient)
    {
        Name = "objectinventory";
        Description = "Retrieves a listing of items inside an object (task inventory). " + usage;
        Category = CommandCategory.Inventory;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length != 1)
            return usage;

        int objectLocalID;
        UUID objectID = UUID.parse(args[0]);
        if (objectID == null)
            return usage;

        Primitive found = Client.Network.getCurrentSim().findPrimitive(objectID, false);
        if (found != null)
            objectLocalID = found.LocalID;
        else
            return "Couldn't find prim " + objectID;

        List<InventoryNode> items = Client.Inventory.GetTaskInventory(objectID, objectLocalID, 1000 * 30);

        if (items != null)
        {
            StringBuilder result = new StringBuilder();

            for (InventoryNode node : items)
            {
                if (node instanceof InventoryFolder)
                {
                    result.append("[Folder] Name: " + node.name + "\n");
                }
                else
                {
                    InventoryItem item = (InventoryItem)node;
                    result.append("[Item] Name: " + item.name + " Desc: " + item.Description + " Type: " + item.assetType + "\n");
                }
            }
            return result.toString();
        }
        return "Failed to download task inventory for " + Helpers.LocalIDToString(objectLocalID);
    }
}
