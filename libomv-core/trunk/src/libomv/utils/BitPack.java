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

public class BitPack
{
    public byte[] Data;

    public int getBytePos()
    {
        if (bytePos != 0 && bitPos == 0)
            return bytePos - 1;
		return bytePos;
    }

    public int getBitPos()
    {
    	return bitPos;
    }

    private int MAX_BITS = 8;
    private static byte[] ON = new byte[] { 1 };
    private static byte[] OFF = new byte[] { 0 };

    private int bytePos;
    private int bitPos;


    /**
     * Default constructor, initialize the bit packer / bit unpacker
     * with a byte array and starting position
     *
     * @param data Byte array to pack bits in to or unpack from
     */
    public BitPack(byte[] data)
    {
        Data = data;
        bytePos = 0;
    }

    /**
     * Default constructor, initialize the bit packer / bit unpacker
     * with a byte array and starting position
     *
     * @param data Byte array to pack bits in to or unpack from
     * @param pos Starting position in the byte array
     */
    public BitPack(byte[] data, int pos)
    {
        Data = data;
        bytePos = pos;
    }

    public byte[] getData()
    {
    	byte[] dest = new byte[getBytePos()];
    	System.arraycopy(Data, 0, dest, 0, getBytePos());
    	return dest;
    }
    
    /**
     * Pack a number of bits from a byte array in to the data
     *
     * @param data byte array to pack
     */
    public void PackBits(byte[] data, int count)
    {
        PackBitArray(data, count);
    }

    /**
     * Pack a number of bits from an integer in to the data
     *
     * @param data integer to pack
     */
    public void PackBits(int data, int count)
    {
        PackBitArray(Helpers.UInt32ToBytesL(data), count);
    }

    /**
     * Pack a floating point value in to the data
     *
     * @param data Floating point value to pack
     */
    public void PackFloat(float data)
    {
        PackBitArray(Helpers.FloatToBytesL(data), 32);
    }

    /**
     * Pack a single bit in to the data
     *
     * @bit Bit to pack
     */
    public void PackBit(boolean bit)
    {
        if (bit)
            PackBitArray(ON, 1);
        else
            PackBitArray(OFF, 1);
    }

    /**
     * Pack a fixed floating point in to the data
     * 
     * @param data
     * @param isSigned
     * @param intBits
     * @param fracBits
     */
    public void PackFixed(float data, boolean isSigned, int intBits, int fracBits)
    {
        int totalBits = intBits + fracBits;
        if (isSigned) totalBits++;
        byte[] dest = new byte[(totalBits + 7) / 8];
        Helpers.FixedToBytesL(dest, 0, data, isSigned, intBits, fracBits);
        PackBitArray(dest, totalBits);
    }

    /**
     * Pack an UUID in to the data
     * 
     * @param data
     */
    public void PackUUID(UUID data)
    {
    	if (bitPos > 0)
    	{
    		bitPos = 0;
    		bytePos++;
    	}
        PackBitArray(data.getBytes(), 128);
    }

    public void PackString(String str) throws UnsupportedEncodingException
    {
    	if (bitPos > 0)
    	{
    		bitPos = 0;
    		bytePos++;
    	}
        PackBitArray(str.getBytes(Helpers.UTF8_ENCODING), str.length());
    }

    /*
     *
     * 
     * @param data
     */
    public void PackColor(Color4 data)
    {
        PackBitArray(data.getBytes(), 32);
    }

    /**
     * Unpacking a floating point value from the data
     *
     * @returns Unpacked floating point value
     */
    public float UnpackFloat()
    {
        return Helpers.BytesToFloatL(UnpackBitsArray(32), 0);
    }

    /**
     * Unpack a variable number of bits from the data in to integer format
     *
     * @param totalCount Number of bits to unpack
     * @returns An integer containing the unpacked bits
     * @remarks This function is only useful up to 32 bits
     */
    public int UnpackBits(int totalCount)
    {
        return Helpers.BytesToInt32L(UnpackBitsArray(totalCount), 0);
    }

