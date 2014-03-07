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
package libomv.examples.TestClient.Commands.Inventory;

import java.util.ArrayList;

import libomv.assets.AssetItem.AssetType;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.inventory.InventoryNode;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class DeleteCommand extends Command
{
    private static final String usage = "Usage: del <itemname-or-path>|<foldername-or-path>";

    public DeleteCommand(TestClient testClient)
    {
        Name = "del";
        Description = "Moves a folder or item to the Trash Folder. " + usage;
        Category = CommandCategory.Inventory;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
    	if (args.length == 0)
    		return usage;
    	
        // parse the command line
        String target = Helpers.EmptyString;
        for (int ct = 0; ct < args.length; ct++)
            target = target + args[ct] + " ";
        target = target.trim();

        /* When it is an absolute path we start at the root node, otherwise from the current directory */
        UUID start = target.startsWith("/") ? Client.Inventory.getRootNode(false).itemID : Client.CurrentDirectory.itemID;
        
        // initialize results list
        ArrayList<InventoryNode> found = new ArrayList<InventoryNode>();

        // find the item or folder
        found = Client.Inventory.LocalFind(start, target.split("/"), 0, true);
        if (found.size() == 1)
        {
        	InventoryNode node = found.get(0);
        	if (node.getType() == InventoryType.Folder)
        	{
                // move the folder to the trash folder
                Client.Inventory.MoveFolder(node.itemID, Client.Inventory.FindFolderForType(AssetType.TrashFolder).itemID);  
                return String.format("Moved folder %s to Trash", node.name);
        	}
        	else
        	{
        		// move the item to the trash folder
        		Client.Inventory.MoveItem(node.itemID, Client.Inventory.FindFolderForType(AssetType.TrashFolder).itemID);
                return String.format("Moved item %s to Trash", node.name);
        	}
        }
        return String.format("Found %d items", found.size());
    }
}
