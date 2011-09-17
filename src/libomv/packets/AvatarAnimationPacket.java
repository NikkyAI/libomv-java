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
import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;
import libomv.types.OverflowException;

public class AvatarAnimationPacket extends Packet
{
    public class SenderBlock
    {
        public UUID ID = null;

        public int getLength(){
            return 16;
        }

        public SenderBlock() { }
        public SenderBlock(ByteBuffer bytes)
        {
            ID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- Sender --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SenderBlock createSenderBlock() {
         return new SenderBlock();
    }

    public class AnimationListBlock
    {
        public UUID AnimID = null;
        public int AnimSequenceID = 0;

        public int getLength(){
            return 20;
        }

        public AnimationListBlock() { }
        public AnimationListBlock(ByteBuffer bytes)
        {
            AnimID = new UUID(bytes);
            AnimSequenceID = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AnimID.GetBytes(bytes);
            bytes.putInt(AnimSequenceID);
        }

        public String toString()
        {
            String output = "-- AnimationList --\n";
            try {
                output += "AnimID: " + AnimID.toString() + "\n";
                output += "AnimSequenceID: " + Integer.toString(AnimSequenceID) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AnimationListBlock createAnimationListBlock() {
         return new AnimationListBlock();
    }

    public class AnimationSourceListBlock
    {
        public UUID ObjectID = null;

        public int getLength(){
            return 16;
        }

        public AnimationSourceListBlock() { }
        public AnimationSourceListBlock(ByteBuffer bytes)
        {
            ObjectID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AnimationSourceList --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AnimationSourceListBlock createAnimationSourceListBlock() {
         return new AnimationSourceListBlock();
    }

    public class PhysicalAvatarEventListBlock
    {
        private byte[] _typedata;
        public byte[] getTypeData() {
            return _typedata;
        }

        public void setTypeData(byte[] value) throws Exception {
            if (value == null) {
                _typedata = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _typedata = new byte[value.length];
                System.arraycopy(value, 0, _typedata, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getTypeData() != null) { length += 1 + getTypeData().length; }
            return length;
        }

        public PhysicalAvatarEventListBlock() { }
        public PhysicalAvatarEventListBlock(ByteBuffer bytes)
        {
            int length;
            length = (int)(bytes.get()) & 0xFF;
            _typedata = new byte[length];
            bytes.get(_typedata); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_typedata.length);
            bytes.put(_typedata);
        }

        public String toString()
        {
            String output = "-- PhysicalAvatarEventList --\n";
            try {
                output += Helpers.FieldToString(_typedata, "TypeData") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public PhysicalAvatarEventListBlock createPhysicalAvatarEventListBlock() {
         return new PhysicalAvatarEventListBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.AvatarAnimation; }
    public SenderBlock Sender;
    public AnimationListBlock[] AnimationList;
    public AnimationSourceListBlock[] AnimationSourceList;
    public PhysicalAvatarEventListBlock[] PhysicalAvatarEventList;

    public AvatarAnimationPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)20);
        header.setReliable(true);
        Sender = new SenderBlock();
        AnimationList = new AnimationListBlock[0];
        AnimationSourceList = new AnimationSourceListBlock[0];
        PhysicalAvatarEventList = new PhysicalAvatarEventListBlock[0];
    }

    public AvatarAnimationPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        Sender = new SenderBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        AnimationList = new AnimationListBlock[count];
        for (int j = 0; j < count; j++)
        { AnimationList[j] = new AnimationListBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        AnimationSourceList = new AnimationSourceListBlock[count];
        for (int j = 0; j < count; j++)
        { AnimationSourceList[j] = new AnimationSourceListBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        PhysicalAvatarEventList = new PhysicalAvatarEventListBlock[count];
        for (int j = 0; j < count; j++)
        { PhysicalAvatarEventList[j] = new PhysicalAvatarEventListBlock(bytes); }
     }

    public AvatarAnimationPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Sender = new SenderBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        AnimationList = new AnimationListBlock[count];
        for (int j = 0; j < count; j++)
        { AnimationList[j] = new AnimationListBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        AnimationSourceList = new AnimationSourceListBlock[count];
        for (int j = 0; j < count; j++)
        { AnimationSourceList[j] = new AnimationSourceListBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        PhysicalAvatarEventList = new PhysicalAvatarEventListBlock[count];
        for (int j = 0; j < count; j++)
        { PhysicalAvatarEventList[j] = new PhysicalAvatarEventListBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += Sender.getLength();
        length++;
        for (int j = 0; j < AnimationList.length; j++) { length += AnimationList[j].getLength(); }
        length++;
        for (int j = 0; j < AnimationSourceList.length; j++) { length += AnimationSourceList[j].getLength(); }
        length++;
        for (int j = 0; j < PhysicalAvatarEventList.length; j++) { length += PhysicalAvatarEventList[j].getLength(); }
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
        Sender.ToBytes(bytes);
        bytes.put((byte)AnimationList.length);
        for (int j = 0; j < AnimationList.length; j++) { AnimationList[j].ToBytes(bytes); }
        bytes.put((byte)AnimationSourceList.length);
        for (int j = 0; j < AnimationSourceList.length; j++) { AnimationSourceList[j].ToBytes(bytes); }
        bytes.put((byte)PhysicalAvatarEventList.length);
        for (int j = 0; j < PhysicalAvatarEventList.length; j++) { PhysicalAvatarEventList[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- AvatarAnimation ---\n";
        output += Sender.toString() + "\n";
        for (int j = 0; j < AnimationList.length; j++)
        {
            output += AnimationList[j].toString() + "\n";
        }
        for (int j = 0; j < AnimationSourceList.length; j++)
        {
            output += AnimationSourceList[j].toString() + "\n";
        }
        for (int j = 0; j < PhysicalAvatarEventList.length; j++)
        {
            output += PhysicalAvatarEventList[j].toString() + "\n";
        }
        return output;
    }
}
