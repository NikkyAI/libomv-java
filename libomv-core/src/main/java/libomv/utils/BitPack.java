/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
package libomv.utils;

import java.io.UnsupportedEncodingException;

import libomv.types.Color4;
import libomv.types.UUID;

public class BitPack {
	public byte[] data;

	public int getBytePos() {
		if (bytePos != 0 && bitPos == 0)
			return bytePos - 1;
		return bytePos;
	}

	public int getBitPos() {
		return bitPos;
	}

	private int MAX_BITS = 8;
	private static byte[] ON = new byte[] { 1 };
	private static byte[] OFF = new byte[] { 0 };

	private int bytePos;
	private int bitPos;

	/**
	 * Default constructor, initialize the bit packer / bit unpacker with a byte
	 * array and starting position
	 *
	 * @param data
	 *            Byte array to pack bits in to or unpack from
	 */
	public BitPack(byte[] data) {
		this.data = data;
		bytePos = 0;
	}

	/**
	 * Default constructor, initialize the bit packer / bit unpacker with a byte
	 * array and starting position
	 *
	 * @param data
	 *            Byte array to pack bits in to or unpack from
	 * @param pos
	 *            Starting position in the byte array
	 */
	public BitPack(byte[] data, int pos) {
		this.data = data;
		bytePos = pos;
	}

	public byte[] getData() {
		byte[] dest = new byte[getBytePos()];
		System.arraycopy(data, 0, dest, 0, getBytePos());
		return dest;
	}

	/**
	 * Pack a number of bits from a byte array in to the data
	 *
	 * @param data
	 *            byte array to pack
	 */
	public void packBits(byte[] data, int count) {
		packBitArray(data, count);
	}

	/**
	 * Pack a number of bits from an integer in to the data
	 *
	 * @param data
	 *            integer to pack
	 */
	public void packBits(int data, int count) {
		packBitArray(Helpers.UInt32ToBytesL(data), count);
	}

	/**
	 * Pack a floating point value in to the data
	 *
	 * @param data
	 *            Floating point value to pack
	 */
	public void packFloat(float data) {
		packBitArray(Helpers.floatToBytesL(data), 32);
	}

	/**
	 * Pack a single bit in to the data
	 *
	 * @bit Bit to pack
	 */
	public void packBit(boolean bit) {
		if (bit)
			packBitArray(ON, 1);
		else
			packBitArray(OFF, 1);
	}

	/**
	 * Pack a fixed floating point in to the data
	 *
	 * @param data
	 * @param isSigned
	 * @param intBits
	 * @param fracBits
	 */
	public void packFixed(float data, boolean isSigned, int intBits, int fracBits) {
		int totalBits = intBits + fracBits;
		if (isSigned)
			totalBits++;
		byte[] dest = new byte[(totalBits + 7) / 8];
		Helpers.fixedToBytesL(dest, 0, data, isSigned, intBits, fracBits);
		packBitArray(dest, totalBits);
	}

	/**
	 * Pack an UUID in to the data
	 *
	 * @param data
	 */
	public void packUUID(UUID data) {
		if (bitPos > 0) {
			bitPos = 0;
			bytePos++;
		}
		packBitArray(data.getBytes(), 128);
	}

	public void packString(String str) throws UnsupportedEncodingException {
		if (bitPos > 0) {
			bitPos = 0;
			bytePos++;
		}
		packBitArray(str.getBytes(Helpers.UTF8_ENCODING), str.length());
	}

	/*
	 *
	 *
	 * @param data
	 */
	public void packColor(Color4 data) {
		packBitArray(data.getBytes(), 32);
	}

	/**
	 * Unpacking a floating point value from the data
	 *
	 * @returns Unpacked floating point value
	 */
	public float unpackFloat() {
		return Helpers.BytesToFloatL(unpackBitsArray(32), 0);
	}

	/**
	 * Unpack a variable number of bits from the data in to integer format
	 *
	 * @param totalCount
	 *            Number of bits to unpack
	 * @returns An integer containing the unpacked bits
	 * @remarks This function is only useful up to 32 bits
	 */
	public int unpackBits(int totalCount) {
		return Helpers.BytesToInt32L(unpackBitsArray(totalCount), 0);
	}

