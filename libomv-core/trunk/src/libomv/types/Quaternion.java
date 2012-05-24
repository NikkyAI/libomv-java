/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
import java.nio.ByteBuffer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class Quaternion
{
	private static float DEG_TO_RAD = 0.017453292519943295769236907684886f;
	
	public float X;

	public float Y;

	public float Z;

	public float W;

	public Quaternion()
	{
		X = Y = Z = 0.0f;
		W = 1.0f;
	}

	public Quaternion(float x, float y, float z, float w)
	{
		X = x;
		Y = y;
		Z = z;
		W = w;
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
	public Quaternion(float x, float y, float z)
	{
		X = x;
		Y = y;
		Z = z;

		float xyzsum = 1 - X * X - Y * Y - Z * Z;
		W = (xyzsum > 0) ? (float) Math.sqrt(xyzsum) : 0;
	}

	public Quaternion(Vector3 vectorPart, float scalarPart)
	{
		X = vectorPart.X;
		Y = vectorPart.Y;
		Z = vectorPart.Z;
		W = scalarPart;
	}

	public Quaternion(Matrix4 mat)
	{
		mat.GetQuaternion(this).normalize();
	}
	/**
	 * Constructor, builds a quaternion object from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing four four-byte floats
	 * @param pos
	 *            Offset in the byte array to start reading at
	 * @param normalized
	 *            Whether the source data is normalized or not. If this is true
	 *            12 bytes will be read, otherwise 16 bytes will be read.
	 */
	public Quaternion(byte[] byteArray, int pos, boolean normalized)
	{
		X = Y = Z = W = 0f;
		FromBytes(byteArray, pos, normalized, false);
	}

	public Quaternion(byte[] byteArray, int pos, boolean normalized, boolean le)
	{
		X = Y = Z = W = 0f;
		FromBytes(byteArray, pos, normalized, le);
	}

	public Quaternion(ByteBuffer byteArray, boolean normalized)
	{
		X = byteArray.getFloat();
		Y = byteArray.getFloat();
		Z = byteArray.getFloat();
		if (!normalized)
		{
			W = byteArray.getFloat();
		}
		else
		{
			float xyzsum = 1f - X * X - Y * Y - Z * Z;
			W = (xyzsum > 0f) ? (float) Math.sqrt(xyzsum) : 0;
		}
	}

	/**
	 * Constructor, builds a quaternion from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
	public Quaternion(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		if (parser.nextTag() != XmlPullParser.START_TAG)
			throw new XmlPullParserException("Unexpected Tag: " + parser.getEventType(), parser, null);
		do
		{
			if (!parser.isEmptyElementTag())
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
				else if (name.equals("Z"))
				{
					Z = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else if (name.equals("W"))
				{
					W = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else
				{
					Helpers.skipElement(parser);
				}
			}
		}
		while (parser.nextTag() == XmlPullParser.START_TAG);
	}

	public Quaternion(Quaternion q)
	{
		X = q.X;
		Y = q.Y;
		Z = q.Z;
		W = q.W;
	}

	public boolean approxEquals(Quaternion quat, float tolerance)
	{
		Quaternion diff = subtract(quat);
		return (diff.lengthSquared() <= tolerance * tolerance);
	}

	public float length()
	{
		return (float) Math.sqrt(lengthSquared());
	}

	public float lengthSquared()
	{
		return (X * X + Y * Y + Z * Z + W * W);
	}

	/** Normalizes the quaternion */
	public Quaternion normalize()
	{
		return normalize(this);
	}

	/**
	 * Normalize this quaternion and serialize it to a byte array
	 * 
	 * @return A 12 byte array containing normalized X, Y, and Z floating point
	 *         values in order using little endian byte ordering
	 * @throws Exception
	 */
	public byte[] GetBytes() throws Exception
	{
		byte[] bytes = new byte[12];
		ToBytes(bytes, 0, false);
		return bytes;
	}

	/**
	 * Returns a ByteBuffer for this vector
	 * 
	 * @param byteArray
	 *            buffer to copye the 12 bytes for X, Y, and Z
	 * @throws Exception
	 */
	public void GetBytes(ByteBuffer bytes) throws Exception
	{
		float norm = (float) Math.sqrt(X * X + Y * Y + Z * Z + W * W);

		if (norm != 0)
		{
			norm = 1f / norm;

			float x, y, z;
			if (W >= 0f)
			{
				x = X;
				y = Y;
				z = Z;
			}
			else
			{
				x = -X;
				y = -Y;
				z = -Z;
			}
			bytes.putFloat(norm * x);
			bytes.putFloat(norm * y);
			bytes.putFloat(norm * z);
		}
		else
		{
			throw new Exception("Quaternion <" + X + "," + Y + "," + Z + "," + W + "> normalized to zero");
		}
	}

	static public Quaternion parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Quaternion(parser);
	}
	
	public void serialize(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(null, "X").text(Float.toString(X)).endTag(null, "X");
		writer.startTag(null, "Y").text(Float.toString(Y)).endTag(null, "Y");
		writer.startTag(null, "Z").text(Float.toString(Z)).endTag(null, "Z");
		writer.startTag(null, "W").text(Float.toString(W)).endTag(null, "W");
	}

	/**
	 * Get a formatted string representation of the vector
	 * 
	 * @return A string representation of the vector
	 */
	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f, %f>", X, Y, Z, W);
	}

	@Override
	public int hashCode()
	{
		return (((Float) X).hashCode() ^ ((Float) Y).hashCode() ^ ((Float) Z).hashCode() ^ ((Float) W).hashCode());
	}

	/**
	 * Builds a quaternion object from a byte array
	 * 
	 * @param byteArray
	 *            The source byte array
	 * @param pos
	 *            Offset in the byte array to start reading at
	 * @param normalized
	 *            Whether the source data is normalized or not. If this is true
	 *            12 bytes will be read, otherwise 16 bytes will be read.
	 */
	public void FromBytes(byte[] bytes, int pos, boolean normalized, boolean le)
	{
		if (le)
		{
			/* Little endian architecture */
			X = Helpers.BytesToFloatL(bytes, pos + 0);
			Y = Helpers.BytesToFloatL(bytes, pos + 4);
			Z = Helpers.BytesToFloatL(bytes, pos + 8);
			if (!normalized)
			{
				W = Helpers.BytesToFloatL(bytes, pos + 12);
			}
		}
		else
		{
			X = Helpers.BytesToFloatB(bytes, pos + 0);
			Y = Helpers.BytesToFloatB(bytes, pos + 4);
			Z = Helpers.BytesToFloatB(bytes, pos + 8);
			if (!normalized)
			{
				W = Helpers.BytesToFloatB(bytes, pos + 12);
			}
		}
		if (normalized)
		{
			float xyzsum = 1f - X * X - Y * Y - Z * Z;
			W = (xyzsum > 0f) ? (float) Math.sqrt(xyzsum) : 0f;
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
	public void ToBytes(byte[] dest, int pos, boolean le) throws Exception
	{
		float norm = (float) Math.sqrt(X * X + Y * Y + Z * Z + W * W);

		if (norm != 0f)
		{
			norm = 1f / norm;

			float x, y, z;
			if (W >= 0f)
			{
				x = X;
				y = Y;
				z = Z;
			}
			else
			{
				x = -X;
				y = -Y;
				z = -Z;
			}

			if (le)
			{
				Helpers.FloatToBytesL(norm * x, dest, pos + 0);
				Helpers.FloatToBytesL(norm * y, dest, pos + 4);
				Helpers.FloatToBytesL(norm * z, dest, pos + 8);
			}
			else
			{
				Helpers.FloatToBytesB(norm * x, dest, pos + 0);
				Helpers.FloatToBytesB(norm * y, dest, pos + 4);
				Helpers.FloatToBytesB(norm * z, dest, pos + 8);
			}
		}
		else
		{
			throw new Exception(String.format("Quaternion %s normalized to zero", toString()));
		}
	}

	/**
	 * Convert this quaternion to euler angles
	 * 
	 * @param roll
	 *            X euler angle
	 * @param pitch
	 *            Y euler angle
	 * @param yaw
	 *            Z euler angle
	 */
	public void getEulerAngles(RefObject<Float> roll, RefObject<Float> pitch, RefObject<Float> yaw)
	{
		float sqx = X * X;
		float sqy = Y * Y;
		float sqz = Z * Z;
		float sqw = W * W;

		// Unit will be a correction factor if the quaternion is not normalized
		float unit = sqx + sqy + sqz + sqw;
		double test = X * Y + Z * W;

		if (test > 0.499f * unit)
		{
			// Singularity at north pole
			yaw.argvalue = 2f * (float) Math.atan2(X, W);
			pitch.argvalue = (float) Math.PI / 2f;
			roll.argvalue = 0f;
		}
		else if (test < -0.499f * unit)
		{
			// Singularity at south pole
			yaw.argvalue = -2f * (float) Math.atan2(X, W);
			pitch.argvalue = -(float) Math.PI / 2f;
			roll.argvalue = 0f;
		}
		else
		{
			yaw.argvalue = (float) Math.atan2(2f * Y * W - 2f * X * Z, sqx - sqy - sqz + sqw);
			pitch.argvalue = (float) Math.asin(2f * test / unit);
			roll.argvalue = (float) Math.atan2(2f * X * W - 2f * Y * Z, -sqx + sqy - sqz + sqw);
		}
	}

	/**
	 * Convert this quaternion to an angle around an axis
	 * 
	 * @param axis
	 *            Unit vector describing the axis
	 * @param angle
	 *            Angle around the axis, in radians
	 */
	public void getAxisAngle(RefObject<Vector3> axis, RefObject<Float> angle)
	{
		axis.argvalue = new Vector3(0f);
		float scale = (float) Math.sqrt(X * X + Y * Y + Z * Z);

		if (scale < Helpers.FLOAT_MAG_THRESHOLD || W > 1.0f || W < -1.0f)
		{
			angle.argvalue = 0.0f;
			axis.argvalue.X = 0.0f;
			axis.argvalue.Y = 1.0f;
			axis.argvalue.Z = 0.0f;
		}
		else
		{
			angle.argvalue = 2.0f * (float) Math.acos(W);
			float ooscale = 1f / scale;
			axis.argvalue.X = X * ooscale;
			axis.argvalue.Y = Y * ooscale;
			axis.argvalue.Z = Z * ooscale;
		}
	}

	/**
	 * Build a quaternion from an axis and an angle of rotation around that axis
	 */
	public static Quaternion createFromAxisAngle(float axisX, float axisY, float axisZ, float angle)
	{
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
	public static Quaternion createFromAxisAngle(Vector3 axis, float angle)
	{
		Quaternion q = new Quaternion();
		axis = Vector3.normalize(axis);

		angle *= 0.5f;
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);

		q.X = axis.X * s;
		q.Y = axis.Y * s;
		q.Z = axis.Z * s;
		q.W = c;

		return normalize(q);
	}

	/**
	 * Creates a quaternion from a vector containing roll, pitch, and yaw in
	 * radians
	 * 
	 * @param eulers
	 *            Vector representation of the euler angles in radians
	 * @return Quaternion representation of the euler angles
	 * @throws Exception
	 */
	public static Quaternion createFromEulers(Vector3 eulers) throws Exception
	{
		return createFromEulers(eulers.X, eulers.Y, eulers.Z);
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
	public static Quaternion createFromEulers(float roll, float pitch, float yaw) throws Exception
	{
		if (roll > Helpers.TWO_PI || pitch > Helpers.TWO_PI || yaw > Helpers.TWO_PI)
		{
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
		return new Quaternion((float) (atSin * leftCos * upCos + atCos * leftSin * upSin), (float) (atCos * leftSin
				* upCos - atSin * leftCos * upSin), (float) (atLeftCos * upSin + atLeftSin * upCos), (float) (atLeftCos
				* upCos - atLeftSin * upSin));
	}

	public static Quaternion createFromRotationMatrix(Matrix4 m)
	{
		Quaternion quat = new Quaternion();

		float trace = m.Trace();

		if (trace > Helpers.FLOAT_MAG_THRESHOLD)
		{
			float s = (float) Math.sqrt(trace + 1f);
			quat.W = s * 0.5f;
			s = 0.5f / s;
			quat.X = (m.M23 - m.M32) * s;
			quat.Y = (m.M31 - m.M13) * s;
			quat.Z = (m.M12 - m.M21) * s;
		}
		else
		{
			if (m.M11 > m.M22 && m.M11 > m.M33)
			{
				float s = (float) Math.sqrt(1f + m.M11 - m.M22 - m.M33);
				quat.X = 0.5f * s;
				s = 0.5f / s;
				quat.Y = (m.M12 + m.M21) * s;
				quat.Z = (m.M13 + m.M31) * s;
				quat.W = (m.M23 - m.M32) * s;
			}
			else if (m.M22 > m.M33)
			{
				float s = (float) Math.sqrt(1f + m.M22 - m.M11 - m.M33);
				quat.Y = 0.5f * s;
				s = 0.5f / s;
				quat.X = (m.M21 + m.M12) * s;
				quat.Z = (m.M32 + m.M23) * s;
				quat.W = (m.M31 - m.M13) * s;
			}
			else
			{
				float s = (float) Math.sqrt(1f + m.M33 - m.M11 - m.M22);
				quat.Z = 0.5f * s;
				s = 0.5f / s;
				quat.X = (m.M31 + m.M13) * s;
				quat.Y = (m.M32 + m.M23) * s;
				quat.W = (m.M12 - m.M21) * s;
			}
		}

		return quat;
	}

	public static float dot(Quaternion q1, Quaternion q2)
	{
		return (q1.X * q2.X) + (q1.Y * q2.Y) + (q1.Z * q2.Z) + (q1.W * q2.W);
	}

	/**
	 * Conjugates and renormalizes a vector
	 */
	public static Quaternion inverse(Quaternion quaternion)
	{
		float norm = quaternion.lengthSquared();

		if (norm == 0f)
		{
			quaternion.X = quaternion.Y = quaternion.Z = quaternion.W = 0f;
		}
		else
		{
			float oonorm = 1f / norm;
			quaternion = conjugate(quaternion);

			quaternion.X *= oonorm;
			quaternion.Y *= oonorm;
			quaternion.Z *= oonorm;
			quaternion.W *= oonorm;
		}
		return quaternion;
	}

	// linear interpolation from identity to q
	public static Quaternion lerp(Quaternion q, float t)
	{
		return new Quaternion(t * q.X, t * q.Y, t * q.Z, t * (q.Z - 1f) + 1f).normalize();
	}

	/* linear interpolation between two quaternions */
	public static Quaternion lerp(Quaternion q1, Quaternion q2, float t)
	{
		float inv_t = 1.f - t;
		return new Quaternion(t * q2.X + inv_t * q1.X, t * q2.Y + inv_t * q1.Y,
		                              t * q2.Z + inv_t * q1.Z, t * q2.W + inv_t * q1.W).normalize();
	}

	/** Spherical linear interpolation between two quaternions */
	public static Quaternion slerp(Quaternion q1, Quaternion q2, float amount)
	{
		float angle = dot(q1, q2);

		if (angle < 0f)
		{
			q1 = multiply(q1, -1f);
			angle *= -1f;
		}

		float scale;
		float invscale;

		if ((angle + 1f) > 0.05f)
		{
			if ((1f - angle) >= 0.05f)
			{
				// slerp
				float theta = (float) Math.acos(angle);
				float invsintheta = 1f / (float) Math.sin(theta);
				scale = (float) Math.sin(theta * (1f - amount)) * invsintheta;
				invscale = (float) Math.sin(theta * amount) * invsintheta;
			}
			else
			{
				// lerp
				scale = 1f - amount;
				invscale = amount;
			}
		}
		else
		{
			q2.X = -q1.Y;
			q2.Y = q1.X;
			q2.Z = -q1.W;
			q2.W = q1.Z;

			scale = (float) Math.sin(Helpers.PI * (0.5f - amount));
			invscale = (float) Math.sin(Helpers.PI * amount);
		}
		return new Quaternion(q1.X * scale + q2.X * invscale, q1.Y * scale + q2.Y * invscale,
				              q1.Z * scale + q2.Z * invscale, q1.W * scale + q2.W * invscale);
	}

	public static Quaternion normalize(Quaternion q)
	{
		float mag = q.length();

		// Catch very small rounding errors when normalizing
		if (mag > Helpers.FLOAT_MAG_THRESHOLD)
		{
			float oomag = 1f / mag;
			q.X *= oomag;
			q.Y *= oomag;
			q.Z *= oomag;
			q.W *= oomag;
		}
		else
		{
			q.X = 0f;
			q.Y = 0f;
			q.Z = 0f;
			q.W = 1f;
		}
		return q;
	}

	public static Quaternion Parse(String val)
	{
		String splitChar = ",";
		String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		if (split.length == 3)
		{
			return new Quaternion(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
					Float.parseFloat(split[2].trim()));
		}
		return new Quaternion(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
				Float.parseFloat(split[2].trim()), Float.parseFloat(split[3].trim()));
	}

	public static boolean TryParse(String val, RefObject<Quaternion> result)
	{
		try
		{
			result.argvalue = Parse(val);
			return true;
		}
		catch (Throwable t)
		{
			result.argvalue = new Quaternion();
			return false;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Quaternion) ? equals((Quaternion)obj) : false;
	}

	public boolean equals(Quaternion other)
	{
		return W == other.W && X == other.X && Y == other.Y && Z == other.Z;
	}

	public boolean isIdentity()
	{
		return (X == 0f && Y == 0f && Z == 0f && W == 1f);
	}
	
	public static Quaternion negate(Quaternion quaternion)
	{
		return new Quaternion(-quaternion.X, -quaternion.Y, -quaternion.Z, -quaternion.W);
	}

	/** Returns the conjugate (spatial inverse) of a quaternion */
	public static Quaternion conjugate(Quaternion quaternion)
	{
		return new Quaternion(-quaternion.X, -quaternion.Y, -quaternion.Z, quaternion.W);
	}

	public Quaternion add(Quaternion q)
	{
		return add(this, q);
	}

	public Quaternion subtract(Quaternion q)
	{
		return subtract(this, q);
	}

	public Quaternion multiply(float scale)
	{
		return multiply(this, scale);
	}

	public Quaternion multiply(Quaternion q)
	{
		return multiply(this, q);
	}

	public Vector4 multiply(Vector4 a)
	{
		return multiply(this, a);
	}

	public Vector3 multiply(Vector3 a)
	{
		return multiply(this, a);
	}

	public Vector3d multiply(Vector3d a)
	{
		return multiply(this, a);
	}

	public Quaternion divide(Quaternion q)
	{
		return divide(this, q);
	}

	public static Quaternion add(Quaternion quaternion1, Quaternion quaternion2)
	{
		
		return new Quaternion(quaternion1.X + quaternion2.X, quaternion1.Y + quaternion2.Y,
		                      quaternion1.Z + quaternion2.Z, quaternion1.W + quaternion2.W);
	}

	public static Quaternion subtract(Quaternion quaternion1, Quaternion quaternion2)
	{
		return new Quaternion(quaternion1.X - quaternion2.X, quaternion1.Y - quaternion2.Y, 
				              quaternion1.Z - quaternion2.Z, quaternion1.W - quaternion2.W);
	}

	public static Quaternion multiply(Quaternion q1, Quaternion q2)
	{
		return new Quaternion((q1.W * q2.X) + (q1.X * q2.W) + (q1.Y * q2.Z) - (q1.Z * q2.Y), 
				              (q1.W * q2.Y) - (q1.X * q2.Z) + (q1.Y * q2.W) + (q1.Z * q2.X), 
				              (q1.W * q2.Z) + (q1.X * q2.Y) - (q1.Y * q2.X) + (q1.Z * q2.W),
				              (q1.W * q2.W) - (q1.X * q2.X) - (q1.Y * q2.Y) - (q1.Z * q2.Z));
	}

	public static Quaternion multiply(Quaternion quaternion, float scaleFactor)
	{	
		return new Quaternion(quaternion.X * scaleFactor, quaternion.Y * scaleFactor,
		                      quaternion.Z * scaleFactor, quaternion.W * scaleFactor);
	}

	public static Vector4 multiply(Quaternion rot, Vector4 a)
	{
	    float rw = - rot.X * a.X - rot.Y * a.Y - rot.Z * a.Z;
	    float rx =   rot.W * a.X + rot.Y * a.Z - rot.Z * a.Y;
	    float ry =   rot.W * a.Y + rot.Z * a.X - rot.X * a.Z;
	    float rz =   rot.W * a.Z + rot.X * a.Y - rot.Y * a.X;

	    float nx = - rw * rot.X +  rx * rot.W - ry * rot.Z + rz * rot.Y;
	    float ny = - rw * rot.Y +  ry * rot.W - rz * rot.X + rx * rot.Z;
	    float nz = - rw * rot.Z +  rz * rot.W - rx * rot.Y + ry * rot.X;

	    return new Vector4(nx, ny, nz, a.S);
	}

	public static Vector3 multiply(Quaternion rot, Vector3 a)
	{
		float rw = - rot.X * a.X - rot.Y * a.Y - rot.Z * a.Z;
		float rx =   rot.W * a.X + rot.Y * a.Z - rot.Z * a.Y;
		float ry =   rot.W * a.Y + rot.Z * a.X - rot.X * a.Z;
		float rz =   rot.W * a.Z + rot.X * a.Y - rot.Y * a.X;

		float nx = - rw * rot.X +  rx * rot.W - ry * rot.Z + rz * rot.Y;
		float ny = - rw * rot.Y +  ry * rot.W - rz * rot.X + rx * rot.Z;
		float nz = - rw * rot.Z +  rz * rot.W - rx * rot.Y + ry * rot.X;

	    return new Vector3(nx, ny, nz);
	}

	public static Vector3d multiply(Quaternion rot, Vector3d a)
	{
	    double rw = - rot.X * a.X - rot.Y * a.Y - rot.Z * a.Z;
	    double rx =   rot.W * a.X + rot.Y * a.Z - rot.Z * a.Y;
	    double ry =   rot.W * a.Y + rot.Z * a.X - rot.X * a.Z;
	    double rz =   rot.W * a.Z + rot.X * a.Y - rot.Y * a.X;

	    double nx = - rw * rot.X +  rx * rot.W - ry * rot.Z + rz * rot.Y;
	    double ny = - rw * rot.Y +  ry * rot.W - rz * rot.X + rx * rot.Z;
	    double nz = - rw * rot.Z +  rz * rot.W - rx * rot.Y + ry * rot.X;

	    return new Vector3d(nx, ny, nz);
	}

	public static Quaternion divide(Quaternion quaternion1, Quaternion quaternion2)
	{
		float q2lensq = quaternion2.lengthSquared();
		float x2 = quaternion2.X / q2lensq;
		float y2 = quaternion2.Y / q2lensq;
		float z2 = quaternion2.Z / q2lensq;
		float w2 = quaternion2.W / q2lensq;

		return new Quaternion((quaternion1.X * w2) - (quaternion1.W * x2) - (quaternion1.Y * z2) + (quaternion1.Z * y2),
				              (quaternion1.Y * w2) - (quaternion1.W * y2) - (quaternion1.Z * x2) + (quaternion1.X * z2),
				              (quaternion1.Z * w2) - (quaternion1.W * z2) - (quaternion1.X * y2) + (quaternion1.Y * x2), 
				              (quaternion1.W * w2) + (quaternion1.X * x2) + (quaternion1.Y * y2) + (quaternion1.Z * z2));
	}

	// calculate the shortest rotation from a to b
	public static Quaternion shortestArc(Vector3 a, Vector3 b)
	{
		// Make a local copy of both vectors.
		Vector3 vec_a = new Vector3(a);
		Vector3 vec_b = new Vector3(b);

		// Make sure neither vector is zero length.  Also normalize
		// the vectors while we are at it.
		float vec_a_mag = vec_a.normalize().mag();
		float vec_b_mag = vec_b.normalize().mag();
		if (vec_a_mag < Helpers.FLOAT_MAG_THRESHOLD ||
			vec_b_mag < Helpers.FLOAT_MAG_THRESHOLD)
		{
			// Can't calculate a rotation from this.
			// Just return ZERO_ROTATION instead.
			return Identity;
		}

		// Create an axis to rotate around, and the cos of the angle to rotate.
		Vector3 axis = Vector3.cross(vec_a, vec_b);
		float cos_theta  = Vector3.dot(vec_a, vec_b);

		// Check the angle between the vectors to see if they are parallel or anti-parallel.
		if (cos_theta > 1.0 - Helpers.FLOAT_MAG_THRESHOLD)
		{
			// a and b are parallel.  No rotation is necessary.
			return Identity;
		}
		else if (cos_theta < -1.0 + Helpers.FLOAT_MAG_THRESHOLD)
		{
			// a and b are anti-parallel.
			// Rotate 180 degrees around some orthogonal axis.
			// Find the projection of the x-axis onto a, and try
			// using the vector between the projection and the x-axis
			// as the orthogonal axis.
			Vector3 proj = vec_a.multiply(vec_a.X / cos_theta);
			Vector3 ortho_axis = Vector3.subtract(Vector3.UnitX, proj);
			
			// Turn this into an orthonormal axis.
			float ortho_length = ortho_axis.normalize().length();
			// If the axis' length is 0, then our guess at an orthogonal axis
			// was wrong (a is parallel to the x-axis).
			if (ortho_length < Helpers.FLOAT_MAG_THRESHOLD)
			{
				// Use the z-axis instead.
				ortho_axis = Vector3.UnitZ;
			}

			// Construct a quaternion from this orthonormal axis.
			return new Quaternion(ortho_axis.X, ortho_axis.Y, ortho_axis.Z, 0f);
		}
		else
		{
			// a and b are NOT parallel or anti-parallel.
			// Return the rotation between these vectors.
			return createFromAxisAngle(axis, (float) Math.acos(cos_theta));
		}
	}

	public enum Order
	{
		XYZ,
		YZX,
		ZXY,
		YXZ,
		XZY,
		ZYX;
	}

	/**
	 * Creates a quaternion from maya's rotation representation, which is 3 rotations (in DEGREES)
	 * with specified order.
	 * 
	 * @param xRot X Rotation value
	 * @param yRot Y Rotation value
	 * @param zRot Z Rotation value
	 * @param order the order of the rotational values
	 * @returns a quaternion representing the 3 rotation values in the defined order
	 */
	public static Quaternion mayaQ(float xRot, float yRot, float zRot, Order order)
	{
		Quaternion xQ = new Quaternion(new Vector3(1.0f, 0.0f, 0.0f), xRot * DEG_TO_RAD);
		Quaternion yQ = new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), yRot * DEG_TO_RAD );
		Quaternion zQ = new Quaternion(new Vector3(0.0f, 0.0f, 1.0f), zRot * DEG_TO_RAD );
		Quaternion ret = null;
		switch (order)
		{
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
		}
		return ret;
	}

	public static Quaternion mayaQ(float[] arr, int pos, Order order)
	{
		return mayaQ(arr[pos], arr[pos + 1], arr[pos + 2], order);
	}

	public static String OrderToString(Order order)
	{
		String p;
		switch (order)
		{
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

	public static Order StringToOrder(String str)
	{
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

	public static Order StringToOrderRev(String str)
	{
		return Order.values()[5 - StringToOrder(str).ordinal()];
	}

	/** A quaternion with a value of 0,0,0,1 */
	public final static Quaternion Identity = new Quaternion(0f, 0f, 0f, 1f);

}
