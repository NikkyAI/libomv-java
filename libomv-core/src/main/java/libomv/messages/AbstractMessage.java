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
package libomv.messages;

import java.util.Date;

import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;

public abstract class AbstractMessage {
	public abstract int getNumberOfBlocks(String blockName) throws Exception;

	public abstract byte getMessageI8(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract short getMessageU8(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract short getMessageI16(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract int getMessageU16(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract int getMessageI32(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract long getMessageU32(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract long getMessageI64(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract long getMessageU64(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract float getMessageF32(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract double getMessageF64(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract String getMessageString(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract UUID getMessageUUID(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract Date getMessageDate(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract Vector3 getMessageVector3(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract Vector3d getMessageVector3d(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract Vector4 getMessageVector4(String blockName, String fieldName, short blockNumber) throws Exception;

	public abstract Quaternion getMessageQuaternion(String blockName, String fieldName, short blockNumber)
			throws Exception;

	public abstract int getNumberOfBlocks(int blockName) throws Exception;

	public abstract byte getMessageI8(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract short getMessageU8(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract short getMessageI16(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract int getMessageU16(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract int getMessageI32(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract long getMessageU32(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract long getMessageI64(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract long getMessageU64(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract float getMessageF32(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract double getMessageF64(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract String getMessageString(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract UUID getMessageUUID(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract Date getMessageDate(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract Vector3 getMessageVector3(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract Vector3d getMessageVector3d(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract Vector4 getMessageVector4(int blockName, int fieldName, short blockNumber) throws Exception;

	public abstract Quaternion getMessageQuaternion(int blockName, int fieldName, short blockNumber) throws Exception;
}
