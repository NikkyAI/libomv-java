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

public class SimCrashedPacket extends Packet
{
    public class DataBlock
    {
        public int RegionX = 0;
        public int RegionY = 0;

        public int getLength(){
            return 8;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            RegionX = bytes.getInt(); 
            RegionY = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(RegionX);
            bytes.putInt(RegionY);
        }

        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "RegionX: " + Integer.toString(RegionX) + "\n";
                output += "RegionY: " + Integer.toString(RegionY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    public class UsersBlock
    {
        public UUID AgentID = null;

        public int getLength(){
            return 16;
        }

        public UsersBlock() { }
        public UsersBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- Users --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public UsersBlock createUsersBlock() {
         return new UsersBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.SimCrashed; }
    public DataBlock Data;
    public UsersBlock[] Users;

    public SimCrashedPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)328);
        header.setReliable(true);
        Data = new DataBlock();
        Users = new UsersBlock[0];
    }

    public SimCrashedPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        Data = new DataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        Users = new UsersBlock[count];
        for (int j = 0; j < count; j++)
        { Users[j] = new UsersBlock(bytes); }
     }

    public SimCrashedPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        Users = new UsersBlock[count];
        for (int j = 0; j < count; j++)
        { Users[j] = new UsersBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        length++;
        for (int j = 0; j < Users.length; j++) { length += Users[j].getLength(); }
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
        Data.ToBytes(bytes);
        bytes.put((byte)Users.length);
        for (int j = 0; j < Users.length; j++) { Users[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- SimCrashed ---\n";
        output += Data.toString() + "\n";
        for (int j = 0; j < Users.length; j++)
        {
            output += Users[j].toString() + "\n";
        }
        return output;
    }
}
