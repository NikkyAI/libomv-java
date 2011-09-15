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

public class ParcelSelectObjectsPacket extends Packet
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

    public class ReturnIDsBlock
    {
        public UUID ReturnID = null;

        public int getLength(){
            return 16;
        }

        public ReturnIDsBlock() { }
        public ReturnIDsBlock(ByteBuffer bytes)
        {
            ReturnID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ReturnID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- ReturnIDs --\n";
            try {
                output += "ReturnID: " + ReturnID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ReturnIDsBlock createReturnIDsBlock() {
         return new ReturnIDsBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.ParcelSelectObjects; }
    public AgentDataBlock AgentData;
    public ParcelDataBlock ParcelData;
    public ReturnIDsBlock[] ReturnIDs;

    public ParcelSelectObjectsPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)202);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        ParcelData = new ParcelDataBlock();
        ReturnIDs = new ReturnIDsBlock[0];
    }

    public ParcelSelectObjectsPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        ReturnIDs = new ReturnIDsBlock[count];
        for (int j = 0; j < count; j++)
        { ReturnIDs[j] = new ReturnIDsBlock(bytes); }
     }

    public ParcelSelectObjectsPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        ReturnIDs = new ReturnIDsBlock[count];
        for (int j = 0; j < count; j++)
        { ReturnIDs[j] = new ReturnIDsBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += ParcelData.getLength();
        length++;
        for (int j = 0; j < ReturnIDs.length; j++) { length += ReturnIDs[j].getLength(); }
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
        bytes.put((byte)ReturnIDs.length);
        for (int j = 0; j < ReturnIDs.length; j++) { ReturnIDs[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- ParcelSelectObjects ---\n";
        output += AgentData.toString() + "\n";
        output += ParcelData.toString() + "\n";
        for (int j = 0; j < ReturnIDs.length; j++)
        {
            output += ReturnIDs[j].toString() + "\n";
        }
        return output;
    }
}
