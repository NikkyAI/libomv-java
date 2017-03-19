/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2014, Frederick Martian
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

// A 128-bit Universally Unique Identifier, used throughout SL and OpenSim
public class UUID implements Serializable
{
	private static final long serialVersionUID = 1L;

	private byte[] data;

	private static byte[] makeNewGuid()
	{
		Random rand = new Random();
		byte[] guid = new byte[16];
		rand.nextBytes(guid);
		return guid;
	}

	/**
	 * Constructor that creates a new random UUID representation
	 */
	public UUID()
	{
		data = makeNewGuid();
	}

	/**
	 * Constructor that takes a string UUID representation
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 *            <example>UUID("11f8aa9c-b071-4242-836b-13b7abe0d489"
	 *            )</example>
	 */
	public UUID(String string)
	{
		fromString(string);
	}

	/**
	 * Constructor that takes a ByteBuffer containing a UUID
	 * 
	 * @param source
	 *            ByteBuffer containing a 16 byte UUID
	 */
	public UUID(ByteBuffer byteArray)
	{
		data = new byte[16];
		byteArray.get(data);
	}

	/**
	 * Constructor that takes a byte array containing a UUID
	 * 
	 * @param source
	 *            Byte array containing a 16 byte UUID
	 * @param pos
	 *            Beginning offset in the array
	 */
	public UUID(byte[] byteArray)
	{
		this(byteArray, 0);
	}

	public UUID(byte[] byteArray, int pos)
	{
		data = new byte[16];
		System.arraycopy(byteArray, pos, data, 0, Math.min(byteArray.length, 16));
	}

	/**
	 * Constructor that takes an unsigned 64-bit unsigned integer to convert to
	 * a UUID
	 * 
	 * @param val
	 *            64-bit unsigned integer to convert to a UUID
	 */
	public UUID(long value)
	{
		this(value, false);
	}

	public UUID(long value, boolean le)
	{
		data = new byte[16];
		if (le)
		{
			Helpers.UInt64ToBytesL(value, data, 0);
		}
		else
		{
			Helpers.UInt64ToBytesB(value, data, 8);
		}
	}

	public UUID(boolean randomize)
	{
		if (randomize)
		{
			data = makeNewGuid();
		}
		else
		{
			data = new byte[16];
		}
	}

