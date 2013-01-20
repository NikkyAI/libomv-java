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
package libomv.examples.TestClient.Commands.Inventory;

import java.util.List;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryManager.InventorySortOrder;
import libomv.inventory.InventoryNode;
import libomv.types.UUID;

public class InventoryCommand extends Command
{
    public InventoryCommand(TestClient testClient)
    {
        Name = "inv";
        Description = "Prints out inventory. Usage: inv";
        Category = CommandCategory.Inventory;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        StringBuilder result = new StringBuilder();

        InventoryFolder rootFolder = Client.Inventory.getRootNode(false);
        PrintFolder(rootFolder, result, 0);

        return result.toString();
    }

    void PrintFolder(InventoryFolder f, StringBuilder result, int indent) throws Exception
    {
        List<InventoryNode> contents = Client.Inventory.FolderContents(f.itemID, Client.Self.getAgentID(), true, true, InventorySortOrder.ByName, 3000);
        if (contents != null)
        {
            for (InventoryNode node : contents)
            {
            	for (int i = 0; i < indent; i++)
            		result.append("  ");
                result.append(node.name + " (" + node.itemID + ")\n");
                if (node instanceof InventoryFolder)
                {
                    InventoryFolder folder = (InventoryFolder)node;
                    PrintFolder(folder, result, indent + 1);
                }
            }
        }
    }
}
