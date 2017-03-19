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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.commons.io.input.SwappedDataInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;

public class Vector4
{
	public float X;

	public float Y;

	public float Z;

	public float S;

	public Vector4()
	{
		X = Y = Z = S = 0;
	}

	public Vector4(float val)
	{
		X = Y = Z = S = val;
	}

	public Vector4(ByteBuffer byteArray)
	{
		X = byteArray.getFloat();
		Y = byteArray.getFloat();
		Z = byteArray.getFloat();
		S = byteArray.getFloat();
	}

	public Vector4(float x, float y, float z, float s)
	{
		X = x;
		Y = y;
		Z = z;
		S = s;
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector4(XmlPullParser parser) throws XmlPullParserException, IOException
    {
		// entering with event on START_TAG for the tag name identifying the Vector3
    	int eventType = parser.getEventType();
    	if (eventType != XmlPullParser.START_TAG)
    		throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(), parser, null);
    	
   		while (parser.nextTag() == XmlPullParser.START_TAG)
   		{
   			String name = parser.getName();
   			if (name.equalsIgnoreCase("X"))
   			{
				X = Helpers.TryParseFloat(parser.nextText().trim());
   			}
   			else if (name.equalsIgnoreCase("Y"))
   			{
				Y = Helpers.TryParseFloat(parser.nextText().trim());
   			}
   			else if (name.equalsIgnoreCase("Z"))
   			{
				Z= Helpers.TryParseFloat(parser.nextText().trim());
   			}
			else if (name.equalsIgnoreCase("S"))
			{
				S = Helpers.TryParseFloat(parser.nextText().trim());
			}
   			else
   			{
   				Helpers.skipElement(parser);
   			}
    	}
    }

	public Vector4(byte[] dest, int pos)
	{
		X = Y = Z = S = 0;
		fromBytes(dest, pos, false);
	}

	public Vector4(byte[] dest, int pos, boolean le)
	{
		X = Y = Z = S = 0;
		fromBytes(dest, pos, le);
	}