	/**
	 * Unpack a variable number of bits from the data in to unsigned integer format
	 *
	 * @param totalCount
	 *            Number of bits to unpack
	 * @returns An unsigned integer containing the unpacked bits
	 * @remarks This function is only useful up to 32 bits
	 */
	public long unpackUBits(int totalCount) {
		return Helpers.BytesToUInt32L(unpackBitsArray(totalCount), 0);
	}

	/**
	 * Unpack a 16-bit signed integer
	 *
	 * @returns 16-bit signed integer
	 */
	public short unpackShort() {
		return Helpers.BytesToInt16L(unpackBitsArray(16), 0);
	}

	/**
	 * Unpack a 16-bit unsigned integer
	 *
	 * @returns 16-bit unsigned integer
	 */
	public int unpackUShort() {
		return Helpers.BytesToUInt16L(unpackBitsArray(16), 0);
	}

	/**
	 * Unpack a 32-bit signed integer
	 *
	 * @returns 32-bit signed integer
	 */
	public int unpackInt() {
		return Helpers.BytesToInt32L(unpackBitsArray(32), 0);
	}

	/**
	 * Unpack a 32-bit unsigned integer
	 *
	 * @returns 32-bit unsigned integer
	 */
	public long unpackUInt() {
		return Helpers.BytesToUInt32L(unpackBitsArray(32), 0);
	}

	public byte unpackByte() {
		byte[] output = unpackBitsArray(8);
		return output[0];
	}

	public float unpackFixed(boolean signed, int intBits, int fracBits) {
		int totalBits = intBits + fracBits;
		if (signed) {
			totalBits++;
		}

		return Helpers.BytesToFixedL(unpackBitsArray(totalBits), 0, signed, intBits, fracBits);
	}

	public String unpackString(int size) throws UnsupportedEncodingException {
		if (bitPos != 0 || bytePos + size > data.length)
			throw new IndexOutOfBoundsException();

		String str = new String(data, bytePos, size, Helpers.UTF8_ENCODING);
		bytePos += size;
		return str;
	}

	public UUID unpackUUID() {
		if (bitPos != 0)
			throw new IndexOutOfBoundsException();

		UUID val = new UUID(data, bytePos);
		bytePos += 16;
		return val;
	}

	private void packBitArray(byte[] data, int totalCount) {
		int count = 0;
		int curBytePos = 0;
		int curBitPos = 0;

		while (totalCount > 0) {
			if (totalCount > MAX_BITS) {
				count = MAX_BITS;
				totalCount -= MAX_BITS;
			} else {
				count = totalCount;
				totalCount = 0;
			}

			while (count > 0) {
				byte curBit = (byte) (0x80 >> bitPos);

				if ((data[curBytePos] & (0x01 << (count - 1))) != 0)
					this.data[bytePos] |= curBit;
				else
					this.data[bytePos] &= (byte) ~curBit;

				--count;
				++bitPos;
				++curBitPos;

				if (bitPos >= MAX_BITS) {
					bitPos = 0;
					++bytePos;
				}
				if (curBitPos >= MAX_BITS) {
					curBitPos = 0;
					++curBytePos;
				}
			}
		}
	}

	private byte[] unpackBitsArray(int totalCount) {
		int count = 0;
		byte[] output = new byte[4];
		int curBytePos = 0;
		int curBitPos = 0;

		while (totalCount > 0) {
			if (totalCount > MAX_BITS) {
				count = MAX_BITS;
				totalCount -= MAX_BITS;
			} else {
				count = totalCount;
				totalCount = 0;
			}

			while (count > 0) {
				// Shift the previous bits
				output[curBytePos] <<= 1;

				// Grab one bit
				if ((data[bytePos] & (0x80 >> bitPos++)) != 0)
					++output[curBytePos];

				--count;
				++curBitPos;

				if (bitPos >= MAX_BITS) {
					bitPos = 0;
					++bytePos;
				}
				if (curBitPos >= MAX_BITS) {
					curBitPos = 0;
					++curBytePos;
				}
			}
		}

		return output;
	}
}
