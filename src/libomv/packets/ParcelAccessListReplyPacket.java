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

import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;

public class ParcelAccessListReplyPacket extends Packet
{
    public class DataBlock
    {
        public UUID AgentID = null;
        public int SequenceID = 0;
        public int Flags = 0;
        public int LocalID = 0;

        public int getLength(){
            return 28;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SequenceID = bytes.getInt(); 
            Flags = bytes.getInt(); 
            LocalID = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.putInt(SequenceID);
            bytes.putInt(Flags);
            bytes.putInt(LocalID);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SequenceID: " + Integer.toString(SequenceID) + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output += "LocalID: " + Integer.toString(LocalID) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    public class ListBlock
    {
        public UUID ID = null;
        public int Time = 0;
        public int Flags = 0;

        public int getLength(){
            return 24;
        }

        public ListBlock() { }
        public ListBlock(ByteBuffer bytes)
        {
            ID = new UUID(bytes);
            Time = bytes.getInt(); 
            Flags = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
            bytes.putInt(Time);
            bytes.putInt(Flags);
        }

        @Override
        public String toString()
        {
            String output = "-- List --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output += "Time: " + Integer.toString(Time) + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ListBlock createListBlock() {
         return new ListBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ParcelAccessListReply; }
    public DataBlock Data;
    public ListBlock[] List;

    public ParcelAccessListReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)216);
        header.setReliable(true);
        Data = new DataBlock();
        List = new ListBlock[0];
    }

    public ParcelAccessListReplyPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        Data = new DataBlock(bytes);
        int count = bytes.get() & 0xFF;
        List = new ListBlock[count];
        for (int j = 0; j < count; j++)
        { List[j] = new ListBlock(bytes); }
     }

    public ParcelAccessListReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
        int count = bytes.get() & 0xFF;
        List = new ListBlock[count];
        for (int j = 0; j < count; j++)
        { List[j] = new ListBlock(bytes); }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        length++;
        for (int j = 0; j < List.length; j++) { length += List[j].getLength(); }
        if (header.AckList.length > 0) {
            length += header.AckList.length * 4 + 1;
        }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        bytes.put((byte)List.length);
        for (int j = 0; j < List.length; j++) { List[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelAccessListReply ---\n";
        output += Data.toString() + "\n";
        for (int j = 0; j < List.length; j++)
        {
            output += List[j].toString() + "\n";
        }
        return output;
    }
}
