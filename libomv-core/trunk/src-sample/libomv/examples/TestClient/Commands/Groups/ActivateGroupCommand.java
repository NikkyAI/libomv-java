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

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 1)
            return Description;

        activeGroup = Helpers.EmptyString;

        String groupName = Helpers.EmptyString;
        for (int i = 0; i < args.length; i++)
            groupName += args[i] + " ";
        groupName = groupName.trim();

        UUID groupUUID = Client.groupName2UUID(groupName);
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
