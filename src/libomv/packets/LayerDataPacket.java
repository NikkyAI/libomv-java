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
import libomv.types.OverflowException;
import libomv.types.PacketHeader;

public class LayerDataPacket extends Packet
{
    public class LayerIDBlock
    {
        public byte Type = 0;

        public int getLength(){
            return 1;
        }

        public LayerIDBlock() { }
        public LayerIDBlock(ByteBuffer bytes)
        {
            Type = bytes.get(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(Type);
        }

        public String toString()
        {
            String output = "-- LayerID --\n";
            try {
                output += "Type: " + Byte.toString(Type) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public LayerIDBlock createLayerIDBlock() {
         return new LayerIDBlock();
    }

    public class LayerDataBlock
    {
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
            int length = 0;
            if (getData() != null) { length += 2 + getData().length; }
            return length;
        }

        public LayerDataBlock() { }
        public LayerDataBlock(ByteBuffer bytes)
        {
            int length;
            length = (int)(bytes.getShort()) & 0xFFFF;
            _data = new byte[length];
            bytes.get(_data); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_data.length);
            bytes.put(_data);
        }

        public String toString()
        {
            String output = "-- LayerData --\n";
            try {
                output += Helpers.FieldToString(_data, "Data") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public LayerDataBlock createLayerDataBlock() {
         return new LayerDataBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.LayerData; }
    public LayerIDBlock LayerID;
    public LayerDataBlock LayerData;

    public LayerDataPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)11);
        header.setReliable(true);
        LayerID = new LayerIDBlock();
        LayerData = new LayerDataBlock();
    }

    public LayerDataPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        LayerID = new LayerIDBlock(bytes);
        LayerData = new LayerDataBlock(bytes);
     }

    public LayerDataPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        LayerID = new LayerIDBlock(bytes);
        LayerData = new LayerDataBlock(bytes);
    }

    public int getLength()
    {
        int length = header.getLength();
        length += LayerID.getLength();
        length += LayerData.getLength();
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
        LayerID.ToBytes(bytes);
        LayerData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- LayerData ---\n";
        output += LayerID.toString() + "\n";
        output += LayerData.toString() + "\n";
        return output;
    }
}
