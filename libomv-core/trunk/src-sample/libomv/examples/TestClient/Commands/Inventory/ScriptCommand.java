/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
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

import java.io.FileReader;

import org.apache.commons.io.LineIterator;

import libomv.examples.TestClient.ClientManager;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class ScriptCommand extends Command
{
    private static final String usage = "Usage: script <filename>";

    public ScriptCommand(TestClient testClient)
    {
        Name = "script";
        Description = "Reads TestClient commands from a file. One command per line, arguments separated by spaces. " + usage;
        Category = CommandCategory.TestClient;
    }

    @Override
	public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length != 1)
            return usage;

        String filename = Helpers.EmptyString;
		for (int ct = 0; ct < args.length;ct++)
			filename += args[ct] + " ";
		filename = filename.trim();

        if (filename.length() == 0)
            return usage;

        int lines = 0;

        // Load the file
    	LineIterator reader = new LineIterator(new FileReader(filename));

    	// Execute all of the commands
        while (reader.hasNext())
        {
            String line = reader.nextLine().trim();

            if (line.length() > 0)
            {
                ClientManager.getInstance().doCommandAll(line, UUID.Zero);
                lines++;
            }
        }
        return "Finished executing " + lines + " commands";
    }

}
