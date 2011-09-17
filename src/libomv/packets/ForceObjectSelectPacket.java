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

public class ForceObjectSelectPacket extends Packet
{
    public class HeaderBlock
    {
        public boolean ResetList = false;

        public int getLength(){
            return 1;
        }

        public HeaderBlock() { }
        public HeaderBlock(ByteBuffer bytes)
        {
            ResetList = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((ResetList) ? 1 : 0));
        }

        public String toString()
        {
            String output = "-- Header --\n";
            try {
                output += "ResetList: " + Boolean.toString(ResetList) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public HeaderBlock createHeaderBlock() {
         return new HeaderBlock();
    }

    public class DataBlock
    {
        public int LocalID = 0;

        public int getLength(){
            return 4;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            LocalID = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(LocalID);
        }

        public String toString()
        {
            String output = "-- Data --\n";
            try {
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

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.ForceObjectSelect; }
    public HeaderBlock _Header;
    public DataBlock[] Data;

    public ForceObjectSelectPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)205);
        header.setReliable(true);
        _Header = new HeaderBlock();
        Data = new DataBlock[0];
    }

    public ForceObjectSelectPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        _Header = new HeaderBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        Data = new DataBlock[count];
        for (int j = 0; j < count; j++)
        { Data[j] = new DataBlock(bytes); }
     }

    public ForceObjectSelectPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        _Header = new HeaderBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        Data = new DataBlock[count];
        for (int j = 0; j < count; j++)
        { Data[j] = new DataBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += _Header.getLength();
        length++;
        for (int j = 0; j < Data.length; j++) { length += Data[j].getLength(); }
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
        _Header.ToBytes(bytes);
        bytes.put((byte)Data.length);
        for (int j = 0; j < Data.length; j++) { Data[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- ForceObjectSelect ---\n";
        output += _Header.toString() + "\n";
        for (int j = 0; j < Data.length; j++)
        {
            output += Data[j].toString() + "\n";
        }
        return output;
    }
}
