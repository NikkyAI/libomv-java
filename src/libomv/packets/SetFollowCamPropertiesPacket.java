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

public class SetFollowCamPropertiesPacket extends Packet
{
    public class ObjectDataBlock
    {
        public UUID ObjectID = null;

        public int getLength(){
            return 16;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            ObjectID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ObjectDataBlock createObjectDataBlock() {
         return new ObjectDataBlock();
    }

    public class CameraPropertyBlock
    {
        public int Type = 0;
        public float Value = 0;

        public int getLength(){
            return 8;
        }

        public CameraPropertyBlock() { }
        public CameraPropertyBlock(ByteBuffer bytes)
        {
            Type = bytes.getInt(); 
            Value = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Type);
            bytes.putFloat(Value);
        }

        @Override
        public String toString()
        {
            String output = "-- CameraProperty --\n";
            try {
                output += "Type: " + Integer.toString(Type) + "\n";
                output += "Value: " + Float.toString(Value) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public CameraPropertyBlock createCameraPropertyBlock() {
         return new CameraPropertyBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SetFollowCamProperties; }
    public ObjectDataBlock ObjectData;
    public CameraPropertyBlock[] CameraProperty;

    public SetFollowCamPropertiesPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)159);
        header.setReliable(true);
        ObjectData = new ObjectDataBlock();
        CameraProperty = new CameraPropertyBlock[0];
    }

    public SetFollowCamPropertiesPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        CameraProperty = new CameraPropertyBlock[count];
        for (int j = 0; j < count; j++)
        { CameraProperty[j] = new CameraPropertyBlock(bytes); }
     }

    public SetFollowCamPropertiesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        CameraProperty = new CameraPropertyBlock[count];
        for (int j = 0; j < count; j++)
        { CameraProperty[j] = new CameraPropertyBlock(bytes); }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ObjectData.getLength();
        length++;
        for (int j = 0; j < CameraProperty.length; j++) { length += CameraProperty[j].getLength(); }
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
        bytes.put((byte)CameraProperty.length);
        for (int j = 0; j < CameraProperty.length; j++) { CameraProperty[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SetFollowCamProperties ---\n";
        output += ObjectData.toString() + "\n";
        for (int j = 0; j < CameraProperty.length; j++)
        {
            output += CameraProperty[j].toString() + "\n";
        }
        return output;
    }
}
