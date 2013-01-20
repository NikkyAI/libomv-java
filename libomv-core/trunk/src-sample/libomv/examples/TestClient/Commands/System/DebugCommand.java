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
package libomv.examples.TestClient.Commands.System;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class DebugCommand extends Command
{
    private static final String usage = "Usage: debug None|Debug|Error|Info|Warn";

    public DebugCommand(TestClient testClient)
    {
        Name = "debug";
        Description = "Turn debug messages on or off. " + usage;
        Category = CommandCategory.TestClient;
    }

    @Override
	public String execute(String[] args, UUID fromAgentID)
    {
        if (args.length != 1)
            return usage;

        if (args[0].equalsIgnoreCase("debug"))
        {
        	Logger.LOG_LEVEL = LogLevel.Debug;
            return "Logging is set to Debug";
        }
        else if (args[0].equalsIgnoreCase("none"))
        {
            Logger.LOG_LEVEL = LogLevel.None;
            return "Logging is set to None";
        }
        else if (args[0].equalsIgnoreCase("warn"))
        {
        	Logger.LOG_LEVEL = LogLevel.Warning;
            return "Logging is set to level Warning";
        }
        else if (args[0].equalsIgnoreCase("info"))
        {
        	Logger.LOG_LEVEL = LogLevel.Info;
            return "Logging is set to level Info";
        }
        else if (args[0].equalsIgnoreCase("error"))
        {
        	Logger.LOG_LEVEL = LogLevel.Error;
            return "Logging is set to level Error";
        }
        else
        {
            return usage;
        }
    }
}
