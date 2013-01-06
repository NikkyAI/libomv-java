package libomv.examples.TestClient.Commands.System;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class HelpCommand extends Command
{
    public HelpCommand(TestClient testClient)
	{
		Name = "help";
		Description = "Lists available commands. usage: help [command] to display information on commands";
        Category = CommandCategory.TestClient;
	}

	@Override
    public String execute(String[] args, UUID fromAgentID)
	{
        if (args.length > 0)
        {
            if (Client.Commands.containsKey(args[0]))
                return Client.Commands.get(args[0]).Description;
            else
                return "Command " + args[0] + " Does not exist. \"help\" to display all available commands.";
        }
		StringBuilder result = new StringBuilder();
        TreeMap<CommandCategory, ArrayList<Command>> CommandTree = new TreeMap<CommandCategory, ArrayList<Command>>();

        CommandCategory cc;
		for (Command c : Client.Commands.values())
		{
            if (c.Category.equals(null))
                cc = CommandCategory.Unknown;
            else
                cc = c.Category;

            if (CommandTree.containsKey(cc))
                CommandTree.get(cc).add(c);
            else
            {
                ArrayList<Command> l = new ArrayList<Command>();
                l.add(c);
                CommandTree.put(cc, l);
            }
		}
		
		Iterator<Entry<CommandCategory, ArrayList<Command>>> iter = CommandTree.entrySet().iterator();

        while (iter.hasNext())
        {
        	Entry<CommandCategory, ArrayList<Command>> kvp = iter.next();
            result.append("\n* {" +  kvp.getKey().toString() + "} Related Commands:\n");
            int colMax = 0;
            for (int i = 0; i < kvp.getValue().size(); i++)
            {
                if (colMax >= 8)
                {
                    result.append("\n");
                    colMax = 0;
                }
                result.append(" {" + kvp.getValue().get(i).Name + "}");
                colMax++;
            }
            result.append("\n");
        }
        result.append("\nHelp [command] for usage/information");
        
        return result.toString();
	}
}
