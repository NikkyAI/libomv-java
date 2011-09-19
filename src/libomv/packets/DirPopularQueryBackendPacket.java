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

public class DirPopularQueryBackendPacket extends Packet
{
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

        @Override
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

    public class QueryDataBlock
    {
        public UUID QueryID = null;
        public int QueryFlags = 0;
        public int EstateID = 0;
        public boolean Godlike = false;

        public int getLength(){
            return 25;
        }

        public QueryDataBlock() { }
        public QueryDataBlock(ByteBuffer bytes)
        {
            QueryID = new UUID(bytes);
            QueryFlags = bytes.getInt(); 
            EstateID = bytes.getInt(); 
            Godlike = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            QueryID.GetBytes(bytes);
            bytes.putInt(QueryFlags);
            bytes.putInt(EstateID);
            bytes.put((byte)((Godlike) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- QueryData --\n";
            try {
                output += "QueryID: " + QueryID.toString() + "\n";
                output += "QueryFlags: " + Integer.toString(QueryFlags) + "\n";
                output += "EstateID: " + Integer.toString(EstateID) + "\n";
                output += "Godlike: " + Boolean.toString(Godlike) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public QueryDataBlock createQueryDataBlock() {
         return new QueryDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.DirPopularQueryBackend; }
    public AgentDataBlock AgentData;
    public QueryDataBlock QueryData;

    public DirPopularQueryBackendPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)52);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        QueryData = new QueryDataBlock();
    }

    public DirPopularQueryBackendPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        QueryData = new QueryDataBlock(bytes);
     }

    public DirPopularQueryBackendPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        QueryData = new QueryDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += QueryData.getLength();
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
        AgentData.ToBytes(bytes);
        QueryData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- DirPopularQueryBackend ---\n";
        output += AgentData.toString() + "\n";
        output += QueryData.toString() + "\n";
        return output;
    }
}
