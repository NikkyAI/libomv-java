/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
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
package libomv.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import libomv.utils.Helpers;
import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;
import libomv.types.OverflowException;

public class AvatarGroupsReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID AvatarID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            AvatarID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            AvatarID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "AvatarID: " + AvatarID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class GroupDataBlock
    {
        public long GroupPowers = 0;
        public boolean AcceptNotices = false;
        private byte[] _grouptitle;
        public byte[] getGroupTitle() {
            return _grouptitle;
        }

        public void setGroupTitle(byte[] value) throws Exception {
            if (value == null) {
                _grouptitle = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _grouptitle = new byte[value.length];
                System.arraycopy(value, 0, _grouptitle, 0, value.length);
            }
        }

        public UUID GroupID = null;
        private byte[] _groupname;
        public byte[] getGroupName() {
            return _groupname;
        }

        public void setGroupName(byte[] value) throws Exception {
            if (value == null) {
                _groupname = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _groupname = new byte[value.length];
                System.arraycopy(value, 0, _groupname, 0, value.length);
            }
        }

        public UUID GroupInsigniaID = null;

        public int getLength(){
            int length = 41;
            if (getGroupTitle() != null) { length += 1 + getGroupTitle().length; }
            if (getGroupName() != null) { length += 1 + getGroupName().length; }
            return length;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            int length;
            GroupPowers = bytes.getLong(); 
            AcceptNotices = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = (int)(bytes.get()) & 0xFF;
            _grouptitle = new byte[length];
            bytes.get(_grouptitle); 
            GroupID = new UUID(bytes);
            length = (int)(bytes.get()) & 0xFF;
            _groupname = new byte[length];
            bytes.get(_groupname); 
            GroupInsigniaID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(GroupPowers);
            bytes.put((byte)((AcceptNotices) ? 1 : 0));
            bytes.put((byte)_grouptitle.length);
            bytes.put(_grouptitle);
            GroupID.GetBytes(bytes);
            bytes.put((byte)_groupname.length);
            bytes.put(_groupname);
            GroupInsigniaID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += "GroupPowers: " + Long.toString(GroupPowers) + "\n";
                output += "AcceptNotices: " + Boolean.toString(AcceptNotices) + "\n";
                output += Helpers.FieldToString(_grouptitle, "GroupTitle") + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += Helpers.FieldToString(_groupname, "GroupName") + "\n";
                output += "GroupInsigniaID: " + GroupInsigniaID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public GroupDataBlock createGroupDataBlock() {
         return new GroupDataBlock();
    }

    public class NewGroupDataBlock
    {
        public boolean ListInProfile = false;

        public int getLength(){
            return 1;
        }

        public NewGroupDataBlock() { }
        public NewGroupDataBlock(ByteBuffer bytes)
        {
            ListInProfile = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((ListInProfile) ? 1 : 0));
        }

        public String toString()
        {
            String output = "-- NewGroupData --\n";
            try {
                output += "ListInProfile: " + Boolean.toString(ListInProfile) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NewGroupDataBlock createNewGroupDataBlock() {
         return new NewGroupDataBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.AvatarGroupsReply; }
    public AgentDataBlock AgentData;
    public GroupDataBlock[] GroupData;
    public NewGroupDataBlock NewGroupData;

    public AvatarGroupsReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)173);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        GroupData = new GroupDataBlock[0];
        NewGroupData = new NewGroupDataBlock();
    }

    public AvatarGroupsReplyPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        { GroupData[j] = new GroupDataBlock(bytes); }
        NewGroupData = new NewGroupDataBlock(bytes);
     }

    public AvatarGroupsReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        { GroupData[j] = new GroupDataBlock(bytes); }
        NewGroupData = new NewGroupDataBlock(bytes);
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += NewGroupData.getLength();
        length++;
        for (int j = 0; j < GroupData.length; j++) { length += GroupData[j].getLength(); }
        if (header.AckList.length > 0) {
            length += header.AckList.length * 4 + 1;
        }
        return length;
    }

    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)GroupData.length);
        for (int j = 0; j < GroupData.length; j++) { GroupData[j].ToBytes(bytes); }
        NewGroupData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- AvatarGroupsReply ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < GroupData.length; j++)
        {
            output += GroupData[j].toString() + "\n";
        }
        output += NewGroupData.toString() + "\n";
        return output;
    }
}