	public Vector4(Vector4 v)
	{
		X = v.X;
		Y = v.Y;
		Z = v.Z;
		S = v.S;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 * 
	 * @param byteArray buffer to copy the 16 bytes for X, Y, Z, and S
	 * @param le True for writing little endian data
	 * @throws IOException 
	 */
	public void write(ByteBuffer byteArray)
	{
		byteArray.putFloat(X);
		byteArray.putFloat(Y);
		byteArray.putFloat(Z);
		byteArray.putFloat(S);
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 * 
	 * @param stream OutputStream to copy the 16 bytes for X, Y, Z, and S
	 * @param le True for writing little endian data
	 * @throws IOException 
	 */
	public void write(OutputStream stream, boolean le) throws IOException
	{
		if (le)
		{
			stream.write(Helpers.FloatToBytesL(X));
			stream.write(Helpers.FloatToBytesL(Y));
			stream.write(Helpers.FloatToBytesL(Z));
			stream.write(Helpers.FloatToBytesL(S));
		}
		else
		{
			stream.write(Helpers.FloatToBytesB(X));
			stream.write(Helpers.FloatToBytesB(Y));
			stream.write(Helpers.FloatToBytesB(Z));
			stream.write(Helpers.FloatToBytesB(S));
		}
	}

	/**
	 * Initializes a vector from a float array
	 * 
	 * @param vec
	 *           the vector to intialize
	 * @param arr
	 *            is the float array
	 * @param pos
	 *            Beginning position in the float array
	 */
	public static Vector4 fromArray(Vector4 vec, float[] arr, int pos)
	{
		if (arr.length >= (pos + 4))
		{		
			vec.X = arr[pos + 0];
			vec.Y = arr[pos + 1];
			vec.Z = arr[pos + 2];
			vec.S = arr[pos + 3];
		}
		return vec;
	}

	/**
	 * Builds a vector from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing a 12 byte vector
	 * @param pos
	 *            Beginning position in the byte array
	 * @param le
	 *            is the byte array in little endian format
	 */
	public void fromBytes(byte[] bytes, int pos, boolean le)
	{
		if (le)
		{
			/* Little endian architecture */
			X = Helpers.BytesToFloatL(bytes, pos + 0);
			Y = Helpers.BytesToFloatL(bytes, pos + 4);
			Z = Helpers.BytesToFloatL(bytes, pos + 8);
			S = Helpers.BytesToFloatL(bytes, pos + 12);
		}
		else
		{
			X = Helpers.BytesToFloatB(bytes, pos + 0);
			Y = Helpers.BytesToFloatB(bytes, pos + 4);
			Z = Helpers.BytesToFloatB(bytes, pos + 8);
			S = Helpers.BytesToFloatB(bytes, pos + 12);
		}
	}

	/**
	 * Builds a vector from a data stream
	 * 
	 * @param is
	 *            DataInputStream to read the vector from
	 * @throws IOException 
	 */
	public void fromBytes(DataInputStream is) throws IOException
	{
		X = is.readFloat();
		Y = is.readFloat();
		Z = is.readFloat();
	}

	public void fromBytes(SwappedDataInputStream is) throws IOException
	{
		X = is.readFloat();
		Y = is.readFloat();
		Z = is.readFloat();
	}

	/**
	 * Serializes this vector into four bytes in a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest)
	{
		return toBytes(dest, 0, false);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest Destination byte array
	 * @param pos Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest, int pos)
	{
		return toBytes(dest, pos, false);
	}
	
	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest Destination byte array
	 * @param pos Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest, int pos, boolean le)
	{
		if (le)
		{
			pos += Helpers.FloatToBytesL(X, dest, pos);
			pos += Helpers.FloatToBytesL(Y, dest, pos);
			pos += Helpers.FloatToBytesL(Z, dest, pos);
			pos += Helpers.FloatToBytesL(S, dest, pos);
		}
		else
		{
			pos += Helpers.FloatToBytesB(X, dest, pos);
			pos += Helpers.FloatToBytesB(Y, dest, pos);
			pos += Helpers.FloatToBytesB(Z, dest, pos);
			pos += Helpers.FloatToBytesB(S, dest, pos);
		}
		return 16;
	}

	static public Vector4 parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector4(parser);
	}
	
	public void serializeXml(XmlSerializer writer, String namespace, String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(Z)).endTag(namespace, "Z");
		writer.startTag(namespace, "S").text(Float.toString(S)).endTag(namespace, "S");
		writer.startTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", Z)).endTag(namespace, "Z");
		writer.startTag(namespace, "S").text(String.format(locale, "%f", S)).endTag(namespace, "S");
		writer.startTag(namespace, name);
	}

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%.3f, %.3f, %.3f, %.3f>", X, Y, Z, S);
	}

	public boolean equals(Vector4 val)
	{
		return val != null && X == val.X && Y == val.Y && Z == val.Z && S == val.S;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj != null && (obj instanceof Vector4) && equals((Vector4)obj);
	}

	@Override
	public int hashCode()
	{
		int hashCode = ((Float)X).hashCode();
		hashCode = hashCode * 31 + ((Float)Y).hashCode();
		hashCode = hashCode * 31 + ((Float)Z).hashCode();
		hashCode = hashCode * 31 + ((Float)S).hashCode();
		return  hashCode;
	}

	/** A vector with a value of 0,0,0,0 */
	public final static Vector4 Zero = new Vector4(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector4 One = new Vector4(1f, 1f, 1f, 1f);
	/** A unit vector facing forward (X axis), value 1,0,0,0 */
	public final static Vector4 UnitX = new Vector4(1f, 0f, 0f, 0f);
	/** A unit vector facing left (Y axis), value 0,1,0,0 */
	public final static Vector4 UnitY = new Vector4(0f, 1f, 0f, 0f);
	/** A unit vector facing up (Z axis), value 0,0,1,0 */
	public final static Vector4 UnitZ = new Vector4(0f, 0f, 1f, 0f);
	/** A unit vector facing up (S axis), value 0,0,0,1 */
	public final static Vector4 UnitS = new Vector4(0f, 0f, 0f, 1f);
}
