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

public class ErrorPacket extends Packet
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

    public class DataBlock
    {
        public int Code = 0;
        private byte[] _token;
        public byte[] getToken() {
            return _token;
        }

        public void setToken(byte[] value) throws Exception {
            if (value == null) {
                _token = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _token = new byte[value.length];
                System.arraycopy(value, 0, _token, 0, value.length);
            }
        }

        public UUID ID = null;
        private byte[] _system;
        public byte[] getSystem() {
            return _system;
        }

        public void setSystem(byte[] value) throws Exception {
            if (value == null) {
                _system = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _system = new byte[value.length];
                System.arraycopy(value, 0, _system, 0, value.length);
            }
        }

        private byte[] _message;
        public byte[] getMessage() {
            return _message;
        }

        public void setMessage(byte[] value) throws Exception {
            if (value == null) {
                _message = null;
            }
            if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _message = new byte[value.length];
                System.arraycopy(value, 0, _message, 0, value.length);
            }
        }

        private byte[] _data;
        public byte[] getData() {
            return _data;
        }

        public void setData(byte[] value) throws Exception {
            if (value == null) {
                _data = null;
            }
            if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _data = new byte[value.length];
                System.arraycopy(value, 0, _data, 0, value.length);
            }
        }


        public int getLength(){
            int length = 20;
            if (getToken() != null) { length += 1 + getToken().length; }
            if (getSystem() != null) { length += 1 + getSystem().length; }
            if (getMessage() != null) { length += 2 + getMessage().length; }
            if (getData() != null) { length += 2 + getData().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            Code = bytes.getInt(); 
            length = (int)(bytes.get()) & 0xFF;
            _token = new byte[length];
            bytes.get(_token); 
            ID = new UUID(bytes);
            length = (int)(bytes.get()) & 0xFF;
            _system = new byte[length];
            bytes.get(_system); 
            length = (int)(bytes.getShort()) & 0xFFFF;
            _message = new byte[length];
            bytes.get(_message); 
            length = (int)(bytes.getShort()) & 0xFFFF;
            _data = new byte[length];
            bytes.get(_data); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Code);
            bytes.put((byte)_token.length);
            bytes.put(_token);
            ID.GetBytes(bytes);
            bytes.put((byte)_system.length);
            bytes.put(_system);
            bytes.putShort((short)_message.length);
            bytes.put(_message);
            bytes.putShort((short)_data.length);
            bytes.put(_data);
        }

        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "Code: " + Integer.toString(Code) + "\n";
                output += Helpers.FieldToString(_token, "Token") + "\n";
                output += "ID: " + ID.toString() + "\n";
                output += Helpers.FieldToString(_system, "System") + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output += Helpers.FieldToString(_data, "Data") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.Error; }
    public AgentDataBlock AgentData;
    public DataBlock Data;

    public ErrorPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)423);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        Data = new DataBlock();
    }

    public ErrorPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        Data = new DataBlock(bytes);
     }

    public ErrorPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        Data = new DataBlock(bytes);
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += Data.getLength();
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
        Data.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- Error ---\n";
        output += AgentData.toString() + "\n";
        output += Data.toString() + "\n";
        return output;
    }
}
