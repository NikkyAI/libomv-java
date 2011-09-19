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

public class UpdateAttachmentPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class AttachmentBlockBlock
    {
        public byte AttachmentPoint = 0;

        public int getLength(){
            return 1;
        }

        public AttachmentBlockBlock() { }
        public AttachmentBlockBlock(ByteBuffer bytes)
        {
            AttachmentPoint = bytes.get(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(AttachmentPoint);
        }

        @Override
        public String toString()
        {
            String output = "-- AttachmentBlock --\n";
            try {
                output += "AttachmentPoint: " + Byte.toString(AttachmentPoint) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AttachmentBlockBlock createAttachmentBlockBlock() {
         return new AttachmentBlockBlock();
    }

    public class OperationDataBlock
    {
        public boolean AddItem = false;
        public boolean UseExistingAsset = false;

        public int getLength(){
            return 2;
        }

        public OperationDataBlock() { }
        public OperationDataBlock(ByteBuffer bytes)
        {
            AddItem = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            UseExistingAsset = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((AddItem) ? 1 : 0));
            bytes.put((byte)((UseExistingAsset) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- OperationData --\n";
            try {
                output += "AddItem: " + Boolean.toString(AddItem) + "\n";
                output += "UseExistingAsset: " + Boolean.toString(UseExistingAsset) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public OperationDataBlock createOperationDataBlock() {
         return new OperationDataBlock();
    }

    public class InventoryDataBlock
    {
        public UUID ItemID = null;
        public UUID FolderID = null;
        public UUID CreatorID = null;
        public UUID OwnerID = null;
        public UUID GroupID = null;
        public int BaseMask = 0;
        public int OwnerMask = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;
        public boolean GroupOwned = false;
        public UUID AssetID = null;
        public byte Type = 0;
        public byte InvType = 0;
        public int Flags = 0;
        public byte SaleType = 0;
        public int SalePrice = 0;
        private byte[] _name;
        public byte[] getName() {
            return _name;
        }

        public void setName(byte[] value) throws Exception {
            if (value == null) {
                _name = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _name = new byte[value.length];
                System.arraycopy(value, 0, _name, 0, value.length);
            }
        }

        private byte[] _description;
        public byte[] getDescription() {
            return _description;
        }

        public void setDescription(byte[] value) throws Exception {
            if (value == null) {
                _description = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _description = new byte[value.length];
                System.arraycopy(value, 0, _description, 0, value.length);
            }
        }

        public int CreationDate = 0;
        public int CRC = 0;

        public int getLength(){
            int length = 136;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public InventoryDataBlock() { }
        public InventoryDataBlock(ByteBuffer bytes)
        {
            int length;
            ItemID = new UUID(bytes);
            FolderID = new UUID(bytes);
            CreatorID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            GroupID = new UUID(bytes);
            BaseMask = bytes.getInt(); 
            OwnerMask = bytes.getInt(); 
            GroupMask = bytes.getInt(); 
            EveryoneMask = bytes.getInt(); 
            NextOwnerMask = bytes.getInt(); 
            GroupOwned = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            AssetID = new UUID(bytes);
            Type = bytes.get(); 
            InvType = bytes.get(); 
            Flags = bytes.getInt(); 
            SaleType = bytes.get(); 
            SalePrice = bytes.getInt(); 
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name); 
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description); 
            CreationDate = bytes.getInt(); 
            CRC = bytes.getInt(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ItemID.GetBytes(bytes);
            FolderID.GetBytes(bytes);
            CreatorID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            bytes.putInt(BaseMask);
            bytes.putInt(OwnerMask);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
            bytes.put((byte)((GroupOwned) ? 1 : 0));
            AssetID.GetBytes(bytes);
            bytes.put(Type);
            bytes.put(InvType);
            bytes.putInt(Flags);
            bytes.put(SaleType);
            bytes.putInt(SalePrice);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_description.length);
            bytes.put(_description);
            bytes.putInt(CreationDate);
            bytes.putInt(CRC);
        }

        @Override
        public String toString()
        {
            String output = "-- InventoryData --\n";
            try {
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "FolderID: " + FolderID.toString() + "\n";
                output += "CreatorID: " + CreatorID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "BaseMask: " + Integer.toString(BaseMask) + "\n";
                output += "OwnerMask: " + Integer.toString(OwnerMask) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
                output += "GroupOwned: " + Boolean.toString(GroupOwned) + "\n";
                output += "AssetID: " + AssetID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += "InvType: " + Byte.toString(InvType) + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output += "SaleType: " + Byte.toString(SaleType) + "\n";
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
                output += "CreationDate: " + Integer.toString(CreationDate) + "\n";
                output += "CRC: " + Integer.toString(CRC) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InventoryDataBlock createInventoryDataBlock() {
         return new InventoryDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UpdateAttachment; }
    public AgentDataBlock AgentData;
    public AttachmentBlockBlock AttachmentBlock;
    public OperationDataBlock OperationData;
    public InventoryDataBlock InventoryData;

    public UpdateAttachmentPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)331);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        AttachmentBlock = new AttachmentBlockBlock();
        OperationData = new OperationDataBlock();
        InventoryData = new InventoryDataBlock();
    }

    public UpdateAttachmentPacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        AttachmentBlock = new AttachmentBlockBlock(bytes);
        OperationData = new OperationDataBlock(bytes);
        InventoryData = new InventoryDataBlock(bytes);
     }

    public UpdateAttachmentPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        AttachmentBlock = new AttachmentBlockBlock(bytes);
        OperationData = new OperationDataBlock(bytes);
        InventoryData = new InventoryDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += AttachmentBlock.getLength();
        length += OperationData.getLength();
        length += InventoryData.getLength();
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
        AgentData.ToBytes(bytes);
        AttachmentBlock.ToBytes(bytes);
        OperationData.ToBytes(bytes);
        InventoryData.ToBytes(bytes);
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UpdateAttachment ---\n";
        output += AgentData.toString() + "\n";
        output += AttachmentBlock.toString() + "\n";
        output += OperationData.toString() + "\n";
        output += InventoryData.toString() + "\n";
        return output;
    }
}
