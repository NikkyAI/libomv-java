/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
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

public class PacketHeader {
	// This header flag signals that ACKs are appended to the packet
	public static final byte MSG_APPENDED_ACKS = 0x10;

	// This header flag signals that this packet has been sent before
	public static final byte MSG_RESENT = 0x20;

	// This header flags signals that an ACK is expected for this packet
	public static final byte MSG_RELIABLE = 0x40;

	// This header flag signals that the message is compressed using zerocoding
	public static final byte MSG_ZEROCODED = (byte) 0x80;

	public byte[] data = null;
	public byte[] extra = null;
	private final byte fixedLen = 6;
	private byte frequency;
	private byte length;
	public int[] ackList = null;

	// Constructors
	public PacketHeader(byte frequency) {
		setFrequency(frequency);
		data = new byte[this.length];
		data[5] = (byte) 0;
		switch (frequency) {
		case PacketFrequency.LOW:
			data[7] = (byte) 0xFF;
		case PacketFrequency.MEDIUM:
			data[6] = (byte) 0xFF;
		default:
			break;
		}
	}

	public PacketHeader(ByteBuffer bytes, byte frequency) throws Exception {
		setFrequency(frequency);
		buildHeader(bytes);
		createAckList(bytes);
	}

	public PacketHeader(ByteBuffer bytes) throws Exception {
		if (bytes.get(6) == (byte) 0xFF) {
			if (bytes.get(7) == (byte) 0xFF) {
				setFrequency(PacketFrequency.LOW);
			} else {
				setFrequency(PacketFrequency.MEDIUM);
			}
		} else {
			setFrequency(PacketFrequency.HIGH);
		}
		buildHeader(bytes);
		createAckList(bytes);
	}

	public byte getFlags() {
		return data[0];
	}

	public void setFlags(byte value) {
		data[0] = value;
	}

	public boolean getReliable() {
		return (data[0] & MSG_RELIABLE) != 0;
	}

	public void setReliable(boolean value) {
		if (value) {
			data[0] |= MSG_RELIABLE;
		} else {
			data[0] -= MSG_RELIABLE;
		}
	}

	public boolean getResent() {
		return (data[0] & MSG_RESENT) != 0;
	}

	public void setResent(boolean value) {
		if (value) {
			data[0] |= MSG_RESENT;
		} else {
			data[0] -= MSG_RESENT;
		}
	}

	public boolean getZerocoded() {
		return (data[0] & MSG_ZEROCODED) != 0;
	}

	public void setZerocoded(boolean value) {
		if (value) {
			data[0] |= MSG_ZEROCODED;
		} else {
			data[0] -= MSG_ZEROCODED;
		}
	}

	public boolean getAppendedAcks() {
		return (data[0] & MSG_APPENDED_ACKS) != 0;
	}

	public void setAppendedAcks(boolean value) {
		if (value) {
			data[0] |= MSG_APPENDED_ACKS;
		} else {
			data[0] -= MSG_APPENDED_ACKS;
		}
	}

	public int getSequence() {
		return ((data[1] & 0xff) >> 24) + ((data[2] & 0xff) << 16) + ((data[3] & 0xff) << 8) + ((data[4] & 0xff) << 0);
	}

	public int getExtraLength() {
		return data[5];
	}

	public short getID() {
		switch (frequency) {
		case PacketFrequency.LOW:
			return (short) (((data[8 + getExtraLength()] & 0xFF) << 8) + ((data[9 + getExtraLength()] & 0xff) << 0));
		case PacketFrequency.MEDIUM:
			return data[7];
		case PacketFrequency.HIGH:
			return data[6];
		default:
			break;
		}
		return 0;
	}

