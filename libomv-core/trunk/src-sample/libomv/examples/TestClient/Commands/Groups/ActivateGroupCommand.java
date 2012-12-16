package libomv.examples.TestClient.Commands.Groups;

import libomv.Simulator;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.packets.AgentDataUpdatePacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

/// Changes Avatars currently active group
public class ActivateGroupCommand extends Command implements PacketCallback
{
    private TimeoutEvent<Boolean> GroupsEvent = new TimeoutEvent<Boolean>();
    String activeGroup;

    public ActivateGroupCommand(TestClient testClient)
    {
        Name = "activategroup";
        Description = "Set a group as active. Usage: activategroup GroupName";
        Category = CommandCategory.Groups;
    }

    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 1)
            return Description;

        activeGroup = Helpers.EmptyString;

        String groupName = Helpers.EmptyString;
        for (int i = 0; i < args.length; i++)
            groupName += args[i] + " ";
        groupName = groupName.trim();

        UUID groupUUID = Client.GroupName2UUID(groupName);
        if (!groupUUID.equals(UUID.Zero))
        {
            Client.Network.RegisterCallback(PacketType.AgentDataUpdate, this);

            System.out.println("setting " + groupName + " as active group");
            Client.Groups.ActivateGroup(groupUUID);
            GroupsEvent.waitOne(30000);

            Client.Network.UnregisterCallback(PacketType.AgentDataUpdate, this);
            GroupsEvent.reset();

            /* A.Biondi 
             * TODO: Handle titles choosing.
             */

            if (activeGroup == null || activeGroup.isEmpty())
                return Client.toString() + " failed to activate the group " + groupName;

            return "Active group is now " + activeGroup;
        }
        return Client.toString() + " doesn't seem to be member of the group " + groupName;
    }

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
        AgentDataUpdatePacket p = (AgentDataUpdatePacket)packet;
        if (p.AgentData.AgentID.equals(Client.Self.getAgentID()))
        {
            activeGroup = Helpers.BytesToString(p.AgentData.getGroupName()) + " ( " + Helpers.BytesToString(p.AgentData.getGroupTitle()) + " )";
            GroupsEvent.set(true);
        }
	}
}
