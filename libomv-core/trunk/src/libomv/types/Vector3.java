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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.commons.io.input.SwappedDataInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class Vector3
{
	public float X;

	public float Y;

	public float Z;

	public Vector3(float val)
	{
		X = Y = Z = val;
	}

	public Vector3(float[] arr)
	{
		fromArray(this, arr, 0);
	}
	
	public Vector3(float[] arr, int offset)
	{
		fromArray(this, arr, offset);
	}
	
	public Vector3(Vector3 v)
	{
		X = v.X;
		Y = v.Y;
		Z = v.Z;
	}

	public Vector3(Vector3d vector)
	{
		X = (float) vector.X;
		Y = (float) vector.Y;
		Z = (float) vector.Z;
	}

	public Vector3(ByteBuffer byteArray)
	{
		X = byteArray.getFloat();
		Y = byteArray.getFloat();
		Z = byteArray.getFloat();
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector3(XmlPullParser parser) throws XmlPullParserException, IOException
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
   				Z = Helpers.TryParseFloat(parser.nextText().trim());
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
    public Vector3(DataInputStream is) throws IOException
    {
		X = Y = Z = 0f;
		fromBytes(is);
    }
    
    public Vector3(SwappedDataInputStream is) throws IOException
    {
		X = Y = Z = 0f;
		fromBytes(is);
    }

    /**
	 * Constructor, builds a vector from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing three four-byte floats
	 * @param pos
	 *            Beginning position in the byte array
	 * @param le
	 *            is the byte array in little endian format
	 */
	public Vector3(byte[] byteArray, int pos)
	{
		X = Y = Z = 0f;
		fromBytes(byteArray, pos, false);
	}

	public Vector3(byte[] byteArray, int pos, boolean le)
	{
		X = Y = Z = 0f;
		fromBytes(byteArray, pos, le);
	}

	public Vector3(float x, float y, float z)
	{
		X = x;
		Y = y;
		Z = z;
	}

/*
  	public Vector3(String value)
 
	{
		// TODO Auto-generated constructor stub
	}
*/
	/**
	 * Returns the raw bytes for this vector
	 * 
	 * @return An eight-byte array containing X and Y
	 */
	public byte[] getBytes()
	{
		byte[] byteArray = new byte[12];
		toBytes(byteArray, 0, false);
		return byteArray;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 * 
	 * @param byteArray buffer to copy the 12 bytes for X, Y, and Z
	 * @param le True for writing little endian data
	 * @throws IOException 
	 */
	public void write(ByteBuffer byteArray)
	{
		byteArray.putFloat(X);
		byteArray.putFloat(Y);
		byteArray.putFloat(Z);
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 * 
	 * @param stream OutputStream to copy the 12 bytes for X, Y, and Z
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
		}
		else
		{
			stream.write(Helpers.FloatToBytesB(X));
			stream.write(Helpers.FloatToBytesB(Y));
			stream.write(Helpers.FloatToBytesB(Z));
		}
	}

	static public Vector3 parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector3(parser);
	}
	
	public void serializeXml(XmlSerializer writer, String namespace, String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(Z)).endTag(namespace, "Z");
		writer.endTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", Z)).endTag(namespace, "Z");
		writer.endTag(namespace, name);
	}

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%.3f, %.3f, %.3f>", X, Y, Z);
	}

	@Override
	public int hashCode()
	{
		return ((Float)X).hashCode() * 31 * 31  + ((Float)Y).hashCode() * 31 + ((Float)Z).hashCode();
	}

	/**
	 * Initializes a vector from a flaot array
	 * 
	 * @param vec The vector to intialize
	 * @param arr Is the float array
	 * @param pos Beginning position in the float array
	 */
	public static Vector3 fromArray(Vector3 vec, float[] arr, int pos)
	{
		if (arr.length >= (pos + 3))
		{		
			vec.X = arr[pos + 0];
			vec.Y = arr[pos + 1];
			vec.Z = arr[pos + 2];
		}
		return vec;
	}

	/**
	 * Builds a vector from a byte array
	 * 
	 * @param byteArray Byte array containing a 12 byte vector
	 * @param pos Beginning position in the byte array
	 * @param le Is the byte array in little endian format
	 */
	public void fromBytes(byte[] bytes, int pos, boolean le)
	{
		if (le)
		{
			/* Little endian architecture */
			X = Helpers.BytesToFloatL(bytes, pos + 0);
			Y = Helpers.BytesToFloatL(bytes, pos + 4);
			Z = Helpers.BytesToFloatL(bytes, pos + 8);
		}
		else
		{
			X = Helpers.BytesToFloatB(bytes, pos + 0);
			Y = Helpers.BytesToFloatB(bytes, pos + 4);
			Z = Helpers.BytesToFloatB(bytes, pos + 8);
		}
	}

	/**
	 * Builds a vector from a data stream
	 * 
	 * @param is DataInputStream to read the vector from
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
	 * Writes the raw bytes for this vector to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 12 bytes before the end of the array
	 */
	public void toBytes(byte[] dest, int pos)
	{
		toBytes(dest, pos, false);
	}

	public void toBytes(byte[] dest, int pos, boolean le)
	{
		if (le)
		{
			Helpers.FloatToBytesL(X, dest, pos + 0);
			Helpers.FloatToBytesL(Y, dest, pos + 4);
			Helpers.FloatToBytesL(Z, dest, pos + 8);
		}
		else
		{
			Helpers.FloatToBytesB(X, dest, pos + 0);
			Helpers.FloatToBytesB(Y, dest, pos + 4);
			Helpers.FloatToBytesB(Z, dest, pos + 8);
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

	public Vector3 normalize()
	{
        // Catch very small rounding errors when normalizing
		float length = length();
		if (length > Helpers.FLOAT_MAG_THRESHOLD)
		{
			return divide(length);
		}
		X = 0f;
		Y = 0f;
		Z = 0f;
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
	public boolean approxEquals(Vector3 vec, float tolerance)
	{
		Vector3 diff = subtract(this, vec);
		return (diff.lengthSquared() <= tolerance * tolerance);
	}

	public int compareTo(Vector3 vector)
	{
		return ((Float) length()).compareTo(vector.length());
	}

	/** Test if this vector is composed of all finite numbers */
	public boolean isFinite()
	{
		return (Helpers.IsFinite(X) && Helpers.IsFinite(Y) && Helpers.IsFinite(Z));
	}

	public boolean isZero()
	{
		return equals(Zero);
	}

	public static boolean isZero(Vector3 v)
	{
		if (v != null)
			return v.equals(Zero);
		return false;
	}
	
	public static boolean isZeroOrNull(Vector3 v)
	{
		if (v != null)
			return v.equals(Zero);
		return true;
	}

	public Vector3 clamp(Vector3 min, Vector3 max)
	{
		X = Helpers.Clamp(X, min.X, max.X);
		Y = Helpers.Clamp(Y, min.Y, max.Y);
		Z = Helpers.Clamp(Z, min.Z, max.Z);
		return this;
	}
	
	public Vector3 clamp(float min, float max)
	{
		X = Helpers.Clamp(X, min, max);
		Y = Helpers.Clamp(Y, min, max);
		Z = Helpers.Clamp(Z, min, max);
		return this;
	}
	
	public float mag()
	{
		return mag(this);
	}

	public static Vector3 cross(Vector3 value1, Vector3 value2)
	{
		return new Vector3(value1).cross(value2);
	}

	public static float distance(Vector3 value1, Vector3 value2)
	{
		return (float) Math.sqrt(distanceSquared(value1, value2));
	}

	public static float distanceSquared(Vector3 value1, Vector3 value2)
	{
		return (value1.X - value2.X) * (value1.X - value2.X)
                + (value1.Y - value2.Y) * (value1.Y - value2.Y)
				+ (value1.Z - value2.Z) * (value1.Z - value2.Z);
	}

	public static float dot(Vector3 value1, Vector3 value2)
	{
		return value1.X * value2.X + value1.Y * value2.Y + value1.Z * value2.Z;
	}

	public static Vector3 lerp(Vector3 value1, Vector3 value2, float amount)
	{

		return new Vector3(Helpers.Lerp(value1.X, value2.X, amount),
				           Helpers.Lerp(value1.Y, value2.Y, amount),
				           Helpers.Lerp(value1.Z, value2.Z, amount));
	}

	public static float mag(Vector3 value)
	{
		return (float) Math.sqrt((value.X * value.X) + (value.Y * value.Y) + (value.Z * value.Z));
	}

	public static Vector3 max(Vector3 value1, Vector3 value2)
	{
		return new Vector3(Math.max(value1.X, value2.X), Math.max(value1.Y, value2.Y), Math.max(value1.Z, value2.Z));
	}

	public static Vector3 min(Vector3 value1, Vector3 value2)
	{
		return new Vector3(Math.min(value1.X, value2.X), Math.min(value1.Y, value2.Y), Math.min(value1.Z, value2.Z));
	}

	public static Vector3 normalize(Vector3 value)
	{
		return new Vector3(value).normalize();
	}

	public static Vector3 clamp(Vector3 value, Vector3 min, Vector3 max)
	{
		return new Vector3(value).clamp(min, max);
	}
	
	public static Vector3 clamp(Vector3 value, float min, float max)
	{
		return new Vector3(value).clamp(min, max);
	}

	/**
	 * Calculate the rotation between two vectors
	 * 
	 * @param a
	 *            Normalized directional vector (such as 1,0,0 for forward
	 *            facing)
	 * @param b
	 *            Normalized target vector
	 */
	public static Quaternion rotationBetween(Vector3 a, Vector3 b)
	{
		float dotProduct = dot(a, b);
		Vector3 crossProduct = cross(a, b);
		float magProduct = a.length() * b.length();
		double angle = Math.acos(dotProduct / magProduct);
		Vector3 axis = crossProduct.normalize();
		float s = (float) Math.sin(angle / 2d);

		return new Quaternion(axis.X * s, axis.Y * s, axis.Z * s, (float) Math.cos(angle / 2d));
	}

	/** Interpolates between two vectors using a cubic equation */
	public static Vector3 smoothStep(Vector3 value1, Vector3 value2, float amount)
	{
		return new Vector3(Helpers.SmoothStep(value1.X, value2.X, amount), Helpers.SmoothStep(value1.Y, value2.Y,
				amount), Helpers.SmoothStep(value1.Z, value2.Z, amount));
	}

	public static Vector3 transform(Vector3 position, Matrix4 matrix)
	{
		return new Vector3((position.X * matrix.M11) + (position.Y * matrix.M21) + (position.Z * matrix.M31)
				+ matrix.M41, (position.X * matrix.M12) + (position.Y * matrix.M22) + (position.Z * matrix.M32)
				+ matrix.M42, (position.X * matrix.M13) + (position.Y * matrix.M23) + (position.Z * matrix.M33)
				+ matrix.M43);
	}

	public static Vector3 transformNormal(Vector3 position, Matrix4 matrix)
	{
		return new Vector3((position.X * matrix.M11) + (position.Y * matrix.M21) + (position.Z * matrix.M31),
				(position.X * matrix.M12) + (position.Y * matrix.M22) + (position.Z * matrix.M32),
				(position.X * matrix.M13) + (position.Y * matrix.M23) + (position.Z * matrix.M33));
	}

	/**
	 * Parse a vector from a string
	 * 
	 * @param val
	 *            A string representation of a 3D vector, enclosed in arrow
	 *            brackets and separated by commas
	 */
	public static Vector3 Parse(String val)
	{
		String splitChar = ",";
		String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		return new Vector3(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
				Float.parseFloat(split[2].trim()));
	}

	public static Vector3 TryParse(String val)
	{
		try
		{
			return Parse(val);
		}
		catch (Throwable t)
		{
			return Vector3.Zero;
		}
	}

	public static boolean TryParse(String val, RefObject<Vector3> result)
	{
		try
		{
			result.argvalue = Parse(val);
			return true;
		}
		catch (Throwable t)
		{
			result.argvalue = Vector3.Zero;
			return false;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj != null && ((obj instanceof Vector3) && equals((Vector3)obj) || (obj instanceof Vector3d) && equals((Vector3d)obj));
	}

	public boolean equals(Vector3 val)
	{
		return val != null && X == val.X && Y == val.Y && Z == val.Z;
	}

	public boolean equals(Vector3d val)
	{
		return val != null && X == val.X && Y == val.Y && Z == val.Z;
	}

	public Vector3 negate()
	{
		X = -X;
		Y = -Y;
		Z = -Z;
		return this;
	}

	public Vector3 add(Vector3 val)
	{
		X += val.X;
		Y += val.Y;
		Z += val.Z;
		return this;
	}

	public Vector3 subtract(Vector3 val)
	{
		X -= val.X;
		Y -= val.Y;
		Z -= val.Z;
		return this;
	}

	public Vector3 multiply(float scaleFactor)
	{
		X *= scaleFactor;
		Y *= scaleFactor;
		Z *= scaleFactor;
		return this;
	}
	
	public Vector3 multiply(Vector3 value)
	{
		X *= value.X;
		Y *= value.Y;
		Z *= value.Z;
		return this;
	}

	public Vector3 multiply(Quaternion rot)
	{
        // From http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/transforms/
		float x = rot.W * rot.W * X + 2f * rot.Y * rot.W * Z - 2f * rot.Z * rot.W * Y + rot.X * rot.X * X
		  + 2f * rot.Y * rot.X * Y + 2f * rot.Z * rot.X * Z - rot.Z * rot.Z * X - rot.Y * rot.Y * X;
        float y = 2f * rot.X * rot.Y * X + rot.Y * rot.Y * Y + 2f * rot.Z * rot.Y * Z + 2f * rot.W * rot.Z * X
          - rot.Z * rot.Z * Y + rot.W * rot.W * Y - 2f * rot.X * rot.W * Z - rot.X * rot.X * Y;
        Z = 2f * rot.X * rot.Z * X + 2f * rot.Y * rot.Z * Y + rot.Z * rot.Z * Z - 2f * rot.W * rot.Y * X
		  - rot.Y * rot.Y * Z + 2f * rot.W * rot.X * Y - rot.X * rot.X * Z + rot.W * rot.W * Z;
		X = x;
		Y = y;
        return this;
	}

	public Vector3 divide(Vector3 value)
	{
		X /= value.X;
		Y /= value.Y;
		Z /= value.Z;
		return this;
	}

	public Vector3 divide(float divider)
	{
		float factor = 1f / divider;
		X *= factor;
		Y *= factor;
		Z *= factor;
		return this;
	}
	
	public Vector3 cross(Vector3 value)
	{
		X = Y * value.Z - value.Y * Z;
		Y = Z * value.X - value.Z * X;
		Z = X * value.Y - value.X * Y;
		return this;
	}

	public static Vector3 negate(Vector3 value)
	{
		return new Vector3(value).negate();
	}

	public static Vector3 add(Vector3 val1, Vector3 val2)
	{
		return new Vector3(val1).add(val2);
	}

	public static Vector3 subtract(Vector3 val1, Vector3 val2)
	{
		return new Vector3(val1).subtract(val2);
	}

	public static Vector3 multiply(Vector3 value1, Vector3 value2)
	{
		return new Vector3(value1).multiply(value2);
	}

	public static Vector3 multiply(Vector3 value1, float scaleFactor)
	{
		return new Vector3(value1).multiply(scaleFactor);
	}

	public static Vector3 multiply(Vector3 vec, Quaternion rot)
	{
		return new Vector3(vec).multiply(rot);
	}

	public static Vector3 multiply(Vector3 vector, Matrix4 matrix)
	{
		return transform(vector, matrix);
	}

	public static Vector3 divide(Vector3 value1, Vector3 value2)
	{
		return new Vector3(value1).divide(value2);
	}

	public static Vector3 divide(Vector3 value, float divider)
	{
		return new Vector3(value).divide(divider);
	}

	/** A vector with a value of 0,0,0 */
	public final static Vector3 Zero = new Vector3(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector3 One = new Vector3(1f, 1f, 1f);
	/** A unit vector facing forward (X axis), value 1,0,0 */
	public final static Vector3 UnitX = new Vector3(1f, 0f, 0f);
	/** A unit vector facing left (Y axis), value 0,1,0 */
	public final static Vector3 UnitY = new Vector3(0f, 1f, 0f);
	/** A unit vector facing up (Z axis), value 0,0,1 */
	public final static Vector3 UnitZ = new Vector3(0f, 0f, 1f);
}