	public UUID(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		// entering with event on START_TAG for the tag name identifying the UUID
		// call nextTag() to proceed to inner <UUID> or <Guid> element
		int eventType = parser.next();
		switch (eventType)
		{
			case XmlPullParser.START_TAG:
				if (parser.getName().equalsIgnoreCase("GUID") || parser.getName().equalsIgnoreCase("UUID"))
				{
					// we got apparently an UUID, try to create it from the string
					fromString(parser.nextText());
				}
				else
				{
					// apperently not an UUID, skip entire element and generate UUID.Zero  
					Helpers.skipElement(parser);
				}
				parser.nextTag(); // Advance to outer end tag
				break;
			case XmlPullParser.TEXT:
				fromString(parser.getText());
				parser.nextTag(); // Advance to end tag
				break;
			case XmlPullParser.END_TAG:
				// empty outer tag, generate UUID.Zero  
			    break;
			default:
	    		throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(), parser, null);
		}
		if (data == null)
			data = new byte[16];
	}

	/**
	 * Copy constructor
	 * 
	 * @param val
	 *            UUID to copy
	 */
	public UUID(UUID val)
	{
		data = new byte[16];
		System.arraycopy(val.data, 0, data, 0, 16);
	}

	/**
	 * Parses a string UUID representation and assigns its value to the object
	 * <example
	 * >uuid.FromString("11f8aa9c-b071-4242-836b-13b7abe0d489")</example>
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 * @return true when successful, false otherwise
	 */
	private boolean fromString(String string)
	{
		// Always create new data array to prevent overwriting byref data
		data = new byte[16];

		if (string.length() >= 38 && string.charAt(0) == '{' && string.charAt(37) == '}')
		{
			string = string.substring(1, 37);
		}
		else if (string.length() > 36)
		{
			string = string.substring(0, 36);
		}
		// Any valid string is now either 32 or 36 bytes long
		if (string.length() == 36 && string.charAt(8) == '-' && string.charAt(13) == '-' && 
				                     string.charAt(18) == '-' && string.charAt(23) == '-')
		{
			string = string.substring(0, 36).replaceAll("-", "");
		}

		// Any valid string contains now only hexadecimal characters in its first 32 bytes	
		if (string.length() >= 32 && string.substring(0, 32).matches("[0-9A-Fa-f]+"))
		{
			try
			{
				for (int i = 0; i < 16; ++i)
				{
					data[i] = (byte) Integer.parseInt(string.substring(i * 2, (i * 2) + 2), 16);
				}
				return true;
			}
			catch (NumberFormatException ex)
			{}
		}
		return false;
	}

	/**
	 * Returns a copy of the raw bytes for this UUID
	 * 
	 * @return A 16 byte array containing this UUID
	 */
	public byte[] getBytes()
	{
		return data;
	}

	/**
	 * Copies the raw bytes for this UUID into a ByteBuffer
	 * 
	 * @param bytes
	 *            The ByteBuffer in which the 16 byte of this UUID are copied
	 */
	public void write(ByteBuffer bytes)
	{
		bytes.put(data);
	}

	/**
	 * Copies the raw bytes for this UUID into an OutputStreaam
	 * 
	 * @param stream
	 *            The OutputStream in which the 16 byte of this UUID are copied
	 */
	public void write(OutputStream stream) throws IOException
	{
		stream.write(data);
	}
	/**
	 * Writes the raw bytes for this UUID to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writeing. Must be
	 *            at least 16 bytes before the end of the array
	 */
	public int toBytes(byte[] dest, int pos)
	{
		int length = Math.min(data.length, dest.length - pos);
		System.arraycopy(data, 0, dest, pos, length);
		return length;
	}

	public long AsLong()
	{
		return AsLong(false);
	}

	public long AsLong(boolean le)
	{
		if (le)
			return Helpers.BytesToUInt64L(data);

		return Helpers.BytesToUInt64B(data);
	}

	/**
	 * Calculate an LLCRC (cyclic redundancy check) for this LLUUID
	 * 
	 * @returns The CRC checksum for this UUID
	 */
	public long CRC()
	{
		long retval = 0;

		retval += ((data[3] << 24) + (data[2] << 16) + (data[1] << 8) + data[0]);
		retval += ((data[7] << 24) + (data[6] << 16) + (data[5] << 8) + data[4]);
		retval += ((data[11] << 24) + (data[10] << 16) + (data[9] << 8) + data[8]);
		retval += ((data[15] << 24) + (data[14] << 16) + (data[13] << 8) + data[12]);

		return retval;
	}

	public static UUID GenerateUUID()
	{
		return new UUID(makeNewGuid());
	}
	
	public void serializeXml(XmlSerializer writer, String namespace, String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
        writer.startTag(namespace, name);
	    writer.startTag(namespace, "UUID").text(toString()).endTag(namespace, "UUID");
        writer.endTag(namespace, name);
	}

	/**
	 * Return a hash code for this UUID
	 * 
	 * @return An integer composed of all the UUID bytes XORed together
	 */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(data);
	}

	/**
	 * Comparison function
	 * 
	 * @param o
	 *            An object to compare to this UUID
	 * @return True if the object is a UUID and both UUIDs are equal
	 */
	@Override
	public boolean equals(Object obj)
	{
		return obj != null && obj instanceof UUID && equals((UUID)obj);
	}

	/**
	 * Comparison function
	 * 
	 * @param uuid
	 *            UUID to compare to
	 * @return True if the UUIDs are equal, otherwise false
	 */
	public boolean equals(UUID uuid)
	{
		if (uuid != null)
		{
			if (uuid.data == null && this.data == null)
				return true;
			
			if (uuid.data != null && this.data != null)
				return Arrays.equals(this.data, uuid.data);
		}
		return false;
	}

	/**
	 * Generate a UUID from a string
	 * 
	 * @param val A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 *            example: UUID.Parse("11f8aa9c-b071-4242-836b-13b7abe0d489")
	 * @returns a new UUID if successful, null otherwise
	 */
	public static UUID parse(String val)
	{
		UUID uuid = new UUID(false);
		if (uuid.fromString(val))
			return uuid;
		return null;
	}

	/**
	 * Generate a UUID from a string
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 * @param result
	 *            Will contain the parsed UUID if successful, otherwise null
	 * @return True if the string was successfully parse, otherwise false
	 *         <example>UUID.TryParse("11f8aa9c-b071-4242-836b-13b7abe0d489",
	 *         result)</example>
	 */
	public static boolean TryParse(String val, RefObject<UUID> result)
	{
		if (val == null || val.length() == 0 || (val.charAt(0) == '{' && val.length() < 38) || (val.length() < 36 && val.length() != 32))
		{
			result.argvalue = UUID.Zero;
			return false;
		}

		try
		{
			result.argvalue = parse(val);
			return true;
		}
		catch (Throwable t)
		{
			result.argvalue = UUID.Zero;
			return false;
		}
	}

	/**
	 * Combine two UUIDs together by taking the MD5 hash of a byte array
	 * containing both UUIDs
	 * 
	 * @param first
	 *            First UUID to combine
	 * @param second
	 *            Second UUID to combine
	 * @return The UUID product of the combination
	 */
	public static UUID Combine(UUID first, UUID second)
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			return null;
		}

		// Construct the buffer that MD5ed
		byte[] input = new byte[32];
		first.toBytes(input, 0);
		second.toBytes(input, 16);
		return new UUID(md.digest(input));
	}

	/**
	 * XOR two UUIDs together
	 * 
	 * @param uuid
	 *            UUID to combine
	 */
	public void XOr(UUID uuid)
	{
		int i = 0;
		for (byte b : uuid.getBytes())
		{
			data[i++] ^= b;
		}
	}

	public static UUID XOr(UUID first, UUID second)
	{
		UUID uuid = new UUID(first);
		uuid.XOr(second);
		return uuid;
	}

	/**
	 * Get a hyphenated string representation of this UUID
	 * 
	 * @return A string representation of this UUID, lowercase and with hyphens
	 *         <example>11f8aa9c-b071-4242-836b-13b7abe0d489</example>
	 */
	@Override
	public String toString()
	{
		if (data == null)
		{
			return ZeroString;
		}

		StringBuffer uuid = new StringBuffer(36);

		for (int i = 0; i < 16; ++i)
		{
			byte value = data[i];
			uuid.append(String.format("%02x", value & 0xFF));
			if (i == 3 || i == 5 || i == 7 || i == 9)
			{
				uuid.append("-");
			}
		}
		return uuid.toString();
	}

	public boolean isZero()
	{
		return equals(Zero);		
	}
	
	public static boolean isZero(UUID uuid)
	{
		if (uuid != null)
			return uuid.equals(Zero);
		return false;
	}
	
	public static boolean isZeroOrNull(UUID uuid)
	{
		if (uuid != null)
			return uuid.equals(Zero);
		return true;
	}

	/** An UUID with a value of all zeroes */
	public final static UUID Zero = new UUID(false);

	/** A cache of UUID.Zero as a string to optimize a common path */
	private static final String ZeroString = Zero.toString();
}
