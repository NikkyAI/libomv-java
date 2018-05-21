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

public class Vector4 {
	/** A vector with a value of 0,0,0,0 */
	public final static Vector4 ZERO = new Vector4(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector4 ONE = new Vector4(1f, 1f, 1f, 1f);
	/** A unit vector facing forward (X axis), value 1,0,0,0 */
	public final static Vector4 UNIT_X = new Vector4(1f, 0f, 0f, 0f);
	/** A unit vector facing left (Y axis), value 0,1,0,0 */
	public final static Vector4 UNIT_Y = new Vector4(0f, 1f, 0f, 0f);
	/** A unit vector facing up (Z axis), value 0,0,1,0 */
	public final static Vector4 UNIT_Z = new Vector4(0f, 0f, 1f, 0f);
	/** A unit vector facing up (S axis), value 0,0,0,1 */
	public final static Vector4 UNIT_S = new Vector4(0f, 0f, 0f, 1f);

	public float x;

	public float y;

	public float z;

	public float s;

	public Vector4() {
		x = y = z = s = 0;
	}

	public Vector4(float val) {
		x = y = z = s = val;
	}

	public Vector4(ByteBuffer byteArray) {
		x = byteArray.getFloat();
		y = byteArray.getFloat();
		z = byteArray.getFloat();
		s = byteArray.getFloat();
	}

	public Vector4(float x, float y, float z, float s) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.s = s;
	}

	/**
	 * Constructor, builds a vector from an XML reader
	 *
	 * @param parser
	 *            XML pull parser reader
	 */
	public Vector4(XmlPullParser parser) throws XmlPullParserException, IOException {
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
			} else if (name.equalsIgnoreCase("S")) {
				s = Helpers.tryParseFloat(parser.nextText().trim());
			} else {
				Helpers.skipElement(parser);
			}
		}
	}

	public Vector4(byte[] dest, int pos) {
		x = y = z = s = 0;
		fromBytes(dest, pos, false);
	}

	public Vector4(byte[] dest, int pos, boolean le) {
		x = y = z = s = 0;
		fromBytes(dest, pos, le);
	}

	public Vector4(Vector4 v) {
		x = v.x;
		y = v.y;
		z = v.z;
		s = v.s;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 *
	 * @param byteArray
	 *            buffer to copy the 16 bytes for X, Y, Z, and S
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(ByteBuffer byteArray) {
		byteArray.putFloat(x);
		byteArray.putFloat(y);
		byteArray.putFloat(z);
		byteArray.putFloat(s);
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 *
	 * @param stream
	 *            OutputStream to copy the 16 bytes for X, Y, Z, and S
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(OutputStream stream, boolean le) throws IOException {
		if (le) {
			stream.write(Helpers.floatToBytesL(x));
			stream.write(Helpers.floatToBytesL(y));
			stream.write(Helpers.floatToBytesL(z));
			stream.write(Helpers.floatToBytesL(s));
		} else {
			stream.write(Helpers.floatToBytesB(x));
			stream.write(Helpers.floatToBytesB(y));
			stream.write(Helpers.floatToBytesB(z));
			stream.write(Helpers.floatToBytesB(s));
		}
	}

	/**
	 * Initializes a vector from a float array
	 *
	 * @param vec
	 *            the vector to intialize
	 * @param arr
	 *            is the float array
	 * @param pos
	 *            Beginning position in the float array
	 */
	public static Vector4 fromArray(Vector4 vec, float[] arr, int pos) {
		if (arr.length >= (pos + 4)) {
			vec.x = arr[pos + 0];
			vec.y = arr[pos + 1];
			vec.z = arr[pos + 2];
			vec.s = arr[pos + 3];
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
	public void fromBytes(byte[] bytes, int pos, boolean le) {
		if (le) {
			/* Little endian architecture */
			x = Helpers.bytesToFloatL(bytes, pos + 0);
			y = Helpers.bytesToFloatL(bytes, pos + 4);
			z = Helpers.bytesToFloatL(bytes, pos + 8);
			s = Helpers.bytesToFloatL(bytes, pos + 12);
		} else {
			x = Helpers.bytesToFloatB(bytes, pos + 0);
			y = Helpers.bytesToFloatB(bytes, pos + 4);
			z = Helpers.bytesToFloatB(bytes, pos + 8);
			s = Helpers.bytesToFloatB(bytes, pos + 12);
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
	 * Serializes this vector into four bytes in a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest) {
		return toBytes(dest, 0, false);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest, int pos) {
		return toBytes(dest, pos, false);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest, int pos, boolean le) {
		if (le) {
			pos += Helpers.floatToBytesL(x, dest, pos);
			pos += Helpers.floatToBytesL(y, dest, pos);
			pos += Helpers.floatToBytesL(z, dest, pos);
			pos += Helpers.floatToBytesL(s, dest, pos);
		} else {
			pos += Helpers.floatToBytesB(x, dest, pos);
			pos += Helpers.floatToBytesB(y, dest, pos);
			pos += Helpers.floatToBytesB(z, dest, pos);
			pos += Helpers.floatToBytesB(s, dest, pos);
		}
		return 16;
	}

	static public Vector4 parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		return new Vector4(parser);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(z)).endTag(namespace, "Z");
		writer.startTag(namespace, "S").text(Float.toString(s)).endTag(namespace, "S");
		writer.startTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", z)).endTag(namespace, "Z");
		writer.startTag(namespace, "S").text(String.format(locale, "%f", s)).endTag(namespace, "S");
		writer.startTag(namespace, name);
	}

	@Override
	public String toString() {
		return String.format(Helpers.EnUsCulture, "<%.3f, %.3f, %.3f, %.3f>", x, y, z, s);
	}

	public boolean equals(Vector4 val) {
		return val != null && x == val.x && y == val.y && z == val.z && s == val.s;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && (obj instanceof Vector4) && equals((Vector4) obj);
	}

	@Override
	public int hashCode() {
		int hashCode = ((Float) x).hashCode();
		hashCode = hashCode * 31 + ((Float) y).hashCode();
		hashCode = hashCode * 31 + ((Float) z).hashCode();
		hashCode = hashCode * 31 + ((Float) s).hashCode();
		return hashCode;
	}

}