	public void setID(int value) {
		switch (frequency) {
		case PacketFrequency.LOW:
			data[8 + getExtraLength()] = (byte) ((value >> 8) & 0xFF);
			data[9 + getExtraLength()] = (byte) ((value >> 0) & 0xFF);
			break;
		case PacketFrequency.MEDIUM:
			data[7] = (byte) (value & 0xFF);
			break;
		case PacketFrequency.HIGH:
			data[6] = (byte) (value & 0xFF);
			break;
		default:
			break;
		}
	}

	public byte getFrequency() {
		return frequency;
	}

	private void setFrequency(byte frequency) {
		this.frequency = frequency;
		switch (frequency) {
		case PacketFrequency.LOW:
			this.length = 10;
			break;
		case PacketFrequency.MEDIUM:
			this.length = 8;
			break;
		case PacketFrequency.HIGH:
			this.length = 7;
			break;
		default:
			break;
		}

	}

	private void buildHeader(ByteBuffer bytes) throws Exception {
		if (bytes.limit() < this.length) {
			throw new Exception("Not enough bytes for " + PacketFrequency.NAMES[frequency] + "Header");
		}
		data = new byte[this.length];
		bytes.get(data, 0, fixedLen);
		int len = getExtraLength();
		if (len > 0) {
			extra = new byte[len];
			bytes.get(extra, 0, len);
		}
		bytes.get(data, fixedLen, getLength() - fixedLen);
	}

	public byte getLength() {
		return length;
	}

	public void toBytes(ByteBuffer bytes) {
		bytes.put(data, 0, fixedLen - 1);
		if (extra == null) {
			bytes.put((byte) 0);
		} else {
			bytes.put((byte) (extra.length & 0xFF));
			bytes.put(extra);
		}
		bytes.put(data, fixedLen, getLength() - fixedLen);
	}

	/**
	 * Encode a byte array with zerocoding. Used to compress packets marked with the
	 * zerocoded flag. Any zeroes in the array are compressed down to a single zero
	 * byte followed by a count of how many zeroes to expand out. A single zero
	 * becomes 0x00 0x01, two zeroes becomes 0x00 0x02, three zeroes becomes 0x00
	 * 0x03, etc. The first four bytes are copied directly to the output buffer.
	 *
	 * @param src
	 *            The byte buffer to encode
	 * @param dest
	 *            The output byte array to encode to
	 * @return The length of the output buffer
	 */
	public static int zeroEncode(ByteBuffer src, byte[] dest) {
		int bodylen;
		int zerolen = 6 + src.get(5);
		byte zerocount = 0;
		int srclen = src.position();

		src.position(0);
		src.get(dest, 0, zerolen);

		if ((src.get(0) & MSG_APPENDED_ACKS) == 0) {
			bodylen = srclen;
		} else {
			bodylen = srclen - src.get(srclen - 1) * 4 - 1;
		}

		int i;
		for (i = zerolen; i < bodylen; i++) {
			if (src.get(i) == 0x00) {
				zerocount++;

				if (zerocount == 0) {
					dest[zerolen++] = 0x00;
					dest[zerolen++] = (byte) 0xff;
					zerocount++;
				}
			} else {
				if (zerocount != 0) {
					dest[zerolen++] = 0x00;
					dest[zerolen++] = zerocount;
					zerocount = 0;
				}
				dest[zerolen++] = src.get(i);
			}
		}

		if (zerocount != 0) {
			dest[zerolen++] = 0x00;
			dest[zerolen++] = zerocount;
		}
		// copy appended ACKs
		for (; i < srclen; i++) {
			dest[zerolen++] = src.get(i);
		}
		return zerolen;
	}

	private void createAckList(ByteBuffer bytes) {
		if (getAppendedAcks()) {
			int packetEnd = bytes.limit() - 1;
			ackList = new int[bytes.get(packetEnd)];
			byte[] array = bytes.array();

			for (int i = ackList.length; i > 0;) {
				packetEnd -= 4;
				ackList[--i] = (int) Helpers.bytesToUInt32B(array, packetEnd);
			}
			bytes.limit(packetEnd);
		}
	}
}
