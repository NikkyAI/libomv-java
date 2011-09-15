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

import libomv.types.PacketFrequency;
import libomv.types.PacketHeader;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Quaternion;

public class AvatarSitResponsePacket extends Packet
{
    public class SitObjectBlock
    {
        public UUID ID = null;

        public int getLength(){
            return 16;
        }

        public SitObjectBlock() { }
        public SitObjectBlock(ByteBuffer bytes)
        {
            ID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- SitObject --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SitObjectBlock createSitObjectBlock() {
         return new SitObjectBlock();
    }

    public class SitTransformBlock
    {
        public boolean AutoPilot = false;
        public Vector3 SitPosition = null;
        public Quaternion SitRotation = null;
        public Vector3 CameraEyeOffset = null;
        public Vector3 CameraAtOffset = null;
        public boolean ForceMouselook = false;

        public int getLength(){
            return 50;
        }

        public SitTransformBlock() { }
        public SitTransformBlock(ByteBuffer bytes)
        {
            AutoPilot = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            SitPosition = new Vector3(bytes); 
            SitRotation = new Quaternion(bytes, true); 
            CameraEyeOffset = new Vector3(bytes); 
            CameraAtOffset = new Vector3(bytes); 
            ForceMouselook = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((AutoPilot) ? 1 : 0));
            SitPosition.GetBytes(bytes);
            SitRotation.GetBytes(bytes);
            CameraEyeOffset.GetBytes(bytes);
            CameraAtOffset.GetBytes(bytes);
            bytes.put((byte)((ForceMouselook) ? 1 : 0));
        }

        public String toString()
        {
            String output = "-- SitTransform --\n";
            try {
                output += "AutoPilot: " + Boolean.toString(AutoPilot) + "\n";
                output += "SitPosition: " + SitPosition.toString() + "\n";
                output += "SitRotation: " + SitRotation.toString() + "\n";
                output += "CameraEyeOffset: " + CameraEyeOffset.toString() + "\n";
                output += "CameraAtOffset: " + CameraAtOffset.toString() + "\n";
                output += "ForceMouselook: " + Boolean.toString(ForceMouselook) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SitTransformBlock createSitTransformBlock() {
         return new SitTransformBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.AvatarSitResponse; }
    public SitObjectBlock SitObject;
    public SitTransformBlock SitTransform;

    public AvatarSitResponsePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)21);
        header.setReliable(true);
        SitObject = new SitObjectBlock();
        SitTransform = new SitTransformBlock();
    }

    public AvatarSitResponsePacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        SitObject = new SitObjectBlock(bytes);
        SitTransform = new SitTransformBlock(bytes);
     }

    public AvatarSitResponsePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        SitObject = new SitObjectBlock(bytes);
        SitTransform = new SitTransformBlock(bytes);
    }

    public int getLength()
    {
        int length = header.getLength();
        length += SitObject.getLength();
        length += SitTransform.getLength();
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
        SitObject.ToBytes(bytes);
        SitTransform.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- AvatarSitResponse ---\n";
        output += SitObject.toString() + "\n";
        output += SitTransform.toString() + "\n";
        return output;
    }
}
