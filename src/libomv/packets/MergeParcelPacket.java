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

public class MergeParcelPacket extends Packet
{
    public class MasterParcelDataBlock
    {
        public UUID MasterID = null;

        public int getLength(){
            return 16;
        }

        public MasterParcelDataBlock() { }
        public MasterParcelDataBlock(ByteBuffer bytes)
        {
            MasterID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            MasterID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- MasterParcelData --\n";
            try {
                output += "MasterID: " + MasterID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MasterParcelDataBlock createMasterParcelDataBlock() {
         return new MasterParcelDataBlock();
    }

    public class SlaveParcelDataBlock
    {
        public UUID SlaveID = null;

        public int getLength(){
            return 16;
        }

        public SlaveParcelDataBlock() { }
        public SlaveParcelDataBlock(ByteBuffer bytes)
        {
            SlaveID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            SlaveID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- SlaveParcelData --\n";
            try {
                output += "SlaveID: " + SlaveID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SlaveParcelDataBlock createSlaveParcelDataBlock() {
         return new SlaveParcelDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.MergeParcel; }
    public MasterParcelDataBlock MasterParcelData;
    public SlaveParcelDataBlock[] SlaveParcelData;

    public MergeParcelPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)223);
        header.setReliable(true);
        MasterParcelData = new MasterParcelDataBlock();
        SlaveParcelData = new SlaveParcelDataBlock[0];
    }

    public MergeParcelPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        MasterParcelData = new MasterParcelDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        SlaveParcelData = new SlaveParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        { SlaveParcelData[j] = new SlaveParcelDataBlock(bytes); }
     }

    public MergeParcelPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        MasterParcelData = new MasterParcelDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        SlaveParcelData = new SlaveParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        { SlaveParcelData[j] = new SlaveParcelDataBlock(bytes); }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += MasterParcelData.getLength();
        length++;
        for (int j = 0; j < SlaveParcelData.length; j++) { length += SlaveParcelData[j].getLength(); }
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
        MasterParcelData.ToBytes(bytes);
        bytes.put((byte)SlaveParcelData.length);
        for (int j = 0; j < SlaveParcelData.length; j++) { SlaveParcelData[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- MergeParcel ---\n";
        output += MasterParcelData.toString() + "\n";
        for (int j = 0; j < SlaveParcelData.length; j++)
        {
            output += SlaveParcelData[j].toString() + "\n";
        }
        return output;
    }
}
