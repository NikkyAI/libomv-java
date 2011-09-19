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

public class PayPriceReplyPacket extends Packet
{
    public class ObjectDataBlock
    {
        public UUID ObjectID = null;
        public int DefaultPayPrice = 0;

        public int getLength(){
            return 20;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            ObjectID = new UUID(bytes);
            DefaultPayPrice = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            bytes.putInt(DefaultPayPrice);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "DefaultPayPrice: " + Integer.toString(DefaultPayPrice) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ObjectDataBlock createObjectDataBlock() {
         return new ObjectDataBlock();
    }

    public class ButtonDataBlock
    {
        public int PayButton = 0;

        public int getLength(){
            return 4;
        }

        public ButtonDataBlock() { }
        public ButtonDataBlock(ByteBuffer bytes)
        {
            PayButton = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(PayButton);
        }

        @Override
        public String toString()
        {
            String output = "-- ButtonData --\n";
            try {
                output += "PayButton: " + Integer.toString(PayButton) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ButtonDataBlock createButtonDataBlock() {
         return new ButtonDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.PayPriceReply; }
    public ObjectDataBlock ObjectData;
    public ButtonDataBlock[] ButtonData;

    public PayPriceReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)162);
        header.setReliable(true);
        ObjectData = new ObjectDataBlock();
        ButtonData = new ButtonDataBlock[0];
    }

    public PayPriceReplyPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ButtonData = new ButtonDataBlock[count];
        for (int j = 0; j < count; j++)
        { ButtonData[j] = new ButtonDataBlock(bytes); }
     }

    public PayPriceReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ButtonData = new ButtonDataBlock[count];
        for (int j = 0; j < count; j++)
        { ButtonData[j] = new ButtonDataBlock(bytes); }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ObjectData.getLength();
        length++;
        for (int j = 0; j < ButtonData.length; j++) { length += ButtonData[j].getLength(); }
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
        ObjectData.ToBytes(bytes);
        bytes.put((byte)ButtonData.length);
        for (int j = 0; j < ButtonData.length; j++) { ButtonData[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- PayPriceReply ---\n";
        output += ObjectData.toString() + "\n";
        for (int j = 0; j < ButtonData.length; j++)
        {
            output += ButtonData[j].toString() + "\n";
        }
        return output;
    }
}
