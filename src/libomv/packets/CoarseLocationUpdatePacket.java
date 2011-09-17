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

public class CoarseLocationUpdatePacket extends Packet
{
    public class LocationBlock
    {
        public byte X = 0;
        public byte Y = 0;
        public byte Z = 0;

        public int getLength(){
            return 3;
        }

        public LocationBlock() { }
        public LocationBlock(ByteBuffer bytes)
        {
            X = bytes.get(); 
            Y = bytes.get(); 
            Z = bytes.get(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(X);
            bytes.put(Y);
            bytes.put(Z);
        }

        public String toString()
        {
            String output = "-- Location --\n";
            try {
                output += "X: " + Byte.toString(X) + "\n";
                output += "Y: " + Byte.toString(Y) + "\n";
                output += "Z: " + Byte.toString(Z) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public LocationBlock createLocationBlock() {
         return new LocationBlock();
    }

    public class IndexBlock
    {
        public short You = 0;
        public short Prey = 0;

        public int getLength(){
            return 4;
        }

        public IndexBlock() { }
        public IndexBlock(ByteBuffer bytes)
        {
            You = bytes.getShort(); 
            Prey = bytes.getShort(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort(You);
            bytes.putShort(Prey);
        }

        public String toString()
        {
            String output = "-- Index --\n";
            try {
                output += "You: " + Short.toString(You) + "\n";
                output += "Prey: " + Short.toString(Prey) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public IndexBlock createIndexBlock() {
         return new IndexBlock();
    }

    public class AgentDataBlock
    {
        public UUID AgentID = null;

        public int getLength(){
            return 16;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.CoarseLocationUpdate; }
    public LocationBlock[] Location;
    public IndexBlock Index;
    public AgentDataBlock[] AgentData;

    public CoarseLocationUpdatePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)6);
        header.setReliable(true);
        Location = new LocationBlock[0];
        Index = new IndexBlock();
        AgentData = new AgentDataBlock[0];
    }

    public CoarseLocationUpdatePacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        int count = (int)bytes.get() & 0xFF;
        Location = new LocationBlock[count];
        for (int j = 0; j < count; j++)
        { Location[j] = new LocationBlock(bytes); }
        Index = new IndexBlock(bytes);
        count = (int)bytes.get() & 0xFF;
        AgentData = new AgentDataBlock[count];
        for (int j = 0; j < count; j++)
        { AgentData[j] = new AgentDataBlock(bytes); }
     }

    public CoarseLocationUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = (int)bytes.get() & 0xFF;
        Location = new LocationBlock[count];
        for (int j = 0; j < count; j++)
        { Location[j] = new LocationBlock(bytes); }
        Index = new IndexBlock(bytes);
        count = (int)bytes.get() & 0xFF;
        AgentData = new AgentDataBlock[count];
        for (int j = 0; j < count; j++)
        { AgentData[j] = new AgentDataBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += Index.getLength();
        length++;
        for (int j = 0; j < Location.length; j++) { length += Location[j].getLength(); }
        length++;
        for (int j = 0; j < AgentData.length; j++) { length += AgentData[j].getLength(); }
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
        bytes.put((byte)Location.length);
        for (int j = 0; j < Location.length; j++) { Location[j].ToBytes(bytes); }
        Index.ToBytes(bytes);
        bytes.put((byte)AgentData.length);
        for (int j = 0; j < AgentData.length; j++) { AgentData[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- CoarseLocationUpdate ---\n";
        for (int j = 0; j < Location.length; j++)
        {
            output += Location[j].toString() + "\n";
        }
        output += Index.toString() + "\n";
        for (int j = 0; j < AgentData.length; j++)
        {
            output += AgentData[j].toString() + "\n";
        }
        return output;
    }
}
