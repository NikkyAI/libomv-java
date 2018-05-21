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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;

public class Vector3d {

	/** A vector with a value of 0,0,0 */
	public final static Vector3d ZERO = new Vector3d(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector3d ONE = new Vector3d(1d, 1d, 1d);
	/** A unit vector facing forward (X axis), value 1,0,0 */
	public final static Vector3d UNIT_X = new Vector3d(1d, 0d, 0d);
	/** A unit vector facing left (Y axis), value 0,1,0 */
	public final static Vector3d UNIT_Y = new Vector3d(0d, 1d, 0d);
	/** A unit vector facing up (Z axis), value 0,0,1 */
	public final static Vector3d UNIT_Z = new Vector3d(0d, 0d, 1d);

	public double x;

	public double y;

	public double z;

	public Vector3d(double val) {
		x = y = z = val;
	}

	public Vector3d(Vector3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	public Vector3d(Vector3d vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3d(byte[] bytes, int offset) {
		x = y = z = 0f;
		fromBytes(bytes, offset, false);
	}

	public Vector3d(byte[] bytes, int offset, boolean le) {
		x = y = z = 0f;
		fromBytes(bytes, offset, le);
	}

	public Vector3d(ByteBuffer byteArray) {
		x = byteArray.getDouble();
		y = byteArray.getDouble();
		z = byteArray.getDouble();
	}

	/**
	 * Constructor, builds a vector from an XML reader
	 *
	 * @param parser
	 *            XML pull parser reader
	 */
	public Vector3d(XmlPullParser parser) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Vector3
		int eventType = parser.getEventType();
		if (eventType != XmlPullParser.START_TAG)
			throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(),
					parser, null);

		while (parser.nextTag() == XmlPullParser.START_TAG) {
			String name = parser.getName();
			if (name.equalsIgnoreCase("X")) {
				x = Helpers.tryParseDouble(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("Y")) {
				y = Helpers.tryParseDouble(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("Z")) {
				z = Helpers.tryParseDouble(parser.nextText().trim());
			} else {
				Helpers.skipElement(parser);
			}
		}
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 *
	 * @param byteArray
	 *            buffer to copy the 24 bytes for X, Y, and Z
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(ByteBuffer byteArray) {
		byteArray.putDouble(x);
		byteArray.putDouble(y);
		byteArray.putDouble(z);
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
			stream.write(Helpers.doubleToBytesL(x));
			stream.write(Helpers.doubleToBytesL(y));
			stream.write(Helpers.doubleToBytesL(z));
		} else {
			stream.write(Helpers.doubleToBytesB(x));
			stream.write(Helpers.doubleToBytesB(y));
			stream.write(Helpers.doubleToBytesB(z));
		}
	}

	public static double distance(Vector3d value1, Vector3d value2) {
		return Math.sqrt(distanceSquared(value1, value2));
	}

	public static double distanceSquared(Vector3d value1, Vector3d value2) {
		return (value1.x - value2.x) * (value1.x - value2.x) + (value1.y - value2.y) * (value1.y - value2.y)
				+ (value1.z - value2.z) * (value1.z - value2.z);
	}

	public static Vector3d normalize(Vector3d value) {
		return new Vector3d(value).normalize();
	}

	public double length() {
		return Math.sqrt(distanceSquared(this, ZERO));
	}

	public double lengthSquared() {
		return distanceSquared(this, ZERO);
	}

	public Vector3d normalize() {
		double length = length();
		if (length > Helpers.FLOAT_MAG_THRESHOLD) {
			return divide(length);
		}
		x = 0f;
		y = 0f;
		z = 0f;
		return this;
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
			x = Helpers.bytesToDoubleL(bytes, pos + 0);
			y = Helpers.bytesToDoubleL(bytes, pos + 8);
			z = Helpers.bytesToDoubleL(bytes, pos + 16);
		} else {
			x = Helpers.bytesToDoubleB(bytes, pos + 0);
			y = Helpers.bytesToDoubleB(bytes, pos + 8);
			z = Helpers.bytesToDoubleB(bytes, pos + 16);
		}
	}

	/**
	 * Writes the raw bytes for this UUID to a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writeing. Must be at
	 *            least 16 bytes before the end of the array
	 */
	public int toBytes(byte[] dest, int pos) {
		return toBytes(dest, pos, false);
	}

	public int toBytes(byte[] dest, int pos, boolean le) {
		if (le) {
			Helpers.doubleToBytesL(x, dest, pos + 0);
			Helpers.doubleToBytesL(y, dest, pos + 4);
			Helpers.doubleToBytesL(z, dest, pos + 8);
		} else {
			Helpers.doubleToBytesB(x, dest, pos + 0);
			Helpers.doubleToBytesB(y, dest, pos + 4);
			Helpers.doubleToBytesB(z, dest, pos + 8);
		}
		return 24;
	}

	static public Vector3d parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		return new Vector3d(parser);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Double.toString(x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Double.toString(y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Double.toString(z)).endTag(namespace, "Z");
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

	@Override
	public int hashCode() {
		return ((Double) x).hashCode() * 31 * 31 + ((Double) y).hashCode() * 31 + ((Double) z).hashCode();
	}

	public Vector3d negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vector3d add(Vector3d val) {
		x += val.x;
		y += val.y;
		z += val.z;
		return this;
	}

	public Vector3d subtract(Vector3d val) {
		x -= val.x;
		y -= val.y;
		z -= val.z;
		return this;
	}

	public Vector3d multiply(double scaleFactor) {
		x *= scaleFactor;
		y *= scaleFactor;
		z *= scaleFactor;
		return this;
	}

	public Vector3d multiply(Vector3d value) {
		x *= value.x;
		y *= value.y;
		z *= value.z;
		return this;
	}

	public Vector3d divide(Vector3d value) {
		x /= value.x;
		y /= value.y;
		z /= value.z;
		return this;
	}

	public Vector3d divide(double divider) {
		double factor = 1d / divider;
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}

	public Vector3d cross(Vector3d value) {
		x = y * value.z - value.y * z;
		y = z * value.x - value.z * x;
		z = x * value.y - value.x * y;
		return this;
	}

}
