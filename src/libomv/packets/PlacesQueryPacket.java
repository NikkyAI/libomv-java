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
import libomv.types.PacketFrequency;
import libomv.types.PacketHeader;
import libomv.types.UUID;
import libomv.types.OverflowException;

public class PlacesQueryPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public UUID QueryID = null;

        public int getLength(){
            return 48;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            QueryID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            QueryID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "QueryID: " + QueryID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class TransactionDataBlock
    {
        public UUID TransactionID = null;

        public int getLength(){
            return 16;
        }

        public TransactionDataBlock() { }
        public TransactionDataBlock(ByteBuffer bytes)
        {
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransactionID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- TransactionData --\n";
            try {
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransactionDataBlock createTransactionDataBlock() {
         return new TransactionDataBlock();
    }

    public class QueryDataBlock
    {
        private byte[] _querytext;
        public byte[] getQueryText() {
            return _querytext;
        }

        public void setQueryText(byte[] value) throws Exception {
            if (value == null) {
                _querytext = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _querytext = new byte[value.length];
                System.arraycopy(value, 0, _querytext, 0, value.length);
            }
        }

        public int QueryFlags = 0;
        public byte Category = 0;
        private byte[] _simname;
        public byte[] getSimName() {
            return _simname;
        }

        public void setSimName(byte[] value) throws Exception {
            if (value == null) {
                _simname = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simname = new byte[value.length];
                System.arraycopy(value, 0, _simname, 0, value.length);
            }
        }


        public int getLength(){
            int length = 5;
            if (getQueryText() != null) { length += 1 + getQueryText().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public QueryDataBlock() { }
        public QueryDataBlock(ByteBuffer bytes)
        {
            int length;
            length = (int)(bytes.get()) & 0xFF;
            _querytext = new byte[length];
            bytes.get(_querytext); 
            QueryFlags = bytes.getInt(); 
            Category = bytes.get(); 
            length = (int)(bytes.get()) & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_querytext.length);
            bytes.put(_querytext);
            bytes.putInt(QueryFlags);
            bytes.put(Category);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
        }

        public String toString()
        {
            String output = "-- QueryData --\n";
            try {
                output += Helpers.FieldToString(_querytext, "QueryText") + "\n";
                output += "QueryFlags: " + Integer.toString(QueryFlags) + "\n";
                output += "Category: " + Byte.toString(Category) + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
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
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.PlacesQuery; }
    public AgentDataBlock AgentData;
    public TransactionDataBlock TransactionData;
    public QueryDataBlock QueryData;

    public PlacesQueryPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)29);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        TransactionData = new TransactionDataBlock();
        QueryData = new QueryDataBlock();
    }

    public PlacesQueryPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        TransactionData = new TransactionDataBlock(bytes);
        QueryData = new QueryDataBlock(bytes);
     }

    public PlacesQueryPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        TransactionData = new TransactionDataBlock(bytes);
        QueryData = new QueryDataBlock(bytes);
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += TransactionData.getLength();
        length += QueryData.getLength();
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
        TransactionData.ToBytes(bytes);
        QueryData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- PlacesQuery ---\n";
        output += AgentData.toString() + "\n";
        output += TransactionData.toString() + "\n";
        output += QueryData.toString() + "\n";
        return output;
    }
}
