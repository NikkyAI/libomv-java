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
package libomv.examples.TestClient.Commands.Directory;

import java.util.ArrayList;

import libomv.DirectoryManager.AgentSearchData;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class SearchPeopleCommand extends Command
{
    private static final String usage = "Usage: searchpeople <search text>";

    public SearchPeopleCommand(TestClient testClient)
    {
        Name = "searchpeople";
        Description = "Searches for other avatars. " + usage;
        Category = CommandCategory.Search;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        // process command line arguments
        if (args.length < 1)
            return usage;

        String searchText = Helpers.EmptyString;
        for (int i = 0; i < args.length; i++)
            searchText += args[i] + " ";
        searchText = searchText.trim();

        // send the request to the directory manager
        ArrayList<AgentSearchData> agents = Client.findFromAgentName(searchText, 20000);
        if (agents == null)
        {
            return "Timeout waiting for simulator to respond.";
        }
        else if (agents.size() == 0)
        {
        	return "Didn't find any people that matched your query :(";
        }

        StringBuilder result = new StringBuilder();
        result.append("Your query '" + searchText + "' matched " + agents.size() + " people.\n");
        for (AgentSearchData agent : agents)
        {
        	result.append(agent.FirstName + " " + agent.LastName + " (" + agent.AgentID + ")\n");                   
        }
        return result.substring(0, result.length() - 1);
    }
}
