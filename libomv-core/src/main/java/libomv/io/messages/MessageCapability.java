/**
 * Copyright (c) 2011-2017, Frederick Martian
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
package libomv.io.messages;

import java.util.Date;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.io.GridClient;
import libomv.messages.AbstractMessage;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;

public class MessageCapability extends AbstractMessage {
	private GridClient client;
	private OSDMap message;

	public MessageCapability(GridClient client) {
		this.client = client;
	}

	@Override
	public int getNumberOfBlocks(String blockName) throws Exception {
		OSD osd = message.get(blockName);
		return osd.getType() == OSDType.Array ? ((OSDArray) osd).size() : 0;
	}

	private OSD getField(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = message.get(blockName);
		if (osd.getType() == OSDType.Array)
			osd = ((OSDArray) osd).get(blockNumber);
		if (osd.getType() != OSDType.Map)
			throw new Exception("Expected a Map of fields but got instead: " + osd.getType());
		return ((OSDMap) osd).get(fieldName);
	}

	@Override
	public byte getMessageI8(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return (byte) osd.asInteger();
	}

	@Override
	public short getMessageU8(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return (short) osd.asUInteger();
	}

	@Override
	public short getMessageI16(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return (short) osd.asInteger();
	}

	@Override
	public int getMessageU16(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asUInteger();
	}

	@Override
	public int getMessageI32(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asInteger();
	}

	@Override
	public long getMessageU32(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asULong();
	}

	@Override
	public long getMessageI64(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asLong();
	}

	@Override
	public long getMessageU64(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asULong();
	}

	@Override
	public float getMessageF32(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return (float) osd.asReal();
	}

	@Override
	public double getMessageF64(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asReal();
	}

	@Override
	public String getMessageString(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asString();
	}

	@Override
	public UUID getMessageUUID(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asUUID();
	}

	@Override
	public Date getMessageDate(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asDate();
	}

	@Override
	public Vector3 getMessageVector3(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asVector3();
	}

	@Override
	public Vector3d getMessageVector3d(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asVector3d();
	}

	@Override
	public Vector4 getMessageVector4(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asVector4();
	}

	@Override
	public Quaternion getMessageQuaternion(String blockName, String fieldName, short blockNumber) throws Exception {
		OSD osd = getField(blockName, fieldName, blockNumber);
		return osd.asQuaternion();
	}

	@Override
	public int getNumberOfBlocks(int blockName) throws Exception {
		return getNumberOfBlocks(client.protocol.keywordPosition(blockName));
	}

	@Override
	public byte getMessageI8(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageI8(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public short getMessageU8(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageU8(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public short getMessageI16(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageI16(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public int getMessageU16(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageU16(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public int getMessageI32(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageI32(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public long getMessageU32(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageU32(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public long getMessageI64(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageI64(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public long getMessageU64(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageU64(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public float getMessageF32(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageF32(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public double getMessageF64(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageF64(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public String getMessageString(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageString(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public UUID getMessageUUID(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageUUID(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public Date getMessageDate(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageDate(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public Vector3 getMessageVector3(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageVector3(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public Vector3d getMessageVector3d(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageVector3d(client.protocol.keywordPosition(blockName),
				client.protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Vector4 getMessageVector4(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageVector4(client.protocol.keywordPosition(blockName), client.protocol.keywordPosition(fieldName),
				blockNumber);
	}

	@Override
	public Quaternion getMessageQuaternion(int blockName, int fieldName, short blockNumber) throws Exception {
		return getMessageQuaternion(client.protocol.keywordPosition(blockName),
				client.protocol.keywordPosition(fieldName), blockNumber);
	}
}
