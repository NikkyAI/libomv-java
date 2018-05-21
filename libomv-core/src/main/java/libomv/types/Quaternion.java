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

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class Quaternion {

	public enum Order {
		XYZ, YZX, ZXY, YXZ, XZY, ZYX;
	}

	/** A quaternion with a value of 0,0,0,0 */
	public final static Quaternion ZERO = new Quaternion(0f, 0f, 0f, 0f);
	/** A quaternion with a value of 0,0,0,1 */
	public final static Quaternion IDENTITY = new Quaternion(0f, 0f, 0f, 1f);

	private static float DEG_TO_RAD = 0.017453292519943295769236907684886f;

	public float x;

	public float y;

	public float z;

	public float w;

	public Quaternion() {
		this.x = this.y = this.z = 0.0f;
		this.w = 1.0f;
	}

	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Build a quaternion from normalized float values
	 *
	 * @param x
	 *            X value from -1.0 to 1.0
	 * @param y
	 *            Y value from -1.0 to 1.0
	 * @param z
	 *            Z value from -1.0 to 1.0
	 */
	public Quaternion(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;

		float xyzsum = 1 - x * x - y * y - z * z;
		this.w = (xyzsum > 0) ? (float) Math.sqrt(xyzsum) : 0;
	}

	public Quaternion(Vector3 vectorPart, float scalarPart) {
		this.x = vectorPart.x;
		this.y = vectorPart.y;
		this.z = vectorPart.z;
		this.w = scalarPart;
	}

	public Quaternion(Matrix4 mat) {
		mat.getQuaternion(this).normalize();
	}

	/**
	 * Constructor, builds a quaternion object from a byte array
	 *
	 * @param byteArray
	 *            Byte array containing four four-byte floats
	 * @param pos
	 *            Offset in the byte array to start reading at
	 * @param normalized
	 *            Whether the source data is normalized or not. If this is true 12
	 *            bytes will be read, otherwise 16 bytes will be read.
	 */
	public Quaternion(byte[] byteArray, int pos, boolean normalized) {
		this.x = this.y = this.z = this.w = 0f;
		fromBytes(byteArray, pos, normalized, false);
	}

	public Quaternion(byte[] byteArray, int pos, boolean normalized, boolean le) {
		this.x = this.y = this.z = this.w = 0f;
		fromBytes(byteArray, pos, normalized, le);
	}

	public Quaternion(ByteBuffer byteArray, boolean normalized) {
		this.x = byteArray.getFloat();
		this.y = byteArray.getFloat();
		this.z = byteArray.getFloat();
		if (!normalized) {
			this.w = byteArray.getFloat();
		} else {
			float xyzsum = 1f - x * x - y * y - z * z;
			this.w = (xyzsum > 0f) ? (float) Math.sqrt(xyzsum) : 0;
		}
	}

	/**
	 * Constructor, builds a quaternion from an XML reader
	 *
	 * @param parser
	 *            XML pull parser reader
	 */
	public Quaternion(XmlPullParser parser) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Quaternion
		int eventType = parser.getEventType();
		if (eventType != XmlPullParser.START_TAG)
			throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(),
					parser, null);

		while (parser.nextTag() == XmlPullParser.START_TAG) {
			String name = parser.getName();
			if (name.equals("X")) {
				x = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equals("Y")) {
				y = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equals("Z")) {
				z = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equals("W")) {
				w = Helpers.tryParseFloat(parser.nextText().trim());
			} else {
				Helpers.skipElement(parser);
			}
		}
	}

	public Quaternion(Quaternion q) {
		this.x = q.x;
		this.y = q.y;
		this.z = q.z;
		this.w = q.w;
	}

	public boolean approxEquals(Quaternion quat, float tolerance) {
		Quaternion diff = subtract(quat);
		return (diff.lengthSquared() <= tolerance * tolerance);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public float lengthSquared() {
		return (x * x + y * y + z * z + w * w);
	}

	/** Normalizes the quaternion */
	public Quaternion normalize() {
		float mag = length();
		// Catch very small rounding errors when normalizing
		if (mag > Helpers.FLOAT_MAG_THRESHOLD) {
			return divide(mag);
		}
		x = 0f;
		y = 0f;
		z = 0f;
		w = 1f;
		return this;
	}

	public Vector3 toVector3() {
		if (w >= 0) {
			return new Vector3(x, y, z);
		}
		return new Vector3(-x, -y, -z);
	}

	/**
	 * Normalize this quaternion and serialize it to a byte array
	 *
	 * @return A 12 byte array containing normalized X, Y, and Z floating point
	 *         values in order using little endian byte ordering
	 * @throws Exception
	 */
	public byte[] getBytes() throws Exception {
		byte[] bytes = new byte[12];
		toBytes(bytes, 0, false);
		return bytes;
	}

	/**
	 * Writes the normalized data for this quaternion to a ByteBuffer
	 *
	 * @param bytes
	 *            The ByteBuffer to copy the 12 bytes for X, Y, and Z
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(ByteBuffer bytes) throws Exception {
		float norm = (float) Math.sqrt(x * x + y * y + z * z + w * w);

		if (norm != 0) {
			norm = 1f / norm;

			float tx;
			float ty;
			float tz;
			if (w >= 0f) {
				tx = x;
				ty = y;
				tz = z;
			} else {
				tx = -x;
				ty = -y;
				tz = -z;
			}
			bytes.putFloat(norm * tx);
			bytes.putFloat(norm * ty);
			bytes.putFloat(norm * tz);
		} else {
			throw new Exception("Quaternion <" + x + "," + y + "," + z + "," + w + "> normalized to zero");
		}
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 *
	 * @param stream
	 *            OutputStream to copy the 16 bytes for X, Y, Z, and W
	 * @param le
	 *            True for writing little endian data
	 * @throws IOException
	 */
	public void write(OutputStream stream, boolean le) throws IOException {
		if (le) {
			stream.write(Helpers.floatToBytesL(x));
			stream.write(Helpers.floatToBytesL(y));
			stream.write(Helpers.floatToBytesL(z));
			stream.write(Helpers.floatToBytesL(w));
		} else {
			stream.write(Helpers.floatToBytesB(x));
			stream.write(Helpers.floatToBytesB(y));
			stream.write(Helpers.floatToBytesB(z));
			stream.write(Helpers.floatToBytesB(w));
		}
	}

	static public Quaternion parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		return new Quaternion(parser);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(z)).endTag(namespace, "Z");
		writer.startTag(namespace, "W").text(Float.toString(w)).endTag(namespace, "W");
		writer.endTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", x)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", z)).endTag(namespace, "Z");
		writer.startTag(namespace, "W").text(String.format(locale, "%f", w)).endTag(namespace, "W");
		writer.endTag(namespace, name);
	}

	/**
	 * Get a formatted string representation of the vector
	 *
	 * @return A string representation of the vector
	 */
	@Override
	public String toString() {
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f, %f>", x, y, z, w);
	}

	@Override
	public int hashCode() {
		int hashCode = ((Float) x).hashCode();
		hashCode = hashCode * 31 + ((Float) y).hashCode();
		hashCode = hashCode * 31 + ((Float) z).hashCode();
		hashCode = hashCode * 31 + ((Float) w).hashCode();
		return hashCode;
	}

	/**
	 * Builds a quaternion object from a byte array
	 *
	 * @param byteArray
	 *            The source byte array
	 * @param pos
	 *            Offset in the byte array to start reading at
	 * @param normalized
	 *            Whether the source data is normalized or not. If this is true 12
	 *            bytes will be read, otherwise 16 bytes will be read.
	 */
	public void fromBytes(byte[] bytes, int pos, boolean normalized, boolean le) {
		if (le) {
			/* Little endian architecture */
			x = Helpers.bytesToFloatL(bytes, pos + 0);
			y = Helpers.bytesToFloatL(bytes, pos + 4);
			z = Helpers.bytesToFloatL(bytes, pos + 8);
			if (!normalized) {
				w = Helpers.bytesToFloatL(bytes, pos + 12);
			}
		} else {
			x = Helpers.bytesToFloatB(bytes, pos + 0);
			y = Helpers.bytesToFloatB(bytes, pos + 4);
			z = Helpers.bytesToFloatB(bytes, pos + 8);
			if (!normalized) {
				w = Helpers.bytesToFloatB(bytes, pos + 12);
			}
		}
		if (normalized) {
			float xyzsum = 1f - x * x - y * y - z * z;
			w = (xyzsum > 0f) ? (float) Math.sqrt(xyzsum) : 0f;
		}
	}

	/**
	 * Writes the raw bytes for this quaternion to a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 12 bytes before the end of the array
	 * @throws Exception
	 */
	public void toBytes(byte[] dest, int pos, boolean le) throws Exception {
		float norm = x * x + y * y + z * z + w * w;

		if (norm >= 0.001f) {
			norm = (float) (1 / Math.sqrt(norm));

			float tx;
			float ty;
			float tz;
			if (w >= 0f) {
				tx = x;
				ty = y;
				tz = z;
			} else {
				tx = -x;
				ty = -y;
				tz = -z;
			}

			if (le) {
				Helpers.floatToBytesL(norm * tx, dest, pos + 0);
				Helpers.floatToBytesL(norm * ty, dest, pos + 4);
				Helpers.floatToBytesL(norm * tz, dest, pos + 8);
			} else {
				Helpers.floatToBytesB(norm * tx, dest, pos + 0);
				Helpers.floatToBytesB(norm * ty, dest, pos + 4);
				Helpers.floatToBytesB(norm * tz, dest, pos + 8);
			}
		} else {
			throw new Exception(String.format("Quaternion %s normalized to zero", toString()));
		}
	}

	/**
	 * Convert this quaternion to euler angles
	 *
	 * Note: according to
	 * http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/
	 *
	 * @return a Vector with the 3 angles roll, pitch, yaw in this order
	 */
	public Vector3 toEuler() {
		float sqx = x * x;
		float sqy = y * y;
		float sqz = z * z;
		float sqw = w * w;

		// Unit will be a correction factor if the quaternion is not normalized
		float unit = sqx + sqy + sqz + sqw;
		if (unit < 0.001)
			return Vector3.ZERO;
		double test = x * y + z * w;

		if (test > 0.499f * unit) {
			// Singularity at north pole
			return new Vector3(0f, (float) (Math.PI / 2.0), 2f * (float) Math.atan2(x, w));
		} else if (test < -0.499f * unit) {
			// Singularity at south pole
			return new Vector3(0f, -(float) (Math.PI / 2.0), -2f * (float) Math.atan2(x, w));
		}
		return new Vector3((float) Math.atan2(2f * x * w - 2f * y * z, -sqx + sqy - sqz + sqw),
				(float) Math.asin(2f * test / unit),
				(float) Math.atan2(2f * y * w - 2f * x * z, sqx - sqy - sqz + sqw));
	}

	/**
	 * Convert this quaternion to an angle around an axis
	 *
	 * @param axis
	 *            Unit vector describing the axis
	 * @param angle
	 *            Angle around the axis, in radians
	 */
	public void getAxisAngle(RefObject<Vector3> axis, RefObject<Float> angle) {
		Quaternion q = this.normalize();
		float sin = (float) Math.sqrt(1.0f - q.w * q.w);
		if (sin >= 0.001) {
			float invSin = 1.0f / sin;
			if (q.w < 0)
				invSin = -invSin;
			axis.argvalue = new Vector3(q.x, q.y, q.z).multiply(invSin);
			angle.argvalue = 2.0f * (float) Math.acos(q.w);
			if (angle.argvalue > Math.PI)
				angle.argvalue = 2.0f * (float) Math.PI - angle.argvalue;
		} else {
			axis.argvalue = Vector3.UNIT_X;
			angle.argvalue = 0f;
		}
	}

	/**
	 * Build a quaternion from an axis and an angle of rotation around that axis
	 */
	public static Quaternion createFromAxisAngle(float axisX, float axisY, float axisZ, float angle) {
		Vector3 axis = new Vector3(axisX, axisY, axisZ);
		return createFromAxisAngle(axis, angle);
	}

	/**
	 * Build a quaternion from an axis and an angle of rotation around that axis
	 *
	 * @param axis
	 *            Axis of rotation
	 * @param angle
	 *            Angle of rotation
	 */
	public static Quaternion createFromAxisAngle(Vector3 axis, float angle) {
		axis = Vector3.normalize(axis);

		angle *= 0.5;
		float s = (float) Math.sin(angle);

		return new Quaternion(axis.x * s, axis.y * s, axis.z * s, (float) Math.cos(angle)).normalize();
	}

	/**
	 * Creates a quaternion from a vector containing roll, pitch, and yaw in radians
	 *
	 * @param eulers
	 *            Vector representation of the euler angles in radians
	 * @return Quaternion representation of the euler angles
	 * @throws Exception
	 */
	public static Quaternion createFromEulers(Vector3 eulers) throws Exception {
		return createFromEulers(eulers.x, eulers.y, eulers.z);
	}

	/**
	 * Creates a quaternion from roll, pitch, and yaw euler angles in radians
	 *
	 * @param roll
	 *            X angle in radians
	 * @param pitch
	 *            Y angle in radians
	 * @param yaw
	 *            Z angle in radians
	 * @return Quaternion representation of the euler angles
	 * @throws Exception
	 */
	public static Quaternion createFromEulers(float roll, float pitch, float yaw) throws Exception {
		if (roll > Helpers.TWO_PI || pitch > Helpers.TWO_PI || yaw > Helpers.TWO_PI) {
			throw new Exception("Euler angles must be in radians");
		}

		double atCos = Math.cos(roll / 2f);
		double atSin = Math.sin(roll / 2f);
		double leftCos = Math.cos(pitch / 2f);
		double leftSin = Math.sin(pitch / 2f);
		double upCos = Math.cos(yaw / 2f);
		double upSin = Math.sin(yaw / 2f);
		double atLeftCos = atCos * leftCos;
		double atLeftSin = atSin * leftSin;
		return new Quaternion((float) (atSin * leftCos * upCos + atCos * leftSin * upSin),
				(float) (atCos * leftSin * upCos - atSin * leftCos * upSin),
				(float) (atLeftCos * upSin + atLeftSin * upCos), (float) (atLeftCos * upCos - atLeftSin * upSin));
	}

	public static Quaternion createFromRotationMatrix(Matrix4 matrix) {
		Quaternion quaternion = new Quaternion();
		quaternion.setFromRotationMatrix(matrix);
		return quaternion;
	}

	public static float dot(Quaternion quaternion1, Quaternion quaternion2) {
		return quaternion1.dot(quaternion2);
	}

	/**
	 * Conjugates and renormalizes a vector
	 */
	public static Quaternion inverse(Quaternion quaternion) {
		float norm = quaternion.lengthSquared();

		if (norm == 0f) {
			quaternion.x = quaternion.y = quaternion.z = quaternion.w = 0f;
		} else {
			float oonorm = 1f / norm;
			quaternion = conjugate(quaternion);

			quaternion.x *= oonorm;
			quaternion.y *= oonorm;
			quaternion.z *= oonorm;
			quaternion.w *= oonorm;
		}
		return quaternion;
	}

	// linear interpolation from identity to q
	public static Quaternion lerp(Quaternion q, float t) {
		return new Quaternion(t * q.x, t * q.y, t * q.z, t * (q.z - 1f) + 1f).normalize();
	}

	/* linear interpolation between two quaternions */
	public static Quaternion lerp(Quaternion q1, Quaternion q2, float t) {
		float inv_t = 1.f - t;
		return new Quaternion(t * q2.x + inv_t * q1.x, t * q2.y + inv_t * q1.y, t * q2.z + inv_t * q1.z,
				t * q2.w + inv_t * q1.w).normalize();
	}

	/** Spherical linear interpolation between two quaternions */
	public static Quaternion slerp(Quaternion q1, Quaternion q2, float amount) {
		float angle = dot(q1, q2);

		if (angle < 0f) {
			q1 = multiply(q1, -1f);
			angle *= -1f;
		}

		float scale;
		float invscale;

		if ((angle + 1f) > 0.05f) {
			if ((1f - angle) >= 0.05f) {
				// slerp
				float theta = (float) Math.acos(angle);
				float invsintheta = 1f / (float) Math.sin(theta);
				scale = (float) Math.sin(theta * (1f - amount)) * invsintheta;
				invscale = (float) Math.sin(theta * amount) * invsintheta;
			} else {
				// lerp
				scale = 1f - amount;
				invscale = amount;
			}
		} else {
			q2.x = -q1.y;
			q2.y = q1.x;
			q2.z = -q1.w;
			q2.w = q1.z;

			scale = (float) Math.sin(Math.PI * (0.5f - amount));
			invscale = (float) Math.sin(Math.PI * amount);
		}
		return new Quaternion(q1.x * scale + q2.x * invscale, q1.y * scale + q2.y * invscale,
				q1.z * scale + q2.z * invscale, q1.w * scale + q2.w * invscale);
	}

	public static Quaternion normalize(Quaternion quaternion) {
		return new Quaternion(quaternion).normalize();
	}

	public static Quaternion parse(String val) {
		String splitChar = ",";
		String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		if (split.length == 3) {
			return new Quaternion(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
					Float.parseFloat(split[2].trim()));
		}
		return new Quaternion(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
				Float.parseFloat(split[2].trim()), Float.parseFloat(split[3].trim()));
	}

	public static boolean tryParse(String val, RefObject<Quaternion> result) {
		try {
			result.argvalue = parse(val);
			return true;
		} catch (Throwable t) {
			result.argvalue = new Quaternion();
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof Quaternion && equals((Quaternion) obj);
	}

	public boolean equals(Quaternion other) {
		return other != null && w == other.w && x == other.x && y == other.y && z == other.z;
	}

	public boolean isIdentity() {
		return (x == 0f && y == 0f && z == 0f && w == 1f);
	}

	public boolean isZero() {
		return equals(ZERO);
	}

	public static boolean isZero(Quaternion q) {
		if (q != null)
			return q.equals(ZERO);
		return false;
	}

	public static boolean isZeroOrNull(Quaternion q) {
		if (q != null)
			return q.equals(ZERO);
		return true;
	}

	public Quaternion negate() {
		x = -x;
		y = -y;
		z = -z;
		w = -w;
		return this;
	}

	/** Returns the conjugate (spatial inverse) of a quaternion */
	public Quaternion conjugate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Quaternion add(Quaternion quaternion) {
		x += quaternion.x;
		y += quaternion.y;
		z += quaternion.z;
		w += quaternion.w;
		return this;
	}

	public Quaternion subtract(Quaternion quaternion) {
		x -= quaternion.x;
		y -= quaternion.y;
		z -= quaternion.z;
		w -= quaternion.w;
		return this;
	}

	public Quaternion multiply(float scaleFactor) {
		x *= scaleFactor;
		y *= scaleFactor;
		z *= scaleFactor;
		w *= scaleFactor;
		return this;
	}

	public Quaternion multiply(Quaternion quaternion) {
		float tx = (w * quaternion.x) + (x * quaternion.w) + (y * quaternion.z) - (z * quaternion.y);
		float ty = (w * quaternion.y) - (x * quaternion.z) + (y * quaternion.w) + (z * quaternion.x);
		float tz = (w * quaternion.z) + (x * quaternion.y) - (y * quaternion.x) + (z * quaternion.w);
		float tw = (w * quaternion.w) - (x * quaternion.x) - (y * quaternion.y) - (z * quaternion.z);
		x = tx;
		y = ty;
		z = tz;
		w = tw;
		return this;
	}

	public Vector4 multiply(Vector4 vector) {
		float rw = -x * vector.x - y * vector.y - z * vector.z;
		float rx = w * vector.x + y * vector.z - z * vector.y;
		float ry = w * vector.y + z * vector.x - x * vector.z;
		float rz = w * vector.z + x * vector.y - y * vector.x;

		float nx = -rw * x + rx * w - ry * z + rz * y;
		float ny = -rw * y + ry * w - rz * x + rx * z;
		float nz = -rw * z + rz * w - rx * y + ry * x;
		return new Vector4(nx, ny, nz, vector.s);
	}

	public Vector3 multiply(Vector3 vector) {
		float rw = -x * vector.x - y * vector.y - z * vector.z;
		float rx = w * vector.x + y * vector.z - z * vector.y;
		float ry = w * vector.y + z * vector.x - x * vector.z;
		float rz = w * vector.z + x * vector.y - y * vector.x;

		float nx = -rw * x + rx * w - ry * z + rz * y;
		float ny = -rw * y + ry * w - rz * x + rx * z;
		float nz = -rw * z + rz * w - rx * y + ry * x;
		return new Vector3(nx, ny, nz);
	}

	public Vector3d multiply(Vector3d vector) {
		double rw = -x * vector.x - y * vector.y - z * vector.z;
		double rx = w * vector.x + y * vector.z - z * vector.y;
		double ry = w * vector.y + z * vector.x - x * vector.z;
		double rz = w * vector.z + x * vector.y - y * vector.x;

		double nx = -rw * x + rx * w - ry * z + rz * y;
		double ny = -rw * y + ry * w - rz * x + rx * z;
		double nz = -rw * z + rz * w - rx * y + ry * x;
		return new Vector3d(nx, ny, nz);
	}

	public Quaternion divide(float divider) {
		divider = 1f / divider;
		x *= divider;
		y *= divider;
		z *= divider;
		w *= divider;
		return this;
	}

	public Quaternion divide(Quaternion quaternion) {
		return inverse().multiply(quaternion);
	}

	public float dot(Quaternion quaternion) {
		return (x * quaternion.x) + (y * quaternion.y) + (z * quaternion.z) + (w * quaternion.w);
	}

	public Quaternion inverse() {
		float norm = lengthSquared();

		if (norm == 0f) {
			x = y = z = w = 0f;
		} else {
			conjugate().divide(norm);
		}
		return this;
	}

	public void setFromRotationMatrix(Matrix4 matrix) {
		float num = (matrix.M11 + matrix.M22) + matrix.M33;
		if (num > 0f) {
			num = (float) Math.sqrt(num + 1f);
			w = num * 0.5f;
			num = 0.5f / num;
			x = (matrix.M23 - matrix.M32) * num;
			y = (matrix.M31 - matrix.M13) * num;
			z = (matrix.M12 - matrix.M21) * num;
		} else if ((matrix.M11 >= matrix.M22) && (matrix.M11 >= matrix.M33)) {
			num = (float) Math.sqrt(1f + matrix.M11 - matrix.M22 - matrix.M33);
			x = 0.5f * num;
			num = 0.5f / num;
			y = (matrix.M12 + matrix.M21) * num;
			z = (matrix.M13 + matrix.M31) * num;
			w = (matrix.M23 - matrix.M32) * num;
		} else if (matrix.M22 > matrix.M33) {
			num = (float) Math.sqrt(1f + matrix.M22 - matrix.M11 - matrix.M33);
			y = 0.5f * num;
			num = 0.5f / num;
			x = (matrix.M21 + matrix.M12) * num;
			z = (matrix.M32 + matrix.M23) * num;
			w = (matrix.M31 - matrix.M13) * num;
		} else {
			num = (float) Math.sqrt(1f + matrix.M33 - matrix.M11 - matrix.M22);
			z = 0.5f * num;
			num = 0.5f / num;
			x = (matrix.M31 + matrix.M13) * num;
			y = (matrix.M32 + matrix.M23) * num;
			w = (matrix.M12 - matrix.M21) * num;
		}
	}

	public static Quaternion negate(Quaternion quaternion) {
		return new Quaternion(quaternion).negate();
	}

	/** Returns the conjugate (spatial inverse) of a quaternion */
	public static Quaternion conjugate(Quaternion quaternion) {
		return new Quaternion(quaternion).conjugate();
	}

	public static Quaternion add(Quaternion quaternion1, Quaternion quaternion2) {

		return new Quaternion(quaternion1).add(quaternion2);
	}

	public static Quaternion subtract(Quaternion quaternion1, Quaternion quaternion2) {
		return new Quaternion(quaternion1).subtract(quaternion2);
	}

	public static Quaternion multiply(Quaternion quaternion1, Quaternion quaternion2) {
		return new Quaternion(quaternion1).multiply(quaternion2);
	}

	public static Quaternion multiply(Quaternion quaternion, float scaleFactor) {
		return new Quaternion(quaternion).multiply(scaleFactor);
	}

	public static Vector4 multiply(Quaternion rot, Vector4 vector) {
		return rot.multiply(vector);
	}

	public static Vector3 multiply(Quaternion rot, Vector3 vector) {
		return rot.multiply(vector);
	}

	public static Vector3d multiply(Quaternion rot, Vector3d vector) {
		return rot.multiply(vector);
	}

	public static Quaternion divide(Quaternion quaternion1, Quaternion quaternion2) {
		return new Quaternion(quaternion1).divide(quaternion2);
	}

	// calculate the shortest rotation from a to b
	public static Quaternion shortestArc(Vector3 a, Vector3 b) {
		// Make a local copy of both vectors.
		Vector3 vec_a = new Vector3(a);
		Vector3 vec_b = new Vector3(b);

		// Make sure neither vector is zero length. Also normalize
		// the vectors while we are at it.
		float vec_a_mag = vec_a.normalize().mag();
		float vec_b_mag = vec_b.normalize().mag();
		if (vec_a_mag < Helpers.FLOAT_MAG_THRESHOLD || vec_b_mag < Helpers.FLOAT_MAG_THRESHOLD) {
			// Can't calculate a rotation from this.
			// Just return ZERO_ROTATION instead.
			return IDENTITY;
		}

		// Create an axis to rotate around, and the cos of the angle to rotate.
		Vector3 axis = Vector3.cross(vec_a, vec_b);
		float cos_theta = Vector3.dot(vec_a, vec_b);

		// Check the angle between the vectors to see if they are parallel or
		// anti-parallel.
		if (cos_theta > 1.0 - Helpers.FLOAT_MAG_THRESHOLD) {
			// a and b are parallel. No rotation is necessary.
			return IDENTITY;
		} else if (cos_theta < -1.0 + Helpers.FLOAT_MAG_THRESHOLD) {
			// a and b are anti-parallel.
			// Rotate 180 degrees around some orthogonal axis.
			// Find the projection of the x-axis onto a, and try
			// using the vector between the projection and the x-axis
			// as the orthogonal axis.
			Vector3 proj = vec_a.multiply(vec_a.x / cos_theta);
			Vector3 ortho_axis = Vector3.subtract(Vector3.UNIT_X, proj);

			// Turn this into an orthonormal axis.
			float ortho_length = ortho_axis.normalize().length();
			// If the axis' length is 0, then our guess at an orthogonal axis
			// was wrong (a is parallel to the x-axis).
			if (ortho_length < Helpers.FLOAT_MAG_THRESHOLD) {
				// Use the z-axis instead.
				ortho_axis = Vector3.UNIT_Z;
			}

			// Construct a quaternion from this orthonormal axis.
			return new Quaternion(ortho_axis.x, ortho_axis.y, ortho_axis.z, 0f);
		} else {
			// a and b are NOT parallel or anti-parallel.
			// Return the rotation between these vectors.
			return createFromAxisAngle(axis, (float) Math.acos(cos_theta));
		}
	}

	/**
	 * Creates a quaternion from maya's rotation representation, which is 3
	 * rotations (in DEGREES) with specified order.
	 *
	 * @param xRot
	 *            X Rotation value
	 * @param yRot
	 *            Y Rotation value
	 * @param zRot
	 *            Z Rotation value
	 * @param order
	 *            the order of the rotational values
	 * @returns a quaternion representing the 3 rotation values in the defined order
	 */
	public static Quaternion mayaQ(float xRot, float yRot, float zRot, Order order) {
		Quaternion xQ = new Quaternion(new Vector3(1.0f, 0.0f, 0.0f), xRot * DEG_TO_RAD);
		Quaternion yQ = new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), yRot * DEG_TO_RAD);
		Quaternion zQ = new Quaternion(new Vector3(0.0f, 0.0f, 1.0f), zRot * DEG_TO_RAD);
		Quaternion ret = null;
		switch (order) {
		case XYZ:
			ret = multiply(multiply(xQ, yQ), zQ);
			break;
		case YZX:
			ret = multiply(multiply(yQ, zQ), xQ);
			break;
		case ZXY:
			ret = multiply(multiply(zQ, xQ), yQ);
			break;
		case XZY:
			ret = multiply(multiply(xQ, zQ), yQ);
			break;
		case YXZ:
			ret = multiply(multiply(yQ, xQ), zQ);
			break;
		case ZYX:
			ret = multiply(multiply(zQ, yQ), xQ);
			break;
		default:
			break;
		}
		return ret;
	}

	public static Quaternion mayaQ(float[] arr, int pos, Order order) {
		return mayaQ(arr[pos], arr[pos + 1], arr[pos + 2], order);
	}

	public static String orderToString(Order order) {
		String p;
		switch (order) {
		default:
		case XYZ:
			p = "XYZ";
			break;
		case YZX:
			p = "YZX";
			break;
		case ZXY:
			p = "ZXY";
			break;
		case XZY:
			p = "XZY";
			break;
		case YXZ:
			p = "YXZ";
			break;
		case ZYX:
			p = "ZYX";
			break;
		}
		return p;
	}

	public static Order stringToOrder(String str) {
		if (str.compareToIgnoreCase("XYZ") == 0)
			return Order.XYZ;

		if (str.compareToIgnoreCase("YZX") == 0)
			return Order.YZX;

		if (str.compareToIgnoreCase("ZXY") == 0)
			return Order.ZXY;

		if (str.compareToIgnoreCase("XZY") == 0)
			return Order.XZY;

		if (str.compareToIgnoreCase("YXZ") == 0)
			return Order.YXZ;

		if (str.compareToIgnoreCase("ZYX") == 0)
			return Order.ZYX;

		return Order.XYZ;
	}

	public static Order stringToOrderRev(String str) {
		return Order.values()[5 - stringToOrder(str).ordinal()];
	}

}
