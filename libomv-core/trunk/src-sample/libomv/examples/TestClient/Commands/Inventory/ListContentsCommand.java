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
import libomv.types.Permissions.PermissionMask;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class ListContentsCommand extends Command
{
    private static final String usage = "Usage: ls [-l]";

    public ListContentsCommand(TestClient client)
    {
        Name = "ls";
        Description = "Lists the contents of the current working inventory folder. " + usage;
        Category = CommandCategory.Inventory;
    }

    private String getSubString(String string, int offset, int length)
    {
    	if (string.length() >= offset + length)
    		return string.substring(offset, length);
    	return string + Helpers.repeat(" ", length - string.length());
    }
    
	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length > 1)
            return usage;

        boolean longDisplay = false;
        if (args.length > 0 && args[0].equals("-l"))
            longDisplay = true;

        // WARNING: Uses local copy of inventory contents, need to download them first.
        List<InventoryNode> contents = Client.CurrentDirectory.getContents();
        if (contents == null)
            return "Empty directory";

        String displayString = Client.CurrentDirectory.name + " contains:" + Helpers.NewLine;
        displayString += "Attributes UUID                                 Type          Description" + Helpers.NewLine;
        displayString += "---------------------------------------------------------------------------------------------------" + Helpers.NewLine;
        // Pretty simple, just print out the contents.
        for (InventoryNode node : contents)
        {
            if (longDisplay)
            {
                // Generate a nicely formatted description of the item.
                // It kinda looks like the output of the unix ls.
                // starts with 'd' if the inventory is a folder, '-' if not.
                // 9 character permissions string
                // UUID of object
                // Name of object
                if (node instanceof InventoryFolder)
                {
                    InventoryFolder folder = (InventoryFolder)node;
                    displayString += "d--------- ";
                    displayString += folder.itemID;
                    displayString += " " + getSubString(folder.preferredType.toString(), 0, 13);
                    displayString += " " + folder.name;
                }
                else if (node instanceof InventoryItem)
                {
                    InventoryItem item = (InventoryItem)node;
                    displayString += "-";
                    displayString += PermMaskString(item.Permissions.OwnerMask);
                    displayString += PermMaskString(item.Permissions.GroupMask);
                    displayString += PermMaskString(item.Permissions.EveryoneMask);
                    displayString += " " + item.itemID;
                    displayString += " " + getSubString(item.assetType.toString(), 0, 13);
                    displayString += " " + item.name;
                    displayString += Helpers.NewLine;
                    displayString += "  AssetID: " + item.assetID;
                }
            }
            else
            {
                displayString += node.name;
            }
            displayString += Helpers.NewLine;
        }
        return displayString;
    }

    /**
     * Returns a 3-character summary of the PermissionMask
     * CMT if the mask allows copy, mod and transfer
     * -MT if it disallows copy
     * --T if it only allows transfer
     * --- if it disallows everything
     *
     * @param mask
     * @returns
     */
    private static String PermMaskString(int mask)
    {
        String str = "";
        if ((mask & PermissionMask.Copy) != 0)
            str += "C";
        else
            str += "-";

        if ((mask & PermissionMask.Modify) != 0)
            str += "M";
        else
            str += "-";

        if ((mask & PermissionMask.Transfer) != 0)
            str += "T";
        else
            str += "-";
        return str;
    }
}
