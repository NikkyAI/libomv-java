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

import java.util.List;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryNode;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class ChangeDirectoryCommand extends Command
{
    private static final String usage = "Usage: cd <path-to-folder>";

    public ChangeDirectoryCommand(TestClient client)
    {
        Name = "cd";
        Description = "Changes the current working inventory folder. " + usage;
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
        InventoryFolder currentFolder = target.startsWith("/") ? Client.Inventory.getRootNode(false) : Client.CurrentDirectory;
        if (currentFolder == null) // We need this to be set to something. 
            return "Error: Client not logged in.";

        // Use '/' as a path separator.
        String[] path = target.split("/");

        // Traverse the path, looking for the 
        for (int i = 0; i < path.length; ++i)
        {
            String nextName = path[i];
            if (nextName == null || nextName.isEmpty() || nextName.equals("."))
                continue; // Ignore '.' and blanks, stay in the current directory.
 
            if (nextName.equals("..") && !currentFolder.equals(Client.Inventory.getRootNode(false)))
            {
                // If we encounter .., move to the parent folder.
                currentFolder = currentFolder.getParentFolder();
            }
            else
            {
                List<InventoryNode> currentContents = currentFolder.getContents();
                // Try and find an InventoryBase with the corresponding name.
                boolean found = false;
                for (InventoryNode item : currentContents)
                {
                    // Allow lookup by UUID as well as name:
                    if (item.name.equals(nextName) || item.itemID.equals(new UUID(nextName)))
                    {
                        found = true;
                        if (item instanceof InventoryFolder)
                        {
                            currentFolder = (InventoryFolder)item;
                        }
                        else
                        {
                            return item.name + " is not a folder.";
                        }
                    }
                }
                if (!found)
                    return nextName + " not found in " + currentFolder.name;
            }
        }
        Client.CurrentDirectory = currentFolder;
        return "Current folder: " + currentFolder.name;
    }
}
