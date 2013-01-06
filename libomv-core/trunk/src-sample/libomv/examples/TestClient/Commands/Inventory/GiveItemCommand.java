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
import libomv.inventory.InventoryException;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class GiveItemCommand extends Command
{
    public GiveItemCommand(TestClient client)
    {
        Name = "give";
        Description = "Gives items from the current working directory to an avatar.";
        Category = CommandCategory.Inventory;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 2)
        {
            return "Usage: give <agent uuid> itemname";
        }
        UUID dest = UUID.parse(args[0]);
        if (dest == null)
        {
            return "First argument expected agent UUID.";
        }

        String ret = "";
        String nl = "\n";

        String target = Helpers.EmptyString;
        for (int ct = 0; ct < args.length; ct++)
            target = target + args[ct] + " ";
        target = target.trim();

        // WARNING: Uses local copy of inventory contents, need to download them first.
        List<InventoryNode> contents = Client.CurrentDirectory.getContents();
        boolean found = false;
        for (InventoryNode node : contents)
        {
            if (target.equals(node.name) || node.itemID.equals(UUID.parse(target)))
            {
                found = true;
                if (node instanceof InventoryItem)
                {
                    InventoryItem item = (InventoryItem)node;
                    Client.Inventory.GiveItem(item.itemID, item.name, item.assetType, dest, true);
                    ret += "Gave " + item.name + " (" + item.assetType + ")" + nl;
                }
                else
                {
                    ret += "Unable to give folder " + node.name + nl;
                }
            }
        }
        if (!found)
            ret += "No inventory item named " + target + " found." + nl;

        return ret;
    }

}