    /**
     * Unpack a variable number of bits from the data in to unsigned integer format
     *
     * @param totalCount Number of bits to unpack
     * @returns An unsigned integer containing the unpacked bits
     * @remarks This function is only useful up to 32 bits
     */
    public long UnpackUBits(int totalCount)
    {
        return Helpers.BytesToUInt32L(UnpackBitsArray(totalCount), 0);
    }

    /**
     * Unpack a 16-bit signed integer
     *
     * @returns 16-bit signed integer
     */
    public short UnpackShort()
    {
        return Helpers.BytesToInt16L(UnpackBitsArray(16), 0);
    }

    /**
     * Unpack a 16-bit unsigned integer
     *
     * @returns 16-bit unsigned integer
     */
    public int UnpackUShort()
    {
        return Helpers.BytesToUInt16L(UnpackBitsArray(16), 0);
    }

    /**
     * Unpack a 32-bit signed integer
     *
     * @returns 32-bit signed integer
     */
    public int UnpackInt()
    {
        return Helpers.BytesToInt32L(UnpackBitsArray(32), 0);
    }

    /**
     * Unpack a 32-bit unsigned integer
     *
     * @returns 32-bit unsigned integer
     */
    public long UnpackUInt()
    {
        return Helpers.BytesToUInt32L(UnpackBitsArray(32), 0);
    }

    public byte UnpackByte()
    {
        byte[] output = UnpackBitsArray(8);
        return output[0];
    }

    public float UnpackFixed(boolean signed, int intBits, int fracBits)
    {
        int totalBits = intBits + fracBits;
        if (signed)
        {
            totalBits++;
        }

    	return Helpers.BytesToFixedL(UnpackBitsArray(totalBits), 0, signed, intBits, fracBits);
    }

    public String UnpackString(int size) throws UnsupportedEncodingException
    {
        if (bitPos != 0 || bytePos + size > Data.length) throw new IndexOutOfBoundsException();

        String str = new String(Data, bytePos, size, Helpers.UTF8_ENCODING);
        bytePos += size;
        return str;
    }

    public UUID UnpackUUID()
    {
        if (bitPos != 0) throw new IndexOutOfBoundsException();

        UUID val = new UUID(Data, bytePos);
        bytePos += 16;
        return val;
    }

    private void PackBitArray(byte[] data, int totalCount)
    {
        int count = 0;
        int curBytePos = 0;
        int curBitPos = 0;

        while (totalCount > 0)
        {
            if (totalCount > MAX_BITS)
            {
                count = MAX_BITS;
                totalCount -= MAX_BITS;
            }
            else
            {
                count = totalCount;
                totalCount = 0;
            }

            while (count > 0)
            {
                byte curBit = (byte)(0x80 >> bitPos);

                if ((data[curBytePos] & (0x01 << (count - 1))) != 0)
                    Data[bytePos] |= curBit;
                else
                    Data[bytePos] &= (byte)~curBit;

                --count;
                ++bitPos;
                ++curBitPos;

                if (bitPos >= MAX_BITS)
                {
                    bitPos = 0;
                    ++bytePos;
                }
                if (curBitPos >= MAX_BITS)
                {
                    curBitPos = 0;
                    ++curBytePos;
                }
            }
        }
    }

    private byte[] UnpackBitsArray(int totalCount)
    {
        int count = 0;
        byte[] output = new byte[4];
        int curBytePos = 0;
        int curBitPos = 0;

        while (totalCount > 0)
        {
            if (totalCount > MAX_BITS)
            {
                count = MAX_BITS;
                totalCount -= MAX_BITS;
            }
            else
            {
                count = totalCount;
                totalCount = 0;
            }

            while (count > 0)
            {
                // Shift the previous bits
                output[curBytePos] <<= 1;

                // Grab one bit
                if ((Data[bytePos] & (0x80 >> bitPos++)) != 0)
                    ++output[curBytePos];

                --count;
                ++curBitPos;

                if (bitPos >= MAX_BITS)
                {
                    bitPos = 0;
                    ++bytePos;
                }
                if (curBitPos >= MAX_BITS)
                {
                    curBitPos = 0;
                    ++curBytePos;
                }
            }
        }

        return output;
    }
}
