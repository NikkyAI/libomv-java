package libomv.examples.TestClient.Commands.Inventory;

import java.io.FileReader;

import org.apache.commons.io.LineIterator;

import libomv.examples.TestClient.ClientManager;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class ScriptCommand extends Command
{
    public ScriptCommand(TestClient testClient)
    {
        Name = "script";
        Description = "Reads TestClient commands from a file. One command per line, arguments separated by spaces. Usage: script [filename]";
        Category = CommandCategory.TestClient;
    }

    @Override
	public String Execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length != 1)
            return "Usage: script [filename]";

        int lines = 0;

        // Load the file
    	LineIterator reader = new LineIterator(new FileReader(args[0]));

    	// Execute all of the commands
        while (reader.hasNext())
        {
            String line = reader.nextLine().trim();

            if (line.length() > 0)
            {
                ClientManager.getInstance().DoCommandAll(line, UUID.Zero);
                lines++;
            }
        }
        return "Finished executing " + lines + " commands";
    }

}
