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
import libomv.utils.RefObject;

/** A two-dimensional vector with floating-point values */
public final class Vector2
{
	/** X value */
	public float X;
	/** Y value */
	public float Y;

	/** Simple Constructors */
	public Vector2()
	{
		X = Y = 0.0f;
	}

	public Vector2(float x, float y)
	{
		X = x;
		Y = y;
	}

	public Vector2(float value)
	{
		X = value;
		Y = value;
	}

	public Vector2(Vector2 vector)
	{
		X = vector.X;
		Y = vector.Y;
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector2(XmlPullParser parser) throws XmlPullParserException, IOException
    {
		// entering with event on START_TAG for the tag name identifying the Quaternion
    	int eventType = parser.getEventType();
    	if (eventType != XmlPullParser.START_TAG)
    		throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(), parser, null);
    	
   		while (parser.nextTag() == XmlPullParser.START_TAG)
   		{
			String name = parser.getName();
			if (name.equals("X"))
			{
				X = Helpers.TryParseFloat(parser.nextText().trim());
			}
			else if (name.equals("Y"))
			{
				Y = Helpers.TryParseFloat(parser.nextText().trim());
			}
			else
			{
				Helpers.skipElement(parser);
			}
    	}
    }

    /**
	 * Constructor, builds a vector from a data stream
	 * 
	 * @param is
	 *            Data stream to read the binary data from
     * @throws IOException 
	 */
    public Vector2(DataInputStream is) throws IOException
    {
		X = Y = 0f;
		fromBytes(is);
    }
    
    public Vector2(SwappedDataInputStream is) throws IOException
    {
		X = Y = 0f;
		fromBytes(is);
    }

    /**
	 * Constructor, builds a vector from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing two four-byte floats
	 * @param pos
	 *            Beginning position in the byte array
	 */
	public Vector2(byte[] byteArray, int pos)
	{
		X = Y = 0f;
		fromBytes(byteArray, pos, false);
	}

	public Vector2(byte[] byteArray, int pos, boolean le)
	{
		X = Y = 0f;
		fromBytes(byteArray, pos, le);
	}

	/**
	 * Constructor, builds a vector from a byte array
	 * 
	 * @param byteArray
	 *            ByteBuffer containing three four-byte floats
	 */
	public Vector2(ByteBuffer byteArray)
	{
		X = byteArray.getFloat();
		Y = byteArray.getFloat();
	}

