/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Copyright (c) 2006, Lateral Arts Limited
 * Copyright (c) 2009-2012, Frederick Martian
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
package libomv.types;

import java.nio.ByteBuffer;

import libomv.utils.Helpers;

public class PacketHeader
{
	// This header flag signals that ACKs are appended to the packet
	public final static byte MSG_APPENDED_ACKS = 0x10;

	// This header flag signals that this packet has been sent before
	public final static byte MSG_RESENT = 0x20;

	// This header flags signals that an ACK is expected for this packet
	public final static byte MSG_RELIABLE = 0x40;

	// This header flag signals that the message is compressed using zerocoding
	public final static byte MSG_ZEROCODED = (byte) 0x80;

	public byte[] Data = null;
	public byte[] Extra = null;
	private final byte fixedLen = 6;
	private byte frequency;
	private byte length;

	public byte getFlags()
	{
		return Data[0];
	}

	public void setFlags(byte value)
	{
		Data[0] = value;
	}

	public boolean getReliable()
	{
		return (Data[0] & MSG_RELIABLE) != 0;
	}

	public void setReliable(boolean value)
	{
		if (value)
		{
			Data[0] |= MSG_RELIABLE;
		}
		else
		{
			Data[0] -= MSG_RELIABLE;
		}
	}

	public boolean getResent()
	{
		return (Data[0] & MSG_RESENT) != 0;
	}

	public void setResent(boolean value)
	{
		if (value)
		{
			Data[0] |= MSG_RESENT;
		}
		else
		{
			Data[0] -= MSG_RESENT;
		}
	}

	public boolean getZerocoded()
	{
		return (Data[0] & MSG_ZEROCODED) != 0;
	}

	public void setZerocoded(boolean value)
	{
		if (value)
		{
			Data[0] |= MSG_ZEROCODED;
		}
		else
		{
			Data[0] -= MSG_ZEROCODED;
		}
	}

	public boolean getAppendedAcks()
	{
		return (Data[0] & MSG_APPENDED_ACKS) != 0;
	}

	public void setAppendedAcks(boolean value)
	{
		if (value)
		{
			Data[0] |= MSG_APPENDED_ACKS;
		}
		else
		{
			Data[0] -= MSG_APPENDED_ACKS;
		}
	}

	public int getSequence()
	{
		return (((Data[1] & 0xff) >> 24) + ((Data[2] & 0xff) << 16) + ((Data[3] & 0xff) << 8) + ((Data[4] & 0xff) << 0));
	}

	public int getExtraLength()
	{
		return Data[5];
	}

	public short getID()
	{
		switch (frequency)
		{
			case PacketFrequency.Low:
				return (short)(((Data[8 + getExtraLength()] & 0xFF) << 8) + ((Data[9 + getExtraLength()] & 0xff) << 0));
			case PacketFrequency.Medium:
				return Data[7];
			case PacketFrequency.High:
				return Data[6];
		}
		return 0;
	}

	public void setID(int value)
	{
		switch (frequency)
		{
			case PacketFrequency.Low:
				Data[8 + getExtraLength()] = (byte) ((value >> 8) & 0xFF);
				Data[9 + getExtraLength()] = (byte) ((value >> 0) & 0xFF);
				break;
			case PacketFrequency.Medium:
				Data[7] = (byte) (value & 0xFF);
				break;
			case PacketFrequency.High:
				Data[6] = (byte) (value & 0xFF);
				break;
		}
	}

	public byte getFrequency()
	{
		return frequency;
	}

	private void setFrequency(byte frequency)
	{
		this.frequency = frequency;
		switch (frequency)
		{
			case PacketFrequency.Low:
				this.length = 10;
				break;
			case PacketFrequency.Medium:
				this.length = 8;
				break;
			case PacketFrequency.High:
				this.length = 7;
				break;
		}

	}

	private void BuildHeader(ByteBuffer bytes) throws Exception
	{
		if (bytes.limit() < this.length)
		{
			throw new Exception("Not enough bytes for " + PacketFrequency.Names[frequency] + "Header");
		}
		Data = new byte[this.length];
		bytes.get(Data, 0, fixedLen);
		int extra = getExtraLength();
		if (extra > 0)
		{
			Extra = new byte[extra];
			bytes.get(Extra, 0, extra);
		}
		bytes.get(Data, fixedLen, getLength() - fixedLen);
	}

