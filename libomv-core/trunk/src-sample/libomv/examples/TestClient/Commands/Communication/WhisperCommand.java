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
package libomv.examples.TestClient.Commands.Communication;

import libomv.AgentManager.ChatType;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class WhisperCommand extends Command
{
    private static final String usage = "Usage: whisper [<channel>] <whatever>";

    public WhisperCommand(TestClient testClient)
    {
        Name = "whisper";
        Description = "Whisper something. " + usage;
        Category = CommandCategory.Communication;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        int channel = 0;
        int startIndex = 0;

        if (args.length < 1)
        {
            return usage;
        }
        else if (args.length > 1)
        {
        	try
        	{
        		channel = Integer.valueOf(args[0]);
                startIndex = 1;
        	}
        	catch (NumberFormatException ex)
        	{
        	}
        }

        String message = Helpers.EmptyString;

        for (int i = startIndex; i < args.length; i++)
        {
            // Append a space before the next arg
            if (i > 0)
                message += " ";
            message += args[i];
        }

        Client.Self.Chat(message, channel, ChatType.Whisper);

        return "Whispered " + message.toString();
    }
}