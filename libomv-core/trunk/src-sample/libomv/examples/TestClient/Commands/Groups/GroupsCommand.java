package libomv.examples.TestClient.Commands.Groups;

import libomv.GroupManager.Group;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class GroupsCommand extends Command
{        
    public GroupsCommand(TestClient testClient)
    {
        Name = "groups";
        Description = "List avatar groups. Usage: groups";
        Category = CommandCategory.Groups;
    }

    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        Client.ReloadGroupsCache();
        return getGroupsString();
    }

    String getGroupsString()
    {
        if (null == Client.GroupsCache)
                return "Groups cache failed.";
        if (0 == Client.GroupsCache.size())
                return "No groups";
        StringBuilder sb = new StringBuilder();
        sb.append("got "+ Client.GroupsCache.size() +" groups:\n");
        for (Group group : Client.GroupsCache.values())
        {
            sb.append(group.getID() + ", " + group.getName() + "\n");           
        } 
        return sb.toString();
    }
}