	// Constructors
	public PacketHeader(byte frequency)
	{
		setFrequency(frequency);
		Data = new byte[this.length];
		Data[5] = (byte) 0;
		switch (frequency)
		{
			case PacketFrequency.Low:
				Data[7] = (byte) 0xFF;
			case PacketFrequency.Medium:
				Data[6] = (byte) 0xFF;
		}
	}

	public PacketHeader(ByteBuffer bytes, byte frequency) throws Exception
	{
		setFrequency(frequency);
		BuildHeader(bytes);
		CreateAckList(bytes);
	}

	public PacketHeader(ByteBuffer bytes) throws Exception
	{
		if (bytes.get(6) == (byte) 0xFF)
		{
			if (bytes.get(7) == (byte) 0xFF)
			{
				setFrequency(PacketFrequency.Low);
			}
			else
			{
				setFrequency(PacketFrequency.Medium);
			}
		}
		else
		{
			setFrequency(PacketFrequency.High);
		}
		BuildHeader(bytes);
		CreateAckList(bytes);
	}

	public byte getLength()
	{
		return length;
	}

	public void ToBytes(ByteBuffer bytes)
	{
		bytes.put(Data, 0, fixedLen - 1);
		if (Extra == null)
		{
			bytes.put((byte) 0);
		}
		else
		{
			bytes.put((byte) (Extra.length & 0xFF));
			bytes.put(Extra);
		}
		bytes.put(Data, fixedLen, getLength() - fixedLen);
	}

	/**
	 * Encode a byte array with zerocoding. Used to compress packets marked with
	 * the zerocoded flag. Any zeroes in the array are compressed down to a
	 * single zero byte followed by a count of how many zeroes to expand out. A
	 * single zero becomes 0x00 0x01, two zeroes becomes 0x00 0x02, three zeroes
	 * becomes 0x00 0x03, etc. The first four bytes are copied directly to the
	 * output buffer.
	 * 
	 * @param src
	 *            The byte buffer to encode
	 * @param dest
	 *            The output byte array to encode to
	 * @return The length of the output buffer
	 */
	public static int zeroEncode(ByteBuffer src, byte[] dest)
	{
		int bodylen, zerolen = 6 + src.get(5);
		byte zerocount = 0;
		int srclen = src.position();

		src.position(0);
		src.get(dest, 0, zerolen);

		if ((src.get(0) & MSG_APPENDED_ACKS) == 0)
		{
			bodylen = srclen;
		}
		else
		{
			bodylen = srclen - src.get(srclen - 1) * 4 - 1;
		}

		int i;
		for (i = zerolen; i < bodylen; i++)
		{
			if (src.get(i) == 0x00)
			{
				zerocount++;

				if (zerocount == 0)
				{
					dest[zerolen++] = 0x00;
					dest[zerolen++] = (byte) 0xff;
					zerocount++;
				}
			}
			else
			{
				if (zerocount != 0)
				{
					dest[zerolen++] = 0x00;
					dest[zerolen++] = zerocount;
					zerocount = 0;
				}
				dest[zerolen++] = src.get(i);
			}
		}

		if (zerocount != 0)
		{
			dest[zerolen++] = 0x00;
			dest[zerolen++] = zerocount;
		}
		// copy appended ACKs
		for (; i < srclen; i++)
		{
			dest[zerolen++] = src.get(i);
		}
		return zerolen;
	}

	public int[] AckList = null;

	private void CreateAckList(ByteBuffer bytes)
	{
		if (getAppendedAcks())
		{
			int packetEnd = bytes.limit() - 1;
			AckList = new int[bytes.get(packetEnd)];
			byte[] array = bytes.array();

			for (int i = AckList.length; i > 0;)
			{
				packetEnd -= 4;
				AckList[--i] = (int) Helpers.BytesToUInt32B(array, packetEnd);
			}
			bytes.limit(packetEnd);
		}
	}
}
