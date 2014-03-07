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

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class SettingsCommand extends Command
{
    private static final String usage = "Usage: settings ?|<name> <value>";

    public SettingsCommand(TestClient testClient)
    {
        Name = "settings";
        Description = "List or change a setting. " + usage;
        Category = CommandCategory.TestClient;
    }

    @Override
	public String execute(String[] args, UUID fromAgentID)
    {
        if (args.length < 1)
            return usage;

        if (args[0].equalsIgnoreCase("?"))
        {
        	StringBuilder sb = new StringBuilder("Currently known settings: ");
        	for (String key : Client.Settings.keys())
        	{
        		sb.append(key + ", ");
        	}
            return sb.substring(0, sb.length() - 2);
        }
        else if (args.length == 1)
        {
        	return Client.Settings.get(args[0]).AsString();
        }
        else if (args.length == 2)
        {
        	String oldVal = Client.Settings.put(args[0], args[1]);
        	return "Value for setting \"" + args[0] + "\" set to \"" + args[1] + "\", was \"" + oldVal + "\"";
        }
        return usage;
    }
}
