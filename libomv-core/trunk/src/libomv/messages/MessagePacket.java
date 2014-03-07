/**
 * Copyright (c) 2011-2014, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package libomv.messages;

import java.util.Date;

import libomv.GridClient;
import libomv.ProtocolManager.FieldType;
import libomv.ProtocolManager.MapField;
import libomv.ProtocolManager.MapPacket;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;
import libomv.utils.Helpers;

public class MessagePacket extends AbstractMessage
{
	private GridClient client;
	private byte[] message;
	private MapPacket packet;
	
	public MessagePacket(GridClient client, byte[] message, MapPacket packet)
	{
		this.client = client;
		this.message = message;
		this.packet = packet;
	}
	
	@Override
	public int getNumberOfBlocks(String blockName) throws Exception
	{
		return getNumberOfBlocks(client.Protocol.keywordPosition(blockName));
	}

	@Override
	public byte getMessageI8(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageI8(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public short getMessageU8(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageU8(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public short getMessageI16(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageI16(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public int getMessageU16(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageU16(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}
	
	@Override
	public int getMessageI32(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageI32(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public long getMessageU32(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageU32(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public long getMessageI64(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageI64(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public long getMessageU64(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageU64(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public float getMessageF32(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageF32(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public double getMessageF64(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageF64(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public String getMessageString(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageString(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public UUID getMessageUUID(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageUUID(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public Date getMessageDate(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageDate(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public Vector3 getMessageVector3(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageVector3(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public Vector3d getMessageVector3d(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageVector3d(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public Vector4 getMessageVector4(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageVector4(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public Quaternion getMessageQuaternion(String blockName, String fieldName, short blockNumber) throws Exception
	{
		return getMessageQuaternion(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(blockName), blockNumber);
	}

	@Override
	public int getNumberOfBlocks(int blockName) throws Exception
	{
		return client.Protocol.getBlockNum(packet, message, blockName);
	}

	@Override
	public byte getMessageI8(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.I8)
			throw new Exception("Expected I8, got " + field.type);
		return message[field.offset];
	}

	@Override
	public short getMessageU8(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.U8)
			throw new Exception("Expected U8, got " + field.type);
		return (short)(message[field.offset] & 0xFF);
	}

	@Override
	public short getMessageI16(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.I16)
			throw new Exception("Expected I16, got " + field.type);
		return Helpers.BytesToInt16L(message, field.offset);
	}

	@Override
	public int getMessageU16(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.U16)
			throw new Exception("Expected U16, got " + field.type);
		return Helpers.BytesToUInt16L(message, field.offset);
	}

	@Override
	public int getMessageI32(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.I32)
			throw new Exception("Expected I32, got " + field.type);
		return Helpers.BytesToInt32L(message, field.offset);
	}

	@Override
	public long getMessageU32(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.U32)
			throw new Exception("Expected U32, got " + field.type);
		return Helpers.BytesToUInt32L(message, field.offset);
	}

	@Override
	public long getMessageI64(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.I64)
			throw new Exception("Expected I64, got " + field.type);
		return Helpers.BytesToInt64L(message, field.offset);
	}

	@Override
	public long getMessageU64(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.U64)
			throw new Exception("Expected U64, got " + field.type);
		return Helpers.BytesToUInt64L(message, field.offset);
	}

	@Override
	public float getMessageF32(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.F32)
			throw new Exception("Expected F32, got " + field.type);
		return Helpers.BytesToFloatL(message, field.offset);
	}

	@Override
	public double getMessageF64(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.F64)
			throw new Exception("Expected F64, got " + field.type);
		return Helpers.BytesToDoubleL(message, field.offset);
	}

	@Override
	public String getMessageString(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.Variable)
			throw new Exception("Expected Variable type (String), got " + field.type);
		int length = 0;
		if (field.count == 1)
			length = message[field.offset] & 0xFF;
		else if (field.count == 2)
			length = Helpers.BytesToUInt16L(message, field.offset);
		return Helpers.BytesToString(message, field.offset, length);
	}

	
	@Override
	public UUID getMessageUUID(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.UUID)
			throw new Exception("Expected UUID, got " + field.type);
		return new UUID(message, field.offset);
	}

	@Override
	public Date getMessageDate(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.U32)
			throw new Exception("Expected U32, got " + field.type);
		long date = Helpers.BytesToUInt32L(message, field.offset);
		return Helpers.UnixTimeToDateTime(date);
	}

	@Override
	public Vector3 getMessageVector3(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.Vector3)
			throw new Exception("Expected Vector3, got " + field.type);
		return new Vector3(message, field.offset, true);
	}

	@Override
	public Vector3d getMessageVector3d(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.Vector3d)
			throw new Exception("Expected Vector3d, got " + field.type);
		return new Vector3d(message, field.offset, true);
	}

	@Override
	public Vector4 getMessageVector4(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.Vector4)
			throw new Exception("Expected Vector4, got " + field.type);
		return new Vector4(message, field.offset, true);
	}

	@Override
	public Quaternion getMessageQuaternion(int blockName, int fieldName, short blockNumber) throws Exception
	{
		MapField field = client.Protocol.getFieldOffset(packet, message, blockName, fieldName, blockNumber);	
		if (field.type != FieldType.Quaternion)
			throw new Exception("Expected Quaternion, got " + field.type);
		return new Quaternion(message, field.offset, false, true);
	}
	
 }
