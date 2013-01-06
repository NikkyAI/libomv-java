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

import java.util.ArrayList;

import libomv.assets.AssetItem.AssetType;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.inventory.InventoryNode;
import libomv.types.UUID;
import libomv.utils.Helpers;

// Inventory Example, Moves a folder to the Trash folder
public class DeleteFolderCommand extends Command
{
    public DeleteFolderCommand(TestClient testClient)
    {
        Name = "deleteFolder";
        Description = "Moves a folder to the Trash Folder";
        Category = CommandCategory.Inventory;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        // parse the command line
        String target = Helpers.EmptyString;
        for (int ct = 0; ct < args.length; ct++)
            target = target + args[ct] + " ";
        target = target.trim();

        // initialize results list
        ArrayList<InventoryNode> found = new ArrayList<InventoryNode>();

        // find the folder
        found = Client.Inventory.LocalFind(Client.Inventory.getRootNode(false).itemID, target.split("/"), 0, true);
        
        if (found.size() == 1)
        {
            // move the folder to the trash folder
            Client.Inventory.MoveFolder(found.get(0).itemID, Client.Inventory.FindFolderForType(AssetType.TrashFolder).itemID);
            
            return String.format("Moved folder %s to Trash", found.get(0).name);
        }
        return Helpers.EmptyString;
    }
}
