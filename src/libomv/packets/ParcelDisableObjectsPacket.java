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

import libomv.types.PacketFrequency;
import libomv.types.PacketHeader;
import libomv.types.UUID;

public class ParcelDisableObjectsPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class ParcelDataBlock
    {
        public int LocalID = 0;
        public int ReturnType = 0;

        public int getLength(){
            return 8;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            LocalID = bytes.getInt(); 
            ReturnType = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(LocalID);
            bytes.putInt(ReturnType);
        }

        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "LocalID: " + Integer.toString(LocalID) + "\n";
                output += "ReturnType: " + Integer.toString(ReturnType) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ParcelDataBlock createParcelDataBlock() {
         return new ParcelDataBlock();
    }

    public class TaskIDsBlock
    {
        public UUID TaskID = null;

        public int getLength(){
            return 16;
        }

        public TaskIDsBlock() { }
        public TaskIDsBlock(ByteBuffer bytes)
        {
            TaskID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TaskID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- TaskIDs --\n";
            try {
                output += "TaskID: " + TaskID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TaskIDsBlock createTaskIDsBlock() {
         return new TaskIDsBlock();
    }

    public class OwnerIDsBlock
    {
        public UUID OwnerID = null;

        public int getLength(){
            return 16;
        }

        public OwnerIDsBlock() { }
        public OwnerIDsBlock(ByteBuffer bytes)
        {
            OwnerID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            OwnerID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- OwnerIDs --\n";
            try {
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public OwnerIDsBlock createOwnerIDsBlock() {
         return new OwnerIDsBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.ParcelDisableObjects; }
    public AgentDataBlock AgentData;
    public ParcelDataBlock ParcelData;
    public TaskIDsBlock[] TaskIDs;
    public OwnerIDsBlock[] OwnerIDs;

    public ParcelDisableObjectsPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)201);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        ParcelData = new ParcelDataBlock();
        TaskIDs = new TaskIDsBlock[0];
        OwnerIDs = new OwnerIDsBlock[0];
    }

    public ParcelDisableObjectsPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        TaskIDs = new TaskIDsBlock[count];
        for (int j = 0; j < count; j++)
        { TaskIDs[j] = new TaskIDsBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        OwnerIDs = new OwnerIDsBlock[count];
        for (int j = 0; j < count; j++)
        { OwnerIDs[j] = new OwnerIDsBlock(bytes); }
     }

    public ParcelDisableObjectsPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        TaskIDs = new TaskIDsBlock[count];
        for (int j = 0; j < count; j++)
        { TaskIDs[j] = new TaskIDsBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        OwnerIDs = new OwnerIDsBlock[count];
        for (int j = 0; j < count; j++)
        { OwnerIDs[j] = new OwnerIDsBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += ParcelData.getLength();
        length++;
        for (int j = 0; j < TaskIDs.length; j++) { length += TaskIDs[j].getLength(); }
        length++;
        for (int j = 0; j < OwnerIDs.length; j++) { length += OwnerIDs[j].getLength(); }
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
        ParcelData.ToBytes(bytes);
        bytes.put((byte)TaskIDs.length);
        for (int j = 0; j < TaskIDs.length; j++) { TaskIDs[j].ToBytes(bytes); }
        bytes.put((byte)OwnerIDs.length);
        for (int j = 0; j < OwnerIDs.length; j++) { OwnerIDs[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- ParcelDisableObjects ---\n";
        output += AgentData.toString() + "\n";
        output += ParcelData.toString() + "\n";
        for (int j = 0; j < TaskIDs.length; j++)
        {
            output += TaskIDs[j].toString() + "\n";
        }
        for (int j = 0; j < OwnerIDs.length; j++)
        {
            output += OwnerIDs[j].toString() + "\n";
        }
        return output;
    }
}
