package libomv.examples.TestClient.Commands.Inventory;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.inventory.InventoryException;
import libomv.types.UUID;

/**
 * TestClient command to empty the trash
 */
public class EmptyTrashCommand extends Command
{
    public EmptyTrashCommand(TestClient testClient)
    {
        Name = "emptytrash";
        Description = "Empty inventory Trash folder";
        Category = CommandCategory.Inventory;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws InventoryException, Exception
    {
        Client.Inventory.EmptyTrash();
        return "Trash Emptied";
    }
}
