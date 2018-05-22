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

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class Vector3 {

	/** A vector with a value of 0,0,0 */
	public static final Vector3 ZERO = new Vector3(0f);
	/** A vector with a value of 1,1,1 */
	public static final Vector3 ONE = new Vector3(1f, 1f, 1f);
	/** A unit vector facing forward (X axis), value 1,0,0 */
	public static final Vector3 UNIT_X = new Vector3(1f, 0f, 0f);
	/** A unit vector facing left (Y axis), value 0,1,0 */
	public static final Vector3 UNIT_Y = new Vector3(0f, 1f, 0f);
	/** A unit vector facing up (Z axis), value 0,0,1 */
	public static final Vector3 UNIT_Z = new Vector3(0f, 0f, 1f);

	public float x;

	public float y;

	public float z;

	public Vector3(float val) {
		x = y = z = val;
	}

	public Vector3(float[] arr) {
		fromArray(this, arr, 0);
	}

	public Vector3(float[] arr, int offset) {
		fromArray(this, arr, offset);
	}

	public Vector3(Vector3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public Vector3(Vector3d vector) {
		x = (float) vector.x;
		y = (float) vector.y;
		z = (float) vector.z;
	}

	public Vector3(ByteBuffer byteArray) {
		x = byteArray.getFloat();
		y = byteArray.getFloat();
		z = byteArray.getFloat();
	}

	/**
	 * Constructor, builds a vector from an XML reader
	 *
	 * @param parser
	 *            XML pull parser reader
	 */
	public Vector3(XmlPullParser parser) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Vector3
		int eventType = parser.getEventType();
		if (eventType != XmlPullParser.START_TAG)
			throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(),
					parser, null);

		while (parser.nextTag() == XmlPullParser.START_TAG) {
			String name = parser.getName();
			if (name.equalsIgnoreCase("X")) {
				x = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("Y")) {
				y = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("Z")) {
				z = Helpers.tryParseFloat(parser.nextText().trim());
			} else {
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
	public Vector3(DataInputStream is) throws IOException {
		x = y = z = 0f;
		fromBytes(is);
	}

	public Vector3(SwappedDataInputStream is) throws IOException {
		x = y = z = 0f;
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
	public Vector3(byte[] byteArray, int pos) {
		x = y = z = 0f;
		fromBytes(byteArray, pos, false);
	}

	public Vector3(byte[] byteArray, int pos, boolean le) {
		x = y = z = 0f;
		fromBytes(byteArray, pos, le);
	}

	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/*
	 * public Vector3(String value)
	 *
	 * { // TODO Auto-generated constructor stub }
	 */
	/**
	 * Returns the raw bytes for this vector
	 *
	 * @return An eight-byte array containing X and Y
	 */
	public byte[] getBytes() {
		byte[] byteArray = new byte[12];
		toBytes(byteArray, 0, false);
		return byteArray;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 *
	 * @param byteArray
	 *            buffer to copy the 12 bytes for X, Y, and Z
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(ByteBuffer byteArray) {
		byteArray.putFloat(x);
		byteArray.putFloat(y);
		byteArray.putFloat(z);
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 *
	 * @param stream
	 *            OutputStream to copy the 12 bytes for X, Y, and Z
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(OutputStream stream, boolean le) throws IOException {
		if (le) {
			stream.write(Helpers.floatToBytesL(x));
			stream.write(Helpers.floatToBytesL(y));
			stream.write(Helpers.floatToBytesL(z));
		} else {
			stream.write(Helpers.floatToBytesB(x));
			stream.write(Helpers.floatToBytesB(y));
			stream.write(Helpers.floatToBytesB(z));
		}
	}

	public static Vector3 parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		return new Vector3(parser);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(z)).endTag(namespace, "Z");
		writer.endTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", z)).endTag(namespace, "Z");
		writer.endTag(namespace, name);
	}

	@Override
	public String toString() {
		return String.format(Helpers.EnUsCulture, "<%.3f, %.3f, %.3f>", x, y, z);
	}

	@Override
	public int hashCode() {
		return ((Float) x).hashCode() * 31 * 31 + ((Float) y).hashCode() * 31 + ((Float) z).hashCode();
	}

	/**
	 * Initializes a vector from a flaot array
	 *
	 * @param vec
	 *            The vector to intialize
	 * @param arr
	 *            Is the float array
	 * @param pos
	 *            Beginning position in the float array
	 */
	public static Vector3 fromArray(Vector3 vec, float[] arr, int pos) {
		if (arr.length >= (pos + 3)) {
			vec.x = arr[pos + 0];
			vec.y = arr[pos + 1];
			vec.z = arr[pos + 2];
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
	 *            Is the byte array in little endian format
	 */
	public void fromBytes(byte[] bytes, int pos, boolean le) {
		if (le) {
			/* Little endian architecture */
			x = Helpers.bytesToFloatL(bytes, pos + 0);
			y = Helpers.bytesToFloatL(bytes, pos + 4);
			z = Helpers.bytesToFloatL(bytes, pos + 8);
		} else {
			x = Helpers.bytesToFloatB(bytes, pos + 0);
			y = Helpers.bytesToFloatB(bytes, pos + 4);
			z = Helpers.bytesToFloatB(bytes, pos + 8);
		}
	}

	/**
	 * Builds a vector from a data stream
	 *
	 * @param is
	 *            DataInputStream to read the vector from
	 * @throws IOException
	 */
	public void fromBytes(DataInputStream is) throws IOException {
		x = is.readFloat();
		y = is.readFloat();
		z = is.readFloat();
	}

	public void fromBytes(SwappedDataInputStream is) throws IOException {
		x = is.readFloat();
		y = is.readFloat();
		z = is.readFloat();
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
	public void toBytes(byte[] dest, int pos) {
		toBytes(dest, pos, false);
	}

	public void toBytes(byte[] dest, int pos, boolean le) {
		if (le) {
			Helpers.floatToBytesL(x, dest, pos + 0);
			Helpers.floatToBytesL(y, dest, pos + 4);
			Helpers.floatToBytesL(z, dest, pos + 8);
		} else {
			Helpers.floatToBytesB(x, dest, pos + 0);
			Helpers.floatToBytesB(y, dest, pos + 4);
			Helpers.floatToBytesB(z, dest, pos + 8);
		}
	}

	public float length() {
		return (float) Math.sqrt(distanceSquared(this, ZERO));
	}

	public float lengthSquared() {
		return distanceSquared(this, ZERO);
	}

	public Vector3 normalize() {
		// Catch very small rounding errors when normalizing
		float length = length();
		if (length > Helpers.FLOAT_MAG_THRESHOLD) {
			return divide(length);
		}
		x = 0f;
		y = 0f;
		z = 0f;
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
	 * @return True if the magnitude of difference between the two vectors is less
	 *         than the given tolerance, otherwise false
	 */
	public boolean approxEquals(Vector3 vec, float tolerance) {
		Vector3 diff = subtract(this, vec);
		return diff.lengthSquared() <= tolerance * tolerance;
	}

	public int compareTo(Vector3 vector) {
		return ((Float) length()).compareTo(vector.length());
	}

	/** Test if this vector is composed of all finite numbers */
	public boolean isFinite() {
		return Helpers.isFinite(x) && Helpers.isFinite(y) && Helpers.isFinite(z);
	}

	public boolean isZero() {
		return equals(ZERO);
	}

	public static boolean isZero(Vector3 v) {
		if (v != null)
			return v.equals(ZERO);
		return false;
	}

	public static boolean isZeroOrNull(Vector3 v) {
		if (v != null)
			return v.equals(ZERO);
		return true;
	}

	public Vector3 clamp(Vector3 min, Vector3 max) {
		x = Helpers.clamp(x, min.x, max.x);
		y = Helpers.clamp(y, min.y, max.y);
		z = Helpers.clamp(z, min.z, max.z);
		return this;
	}

	public Vector3 clamp(float min, float max) {
		x = Helpers.clamp(x, min, max);
		y = Helpers.clamp(y, min, max);
		z = Helpers.clamp(z, min, max);
		return this;
	}

	public float mag() {
		return mag(this);
	}

	public static Vector3 cross(Vector3 value1, Vector3 value2) {
		return new Vector3(value1).cross(value2);
	}

	public static float distance(Vector3 value1, Vector3 value2) {
		return (float) Math.sqrt(distanceSquared(value1, value2));
	}

	public static float distanceSquared(Vector3 value1, Vector3 value2) {
		return (value1.x - value2.x) * (value1.x - value2.x) + (value1.y - value2.y) * (value1.y - value2.y)
				+ (value1.z - value2.z) * (value1.z - value2.z);
	}

	public static float dot(Vector3 value1, Vector3 value2) {
		return value1.x * value2.x + value1.y * value2.y + value1.z * value2.z;
	}

	public static Vector3 lerp(Vector3 value1, Vector3 value2, float amount) {

		return new Vector3(Helpers.lerp(value1.x, value2.x, amount), Helpers.lerp(value1.y, value2.y, amount),
				Helpers.lerp(value1.z, value2.z, amount));
	}

	public static float mag(Vector3 value) {
		return (float) Math.sqrt((value.x * value.x) + (value.y * value.y) + (value.z * value.z));
	}

	public static Vector3 max(Vector3 value1, Vector3 value2) {
		return new Vector3(Math.max(value1.x, value2.x), Math.max(value1.y, value2.y), Math.max(value1.z, value2.z));
	}

	public static Vector3 min(Vector3 value1, Vector3 value2) {
		return new Vector3(Math.min(value1.x, value2.x), Math.min(value1.y, value2.y), Math.min(value1.z, value2.z));
	}

	public static Vector3 normalize(Vector3 value) {
		return new Vector3(value).normalize();
	}

	public static Vector3 clamp(Vector3 value, Vector3 min, Vector3 max) {
		return new Vector3(value).clamp(min, max);
	}

	public static Vector3 clamp(Vector3 value, float min, float max) {
		return new Vector3(value).clamp(min, max);
	}

	/**
	 * Calculate the rotation between two vectors
	 *
	 * @param a
	 *            Normalized directional vector (such as 1,0,0 for forward facing)
	 * @param b
	 *            Normalized target vector
	 */
	public static Quaternion rotationBetween(Vector3 a, Vector3 b) {
		float dotProduct = dot(a, b);
		Vector3 crossProduct = cross(a, b);
		float magProduct = a.length() * b.length();
		double angle = Math.acos(dotProduct / magProduct);
		Vector3 axis = crossProduct.normalize();
		float s = (float) Math.sin(angle / 2d);

		return new Quaternion(axis.x * s, axis.y * s, axis.z * s, (float) Math.cos(angle / 2d));
	}

	/** Interpolates between two vectors using a cubic equation */
	public static Vector3 smoothStep(Vector3 value1, Vector3 value2, float amount) {
		return new Vector3(Helpers.smoothStep(value1.x, value2.x, amount),
				Helpers.smoothStep(value1.y, value2.y, amount), Helpers.smoothStep(value1.z, value2.z, amount));
	}

	public static Vector3 transform(Vector3 position, Matrix4 matrix) {
		return new Vector3(
				(position.x * matrix.M11) + (position.y * matrix.M21) + (position.z * matrix.M31) + matrix.M41,
				(position.x * matrix.M12) + (position.y * matrix.M22) + (position.z * matrix.M32) + matrix.M42,
				(position.x * matrix.M13) + (position.y * matrix.M23) + (position.z * matrix.M33) + matrix.M43);
	}

	public static Vector3 transformNormal(Vector3 position, Matrix4 matrix) {
		return new Vector3((position.x * matrix.M11) + (position.y * matrix.M21) + (position.z * matrix.M31),
				(position.x * matrix.M12) + (position.y * matrix.M22) + (position.z * matrix.M32),
				(position.x * matrix.M13) + (position.y * matrix.M23) + (position.z * matrix.M33));
	}

	/**
	 * Parse a vector from a string
	 *
	 * @param val
	 *            A string representation of a 3D vector, enclosed in arrow brackets
	 *            and separated by commas
	 */
	public static Vector3 parse(String val) {
		String splitChar = ",";
		String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		return new Vector3(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
				Float.parseFloat(split[2].trim()));
	}

	public static Vector3 tryParse(String val) {
		try {
			return parse(val);
		} catch (Throwable t) {
			return Vector3.ZERO;
		}
	}

	public static boolean tryParse(String val, RefObject<Vector3> result) {
		try {
			result.argvalue = parse(val);
			return true;
		} catch (Throwable t) {
			result.argvalue = Vector3.ZERO;
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && ((obj instanceof Vector3) && equals((Vector3) obj)
				|| (obj instanceof Vector3d) && equals((Vector3d) obj));
	}

	public boolean equals(Vector3 val) {
		return val != null && x == val.x && y == val.y && z == val.z;
	}

	public boolean equals(Vector3d val) {
		return val != null && x == val.x && y == val.y && z == val.z;
	}

	public Vector3 negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vector3 add(Vector3 val) {
		x += val.x;
		y += val.y;
		z += val.z;
		return this;
	}

	public Vector3 subtract(Vector3 val) {
		x -= val.x;
		y -= val.y;
		z -= val.z;
		return this;
	}

	public Vector3 multiply(float scaleFactor) {
		x *= scaleFactor;
		y *= scaleFactor;
		z *= scaleFactor;
		return this;
	}

	public Vector3 multiply(Vector3 value) {
		x *= value.x;
		y *= value.y;
		z *= value.z;
		return this;
	}

	public Vector3 multiply(Quaternion rot) {
		// From
		// http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/transforms/
		float tx = rot.w * rot.w * x + 2f * rot.y * rot.w * z - 2f * rot.z * rot.w * y + rot.x * rot.x * x
				+ 2f * rot.y * rot.x * y + 2f * rot.z * rot.x * z - rot.z * rot.z * x - rot.y * rot.y * x;
		float ty = 2f * rot.x * rot.y * x + rot.y * rot.y * y + 2f * rot.z * rot.y * z + 2f * rot.w * rot.z * x
				- rot.z * rot.z * y + rot.w * rot.w * y - 2f * rot.x * rot.w * z - rot.x * rot.x * y;
		z = 2f * rot.x * rot.z * x + 2f * rot.y * rot.z * y + rot.z * rot.z * z - 2f * rot.w * rot.y * x
				- rot.y * rot.y * z + 2f * rot.w * rot.x * y - rot.x * rot.x * z + rot.w * rot.w * z;
		x = tx;
		y = ty;
		return this;
	}

	public Vector3 divide(Vector3 value) {
		x /= value.x;
		y /= value.y;
		z /= value.z;
		return this;
	}

	public Vector3 divide(float divider) {
		float factor = 1f / divider;
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}

	public Vector3 cross(Vector3 value) {
		x = y * value.z - value.y * z;
		y = z * value.x - value.z * x;
		z = x * value.y - value.x * y;
		return this;
	}

	public static Vector3 negate(Vector3 value) {
		return new Vector3(value).negate();
	}

	public static Vector3 add(Vector3 val1, Vector3 val2) {
		return new Vector3(val1).add(val2);
	}

	public static Vector3 subtract(Vector3 val1, Vector3 val2) {
		return new Vector3(val1).subtract(val2);
	}

	public static Vector3 multiply(Vector3 value1, Vector3 value2) {
		return new Vector3(value1).multiply(value2);
	}

	public static Vector3 multiply(Vector3 value1, float scaleFactor) {
		return new Vector3(value1).multiply(scaleFactor);
	}

	public static Vector3 multiply(Vector3 vec, Quaternion rot) {
		return new Vector3(vec).multiply(rot);
	}

	public static Vector3 multiply(Vector3 vector, Matrix4 matrix) {
		return transform(vector, matrix);
	}

	public static Vector3 divide(Vector3 value1, Vector3 value2) {
		return new Vector3(value1).divide(value2);
	}

	public static Vector3 divide(Vector3 value, float divider) {
		return new Vector3(value).divide(divider);
	}

}