	/**
	 * Returns the raw bytes for this vector
	 * 
	 * @return An eight-byte array containing X and Y
	 */
	public byte[] getBytes()
	{
		byte[] byteArray = new byte[8];
		toBytes(byteArray, 0, false);
		return byteArray;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 * 
	 * @param byteArray Buffer to copy the 8 bytes for X and Y
	 */
	public void write(ByteBuffer byteArray)
	{
		byteArray.putFloat(X);
		byteArray.putFloat(Y);
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 * 
	 * @param stream OutputStream to copy the 8 bytes for X and Y
	 * @param le True for writing little endian data
	 * @throws IOException 
	 */
	public void write(OutputStream stream, boolean le) throws IOException
	{
		if (le)
		{
			stream.write(Helpers.FloatToBytesL(X));
			stream.write(Helpers.FloatToBytesL(Y));
		}
		else
		{
			stream.write(Helpers.FloatToBytesB(X));
			stream.write(Helpers.FloatToBytesB(Y));
		}
	}

	static public Vector2 parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector2(parser);
	}
	
	public void serializeXml(XmlSerializer writer, String namespace, String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(Y)).endTag(namespace, "Y");
		writer.endTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", Y)).endTag(namespace, "Y");
		writer.endTag(namespace, name);
	}
	/**
	 * Get a formatted string representation of the vector
	 * 
	 * @return A string representation of the vector
	 */
	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%f, %f>", X, Y);
	}

	/**
	 * Get a string representation of the vector elements with up to three
	 * decimal digits and separated by spaces only
	 * 
	 * @return Raw string representation of the vector
	 */
	public String toRawString()
	{
		return String.format(Helpers.EnUsCulture, "%.3f, %.3f", X, Y);
	}

	/** Creates a hash code for the vector */
	@Override
	public int hashCode()
	{
		return ((Float)X).hashCode() * 31 + ((Float)Y).hashCode();
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
		}
		else
		{
			X = Helpers.BytesToFloatB(bytes, pos + 0);
			Y = Helpers.BytesToFloatB(bytes, pos + 4);
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
	}

	public void fromBytes(SwappedDataInputStream is) throws IOException
	{
		X = is.readFloat();
		Y = is.readFloat();
	}

	/**
	 * Writes the raw bytes for this vector to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 12 bytes before the end of the array
	 */
	public void toBytes(byte[] dest, int pos, boolean le)
	{
		if (le)
		{
			Helpers.FloatToBytesL(X, dest, pos + 0);
			Helpers.FloatToBytesL(Y, dest, pos + 4);
		}
		else
		{
			Helpers.FloatToBytesB(X, dest, pos + 0);
			Helpers.FloatToBytesB(Y, dest, pos + 4);
		}
	}

	public float length()
	{
		return (float) Math.sqrt(distanceSquared(this, Zero));
	}

	public float lengthSquared()
	{
		return distanceSquared(this, Zero);
	}

	public Vector2 normalize()
	{
		float length = length();
		if (length > Helpers.FLOAT_MAG_THRESHOLD)
		{
			return divide(length);
		}
		X = 0f;
		Y = 0f;
		return this;
	}

	/**
	 * Test if this vector is equal to another vector, within a given tolerance
	 * range
	 * 
	 * @param vec
	 *            Vector to test against
	 * @param tolerance
	 *            The acceptable magnitude of difference between the two vectors
	 * @return True if the magnitude of difference between the two vectors is
	 *         less than the given tolerance, otherwise false
	 */
	public boolean approxEquals(Vector2 vec, float tolerance)
	{
		Vector2 diff = subtract(vec);
		return (diff.lengthSquared() <= tolerance * tolerance);
	}

	public int compareTo(Vector2 vector)
	{
		return ((Float) length()).compareTo(vector.length());
	}

	/** Test if this vector is composed of all finite numbers */
	public boolean isFinite()
	{
		return Helpers.IsFinite(X) && Helpers.IsFinite(Y);
	}

	public boolean isZero()
	{
		return equals(Zero);
	}

	public static boolean isZero(Vector2 v)
	{
		if (v != null)
			return v.equals(Zero);
		return false;
	}

	public static boolean isZeroOrNull(Vector2 v)
	{
		if (v != null)
			return v.equals(Zero);
		return true;
	}

	public static Vector2 clamp(Vector2 value1, Vector2 min, Vector2 max)
	{
		return new Vector2(Helpers.Clamp(value1.X, min.X, max.X), Helpers.Clamp(value1.Y, min.Y, max.Y));
	}

	public static float distance(Vector2 value1, Vector2 value2)
	{
		return (float) Math.sqrt(distanceSquared(value1, value2));
	}

	public static float distanceSquared(Vector2 value1, Vector2 value2)
	{
		return (value1.X - value2.X) * (value1.X - value2.X) + (value1.Y - value2.Y) * (value1.Y - value2.Y);
	}

	public static float dot(Vector2 value1, Vector2 value2)
	{
		return value1.X * value2.X + value1.Y * value2.Y;
	}

	public static Vector2 lerp(Vector2 value1, Vector2 value2, float amount)
	{
		return new Vector2(Helpers.Lerp(value1.X, value2.X, amount), Helpers.Lerp(value1.Y, value2.Y, amount));
	}

	public static Vector2 max(Vector2 value1, Vector2 value2)
	{
		return new Vector2(Math.max(value1.X, value2.X), Math.max(value1.Y, value2.Y));
	}

	public static Vector2 min(Vector2 value1, Vector2 value2)
	{
		return new Vector2(Math.min(value1.X, value2.X), Math.min(value1.Y, value2.Y));
	}

	public static Vector2 normalize(Vector2 value)
	{
		return new Vector2(value).normalize();
	}

	/** Interpolates between two vectors using a cubic equation */
	public static Vector2 smoothStep(Vector2 value1, Vector2 value2, float amount)
	{
		return new Vector2(Helpers.SmoothStep(value1.X, value2.X, amount), Helpers.SmoothStep(value1.Y, value2.Y,
				amount));
	}

	public static Vector2 transform(Vector2 position, Matrix4 matrix)
	{
		return new Vector2((position.X * matrix.M11) + (position.Y * matrix.M21) + matrix.M41,
		                   (position.X * matrix.M12) + (position.Y * matrix.M22) + matrix.M42);
	}

	public static Vector2 transformNormal(Vector2 position, Matrix4 matrix)
	{
		return new Vector2((position.X * matrix.M11) + (position.Y * matrix.M21),
		                   (position.X * matrix.M12) + (position.Y * matrix.M22));
	}

	/**
	 * Parse a vector from a string
	 * 
	 * @param val
	 *            A string representation of a 2D vector, enclosed in arrow
	 *            brackets and separated by commas
	 */
	public static Vector2 Parse(String val)
	{
		String splitChar = ",";
		String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		return new Vector2(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()));
	}

	public static boolean TryParse(String val, RefObject<Vector2> result)
	{
		try
		{
			result.argvalue = Parse(val);
			return true;
		}
		catch (Throwable t)
		{
			result.argvalue = Vector2.Zero;
			return false;
		}
	}

	public boolean equals(Vector3 val)
	{
		return val != null && X == val.X && Y == val.Y;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj != null && (obj instanceof Vector2) && equals((Vector2)obj);
	}

	public boolean equals(Vector2 o)
	{
		return o != null && o.X == X && o.Y == Y;
	}

    public Vector2 flip()
    {
        X = 1.0f - X;
        Y = 1.0f - Y;
        return this;
    }

    public Vector2 negate()
	{
		X = -X;
		Y = -Y;
		return this;
	}

	public Vector2 add(Vector2 value)
	{
		X += value.X;
		Y += value.Y;
		return this;
	}

	public Vector2 subtract(Vector2 value)
	{
		X -= value.X;
		Y -= value.X;
		return this;
	}

	public Vector2 multiply(Vector2 value)
	{
		X *= value.X;
		Y *= value.Y;
		return this;
	}

	public Vector2 multiply(float scaleFactor)
	{
		X *= scaleFactor;
		Y *= scaleFactor;
		return this;
	}

	public Vector2 divide(Vector2 value)
	{
		X /= value.X;
		Y /= value.Y;
		return this;
	}

	public Vector2 divide(float divider)
	{
		float factor = 1 / divider;
		X *= factor;
		Y *= factor;
		return this;
	}

	public static Vector2 negate(Vector2 value)
	{
		return new Vector2(value).negate();
	}

	public static Vector2 add(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1).add(value2);
	}

	public static Vector2 subtract(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1).subtract(value2);
	}

	public static Vector2 multiply(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1).multiply(value2);
	}

	public static Vector2 multiply(Vector2 value1, float scaleFactor)
	{
		return new Vector2(value1).multiply(scaleFactor);
	}

	public static Vector2 divide(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1).divide(value2);
	}

	public static Vector2 divide(Vector2 value1, float divider)
	{
		return new Vector2(value1).divide(divider);
	}

	/**
	 * A vector with a value of 0,0
	 */
	public final static Vector2 Zero = new Vector2();
	/**
	 * A vector with a value of 1,1
	 */
	public final static Vector2 One = new Vector2(1f, 1f);
	/**
	 * A vector with a value of 1,0
	 */
	public final static Vector2 UnitX = new Vector2(1f, 0f);
	/**
	 * A vector with a value of 0,1
	 */
	public final static Vector2 UnitY = new Vector2(0f, 1f);
}
