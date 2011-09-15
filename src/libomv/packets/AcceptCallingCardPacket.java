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

public class AcceptCallingCardPacket extends Packet
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

    public class TransactionBlockBlock
    {
        public UUID TransactionID = null;

        public int getLength(){
            return 16;
        }

        public TransactionBlockBlock() { }
        public TransactionBlockBlock(ByteBuffer bytes)
        {
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransactionID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- TransactionBlock --\n";
            try {
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransactionBlockBlock createTransactionBlockBlock() {
         return new TransactionBlockBlock();
    }

    public class FolderDataBlock
    {
        public UUID FolderID = null;

        public int getLength(){
            return 16;
        }

        public FolderDataBlock() { }
        public FolderDataBlock(ByteBuffer bytes)
        {
            FolderID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            FolderID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- FolderData --\n";
            try {
                output += "FolderID: " + FolderID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public FolderDataBlock createFolderDataBlock() {
         return new FolderDataBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.AcceptCallingCard; }
    public AgentDataBlock AgentData;
    public TransactionBlockBlock TransactionBlock;
    public FolderDataBlock[] FolderData;

    public AcceptCallingCardPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)302);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        TransactionBlock = new TransactionBlockBlock();
        FolderData = new FolderDataBlock[0];
    }

    public AcceptCallingCardPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        TransactionBlock = new TransactionBlockBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        FolderData = new FolderDataBlock[count];
        for (int j = 0; j < count; j++)
        { FolderData[j] = new FolderDataBlock(bytes); }
     }

    public AcceptCallingCardPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        TransactionBlock = new TransactionBlockBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        FolderData = new FolderDataBlock[count];
        for (int j = 0; j < count; j++)
        { FolderData[j] = new FolderDataBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += TransactionBlock.getLength();
        length++;
        for (int j = 0; j < FolderData.length; j++) { length += FolderData[j].getLength(); }
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
        TransactionBlock.ToBytes(bytes);
        bytes.put((byte)FolderData.length);
        for (int j = 0; j < FolderData.length; j++) { FolderData[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- AcceptCallingCard ---\n";
        output += AgentData.toString() + "\n";
        output += TransactionBlock.toString() + "\n";
        for (int j = 0; j < FolderData.length; j++)
        {
            output += FolderData[j].toString() + "\n";
        }
        return output;
    }
}
