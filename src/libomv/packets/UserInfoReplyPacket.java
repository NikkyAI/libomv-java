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

public class UserInfoReplyPacket extends Packet
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

    public class UserDataBlock
    {
        public boolean IMViaEMail = false;
        private byte[] _directoryvisibility;
        public byte[] getDirectoryVisibility() {
            return _directoryvisibility;
        }

        public void setDirectoryVisibility(byte[] value) throws Exception {
            if (value == null) {
                _directoryvisibility = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _directoryvisibility = new byte[value.length];
                System.arraycopy(value, 0, _directoryvisibility, 0, value.length);
            }
        }

        private byte[] _email;
        public byte[] getEMail() {
            return _email;
        }

        public void setEMail(byte[] value) throws Exception {
            if (value == null) {
                _email = null;
            }
            if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _email = new byte[value.length];
                System.arraycopy(value, 0, _email, 0, value.length);
            }
        }


        public int getLength(){
            int length = 1;
            if (getDirectoryVisibility() != null) { length += 1 + getDirectoryVisibility().length; }
            if (getEMail() != null) { length += 2 + getEMail().length; }
            return length;
        }

        public UserDataBlock() { }
        public UserDataBlock(ByteBuffer bytes)
        {
            int length;
            IMViaEMail = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = (int)(bytes.get()) & 0xFF;
            _directoryvisibility = new byte[length];
            bytes.get(_directoryvisibility); 
            length = (int)(bytes.getShort()) & 0xFFFF;
            _email = new byte[length];
            bytes.get(_email); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((IMViaEMail) ? 1 : 0));
            bytes.put((byte)_directoryvisibility.length);
            bytes.put(_directoryvisibility);
            bytes.putShort((short)_email.length);
            bytes.put(_email);
        }

        public String toString()
        {
            String output = "-- UserData --\n";
            try {
                output += "IMViaEMail: " + Boolean.toString(IMViaEMail) + "\n";
                output += Helpers.FieldToString(_directoryvisibility, "DirectoryVisibility") + "\n";
                output += Helpers.FieldToString(_email, "EMail") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public UserDataBlock createUserDataBlock() {
         return new UserDataBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.UserInfoReply; }
    public AgentDataBlock AgentData;
    public UserDataBlock UserData;

    public UserInfoReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)400);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        UserData = new UserDataBlock();
    }

    public UserInfoReplyPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        UserData = new UserDataBlock(bytes);
     }

    public UserInfoReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        UserData = new UserDataBlock(bytes);
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += UserData.getLength();
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
        UserData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- UserInfoReply ---\n";
        output += AgentData.toString() + "\n";
        output += UserData.toString() + "\n";
        return output;
    }
}
