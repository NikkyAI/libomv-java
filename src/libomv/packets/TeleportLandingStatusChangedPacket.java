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

public class TeleportLandingStatusChangedPacket extends Packet
{
    public class RegionDataBlock
    {
        public long RegionHandle = 0;

        public int getLength(){
            return 8;
        }

        public RegionDataBlock() { }
        public RegionDataBlock(ByteBuffer bytes)
        {
            RegionHandle = bytes.getLong(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(RegionHandle);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionData --\n";
            try {
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionDataBlock createRegionDataBlock() {
         return new RegionDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TeleportLandingStatusChanged; }
    public RegionDataBlock RegionData;

    public TeleportLandingStatusChangedPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)147);
        header.setReliable(true);
        RegionData = new RegionDataBlock();
    }

    public TeleportLandingStatusChangedPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        RegionData = new RegionDataBlock(bytes);
     }

    public TeleportLandingStatusChangedPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        RegionData = new RegionDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += RegionData.getLength();
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
        RegionData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TeleportLandingStatusChanged ---\n";
        output += RegionData.toString() + "\n";
        return output;
    }
}
