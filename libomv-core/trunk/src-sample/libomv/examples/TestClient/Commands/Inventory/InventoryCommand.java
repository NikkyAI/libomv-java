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
        Description = "Prints out inventory.";
        Category = CommandCategory.Inventory;
    }

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
