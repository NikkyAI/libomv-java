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
import libomv.types.Vector3;
import libomv.types.OverflowException;
import libomv.types.Quaternion;

public class ChildAgentUpdatePacket extends Packet
{
    public class AgentDataBlock
    {
        public long RegionHandle = 0;
        public int ViewerCircuitCode = 0;
        public UUID AgentID = null;
        public UUID SessionID = null;
        public Vector3 AgentPos = null;
        public Vector3 AgentVel = null;
        public Vector3 Center = null;
        public Vector3 Size = null;
        public Vector3 AtAxis = null;
        public Vector3 LeftAxis = null;
        public Vector3 UpAxis = null;
        public boolean ChangedGrid = false;
        public float Far = 0;
        public float Aspect = 0;
        private byte[] _throttles;
        public byte[] getThrottles() {
            return _throttles;
        }

        public void setThrottles(byte[] value) throws Exception {
            if (value == null) {
                _throttles = null;
            }
            if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _throttles = new byte[value.length];
                System.arraycopy(value, 0, _throttles, 0, value.length);
            }
        }

        public int LocomotionState = 0;
        public Quaternion HeadRotation = null;
        public Quaternion BodyRotation = null;
        public int ControlFlags = 0;
        public float EnergyLevel = 0;
        public byte GodLevel = 0;
        public boolean AlwaysRun = false;
        public UUID PreyAgent = null;
        public byte AgentAccess = 0;
        private byte[] _agenttextures;
        public byte[] getAgentTextures() {
            return _agenttextures;
        }

