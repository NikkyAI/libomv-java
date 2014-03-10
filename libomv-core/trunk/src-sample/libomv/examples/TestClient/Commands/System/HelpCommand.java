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
		Description = "Lists available commands. Usage: help <command> to display information on a command";
        Category = CommandCategory.TestClient;
	}

	@Override
    public String execute(String[] args, UUID fromAgentID)
	{
        if (args.length > 0)
        {
            if (Client.Commands.containsKey(args[0]))
            {
                return Client.Commands.get(args[0]).Description;
            }
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
        result.append("\nhelp [command] for usage/information");
        
        return result.toString();
	}
}