        public void setAgentTextures(byte[] value) throws Exception {
            if (value == null) {
                _agenttextures = null;
            }
            if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _agenttextures = new byte[value.length];
                System.arraycopy(value, 0, _agenttextures, 0, value.length);
            }
        }

        public UUID ActiveGroupID = null;

        public int getLength(){
            int length = 208;
            if (getThrottles() != null) { length += 1 + getThrottles().length; }
            if (getAgentTextures() != null) { length += 2 + getAgentTextures().length; }
            return length;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            int length;
            RegionHandle = bytes.getLong(); 
            ViewerCircuitCode = bytes.getInt(); 
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            AgentPos = new Vector3(bytes); 
            AgentVel = new Vector3(bytes); 
            Center = new Vector3(bytes); 
            Size = new Vector3(bytes); 
            AtAxis = new Vector3(bytes); 
            LeftAxis = new Vector3(bytes); 
            UpAxis = new Vector3(bytes); 
            ChangedGrid = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Far = bytes.getFloat();
            Aspect = bytes.getFloat();
            length = (int)(bytes.get()) & 0xFF;
            _throttles = new byte[length];
            bytes.get(_throttles); 
            LocomotionState = bytes.getInt(); 
            HeadRotation = new Quaternion(bytes, true); 
            BodyRotation = new Quaternion(bytes, true); 
            ControlFlags = bytes.getInt(); 
            EnergyLevel = bytes.getFloat();
            GodLevel = bytes.get(); 
            AlwaysRun = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            PreyAgent = new UUID(bytes);
            AgentAccess = bytes.get(); 
            length = (int)(bytes.getShort()) & 0xFFFF;
            _agenttextures = new byte[length];
            bytes.get(_agenttextures); 
            ActiveGroupID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(RegionHandle);
            bytes.putInt(ViewerCircuitCode);
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            AgentPos.GetBytes(bytes);
            AgentVel.GetBytes(bytes);
            Center.GetBytes(bytes);
            Size.GetBytes(bytes);
            AtAxis.GetBytes(bytes);
            LeftAxis.GetBytes(bytes);
            UpAxis.GetBytes(bytes);
            bytes.put((byte)((ChangedGrid) ? 1 : 0));
            bytes.putFloat(Far);
            bytes.putFloat(Aspect);
            bytes.put((byte)_throttles.length);
            bytes.put(_throttles);
            bytes.putInt(LocomotionState);
            HeadRotation.GetBytes(bytes);
            BodyRotation.GetBytes(bytes);
            bytes.putInt(ControlFlags);
            bytes.putFloat(EnergyLevel);
            bytes.put(GodLevel);
            bytes.put((byte)((AlwaysRun) ? 1 : 0));
            PreyAgent.GetBytes(bytes);
            bytes.put(AgentAccess);
            bytes.putShort((short)_agenttextures.length);
            bytes.put(_agenttextures);
            ActiveGroupID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "ViewerCircuitCode: " + Integer.toString(ViewerCircuitCode) + "\n";
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "AgentPos: " + AgentPos.toString() + "\n";
                output += "AgentVel: " + AgentVel.toString() + "\n";
                output += "Center: " + Center.toString() + "\n";
                output += "Size: " + Size.toString() + "\n";
                output += "AtAxis: " + AtAxis.toString() + "\n";
                output += "LeftAxis: " + LeftAxis.toString() + "\n";
                output += "UpAxis: " + UpAxis.toString() + "\n";
                output += "ChangedGrid: " + Boolean.toString(ChangedGrid) + "\n";
                output += "Far: " + Float.toString(Far) + "\n";
                output += "Aspect: " + Float.toString(Aspect) + "\n";
                output += Helpers.FieldToString(_throttles, "Throttles") + "\n";
                output += "LocomotionState: " + Integer.toString(LocomotionState) + "\n";
                output += "HeadRotation: " + HeadRotation.toString() + "\n";
                output += "BodyRotation: " + BodyRotation.toString() + "\n";
                output += "ControlFlags: " + Integer.toString(ControlFlags) + "\n";
                output += "EnergyLevel: " + Float.toString(EnergyLevel) + "\n";
                output += "GodLevel: " + Byte.toString(GodLevel) + "\n";
                output += "AlwaysRun: " + Boolean.toString(AlwaysRun) + "\n";
                output += "PreyAgent: " + PreyAgent.toString() + "\n";
                output += "AgentAccess: " + Byte.toString(AgentAccess) + "\n";
                output += Helpers.FieldToString(_agenttextures, "AgentTextures") + "\n";
                output += "ActiveGroupID: " + ActiveGroupID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class GroupDataBlock
    {
        public UUID GroupID = null;
        public long GroupPowers = 0;
        public boolean AcceptNotices = false;

        public int getLength(){
            return 25;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            GroupID = new UUID(bytes);
            GroupPowers = bytes.getLong(); 
            AcceptNotices = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GroupID.GetBytes(bytes);
            bytes.putLong(GroupPowers);
            bytes.put((byte)((AcceptNotices) ? 1 : 0));
        }

        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "GroupPowers: " + Long.toString(GroupPowers) + "\n";
                output += "AcceptNotices: " + Boolean.toString(AcceptNotices) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public GroupDataBlock createGroupDataBlock() {
         return new GroupDataBlock();
    }

    public class AnimationDataBlock
    {
        public UUID Animation = null;
        public UUID ObjectID = null;

        public int getLength(){
            return 32;
        }

        public AnimationDataBlock() { }
        public AnimationDataBlock(ByteBuffer bytes)
        {
            Animation = new UUID(bytes);
            ObjectID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            Animation.GetBytes(bytes);
            ObjectID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- AnimationData --\n";
            try {
                output += "Animation: " + Animation.toString() + "\n";
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AnimationDataBlock createAnimationDataBlock() {
         return new AnimationDataBlock();
    }

    public class GranterBlockBlock
    {
        public UUID GranterID = null;

        public int getLength(){
            return 16;
        }

        public GranterBlockBlock() { }
        public GranterBlockBlock(ByteBuffer bytes)
        {
            GranterID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GranterID.GetBytes(bytes);
        }

        public String toString()
        {
            String output = "-- GranterBlock --\n";
            try {
                output += "GranterID: " + GranterID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public GranterBlockBlock createGranterBlockBlock() {
         return new GranterBlockBlock();
    }

    public class NVPairDataBlock
    {
        private byte[] _nvpairs;
        public byte[] getNVPairs() {
            return _nvpairs;
        }

        public void setNVPairs(byte[] value) throws Exception {
            if (value == null) {
                _nvpairs = null;
            }
            if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _nvpairs = new byte[value.length];
                System.arraycopy(value, 0, _nvpairs, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getNVPairs() != null) { length += 2 + getNVPairs().length; }
            return length;
        }

        public NVPairDataBlock() { }
        public NVPairDataBlock(ByteBuffer bytes)
        {
            int length;
            length = (int)(bytes.getShort()) & 0xFFFF;
            _nvpairs = new byte[length];
            bytes.get(_nvpairs); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_nvpairs.length);
            bytes.put(_nvpairs);
        }

        public String toString()
        {
            String output = "-- NVPairData --\n";
            try {
                output += Helpers.FieldToString(_nvpairs, "NVPairs") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NVPairDataBlock createNVPairDataBlock() {
         return new NVPairDataBlock();
    }

    public class VisualParamBlock
    {
        public byte ParamValue = 0;

        public int getLength(){
            return 1;
        }

        public VisualParamBlock() { }
        public VisualParamBlock(ByteBuffer bytes)
        {
            ParamValue = bytes.get(); 
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(ParamValue);
        }

        public String toString()
        {
            String output = "-- VisualParam --\n";
            try {
                output += "ParamValue: " + Byte.toString(ParamValue) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public VisualParamBlock createVisualParamBlock() {
         return new VisualParamBlock();
    }

    private PacketHeader header;
    public PacketHeader getHeader() { return header; }
    public void setHeader(PacketHeader value) { header = value; }
    public PacketType getType() { return PacketType.ChildAgentUpdate; }
    public AgentDataBlock AgentData;
    public GroupDataBlock[] GroupData;
    public AnimationDataBlock[] AnimationData;
    public GranterBlockBlock[] GranterBlock;
    public NVPairDataBlock[] NVPairData;
    public VisualParamBlock[] VisualParam;

    public ChildAgentUpdatePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)25);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        GroupData = new GroupDataBlock[0];
        AnimationData = new AnimationDataBlock[0];
        GranterBlock = new GranterBlockBlock[0];
        NVPairData = new NVPairDataBlock[0];
        VisualParam = new VisualParamBlock[0];
    }

    public ChildAgentUpdatePacket(ByteBuffer bytes) throws Exception
    {
        int [] a_packetEnd = new int[] { bytes.position()-1 };
        header = new PacketHeader(bytes, a_packetEnd, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        { GroupData[j] = new GroupDataBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        AnimationData = new AnimationDataBlock[count];
        for (int j = 0; j < count; j++)
        { AnimationData[j] = new AnimationDataBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        GranterBlock = new GranterBlockBlock[count];
        for (int j = 0; j < count; j++)
        { GranterBlock[j] = new GranterBlockBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        NVPairData = new NVPairDataBlock[count];
        for (int j = 0; j < count; j++)
        { NVPairData[j] = new NVPairDataBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        VisualParam = new VisualParamBlock[count];
        for (int j = 0; j < count; j++)
        { VisualParam[j] = new VisualParamBlock(bytes); }
     }

    public ChildAgentUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = (int)bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        { GroupData[j] = new GroupDataBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        AnimationData = new AnimationDataBlock[count];
        for (int j = 0; j < count; j++)
        { AnimationData[j] = new AnimationDataBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        GranterBlock = new GranterBlockBlock[count];
        for (int j = 0; j < count; j++)
        { GranterBlock[j] = new GranterBlockBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        NVPairData = new NVPairDataBlock[count];
        for (int j = 0; j < count; j++)
        { NVPairData[j] = new NVPairDataBlock(bytes); }
        count = (int)bytes.get() & 0xFF;
        VisualParam = new VisualParamBlock[count];
        for (int j = 0; j < count; j++)
        { VisualParam[j] = new VisualParamBlock(bytes); }
    }

    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < GroupData.length; j++) { length += GroupData[j].getLength(); }
        length++;
        for (int j = 0; j < AnimationData.length; j++) { length += AnimationData[j].getLength(); }
        length++;
        for (int j = 0; j < GranterBlock.length; j++) { length += GranterBlock[j].getLength(); }
        length++;
        for (int j = 0; j < NVPairData.length; j++) { length += NVPairData[j].getLength(); }
        length++;
        for (int j = 0; j < VisualParam.length; j++) { length += VisualParam[j].getLength(); }
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
        bytes.put((byte)GroupData.length);
        for (int j = 0; j < GroupData.length; j++) { GroupData[j].ToBytes(bytes); }
        bytes.put((byte)AnimationData.length);
        for (int j = 0; j < AnimationData.length; j++) { AnimationData[j].ToBytes(bytes); }
        bytes.put((byte)GranterBlock.length);
        for (int j = 0; j < GranterBlock.length; j++) { GranterBlock[j].ToBytes(bytes); }
        bytes.put((byte)NVPairData.length);
        for (int j = 0; j < NVPairData.length; j++) { NVPairData[j].ToBytes(bytes); }
        bytes.put((byte)VisualParam.length);
        for (int j = 0; j < VisualParam.length; j++) { VisualParam[j].ToBytes(bytes); }
        if (header.AckList.length > 0) {
            header.AcksToBytes(bytes);
        }
        return bytes;
    }

    public String toString()
    {
        String output = "--- ChildAgentUpdate ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < GroupData.length; j++)
        {
            output += GroupData[j].toString() + "\n";
        }
        for (int j = 0; j < AnimationData.length; j++)
        {
            output += AnimationData[j].toString() + "\n";
        }
        for (int j = 0; j < GranterBlock.length; j++)
        {
            output += GranterBlock[j].toString() + "\n";
        }
        for (int j = 0; j < NVPairData.length; j++)
        {
            output += NVPairData[j].toString() + "\n";
        }
        for (int j = 0; j < VisualParam.length; j++)
        {
            output += VisualParam[j].toString() + "\n";
        }
        return output;
    }
}
