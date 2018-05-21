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
package libomv.StructuredData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;

import libomv.types.Color4;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;
import libomv.types.Quaternion;
import libomv.utils.Helpers;

public class OSD implements Cloneable {
	protected static final String FRACT_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
	protected static final String WHOLE_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public enum OSDType {
		Unknown, Boolean, Integer, Real, String, UUID, Date, URI, Binary, Map, Array
	}

	public enum OSDFormat {
		Xml, Json, Notation, Binary;

		public static String contentType(OSDFormat format) {
			switch (format) {
			case Xml:
				return "application/llsd+xml";
			case Binary:
				return "application/llsd+binary";
			case Notation:
				return "application/llsd+notation";
			default:
				break;
			}
			return "application/llsd+json";
		}

		public static String contentEncodingDefault(OSDFormat format) {
			return Helpers.UTF8_ENCODING;
		}
	}

	public OSD() {
		super();
	}

	/** The OSD class implementation */
	public OSDType getType() {
		return OSDType.Unknown;
	}

	public boolean asBoolean() {
		return false;
	}

	public int asInteger() {
		return 0;
	}

	public int asUInteger() {
		return 0;
	}

	public long asLong() {
		return 0;
	}

	public long asULong() {
		return 0;
	}

	public double asReal() {
		return 0d;
	}

	public String asString() {
		return "";
	}

	public UUID asUUID() {
		return UUID.Zero;
	}

	public Date asDate() {
		return Helpers.Epoch;
	}

	public URI asUri() {
		return null;
	}

	public byte[] asBinary() {
		return Helpers.EmptyBytes;
	}

	public InetAddress asInetAddress() {
		try {
			return InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public Vector2 asVector2() {
		return Vector2.Zero;
	}

	public Vector3 asVector3() {
		return Vector3.Zero;
	}

	public Vector3d asVector3d() {
		return Vector3d.Zero;
	}

	public Vector4 asVector4() {
		return Vector4.Zero;
	}

	public Quaternion asQuaternion() {
		return Quaternion.Identity;
	}

	public Color4 asColor4() {
		return Color4.Black;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof OSD && equals((OSD) obj);
	}

	public boolean equals(OSD osd) {
		return osd != null && osd.getType() == OSDType.Unknown;
	}

	public OSD clone() {
		OSD osd = null;
		try {
			osd = (OSD) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return osd;
	}

	@Override
	public String toString() {
		return "undef";
	}

	public static OSD fromBoolean(boolean value) {
		return new OSDBoolean(value);
	}

	public static OSD fromInteger(short value) {
		return new OSDInteger(value);
	}

	public static OSD fromInteger(int value) {
		return new OSDInteger(value);
	}

	public static OSD fromUInteger(int value) {
		return new OSDBinary(value & 0xffffffff);
	}

	public static OSD fromLong(long value) {
		return new OSDBinary(value);
	}

	public static OSD fromULong(long value) {
		return new OSDBinary(value & 0xffffffffffffffffl);
	}

	public static OSD fromReal(double value) {
		return new OSDReal(value);
	}

	public static OSD fromReal(float value) {
		return new OSDReal(value);
	}

	public static OSD fromString(String value) {
		return new OSDString(value);
	}

	public static OSD fromString(InetAddress value) {
		return new OSDString(value.getHostAddress());
	}

	public static OSD fromUUID(String value) {
		if (value.length() >= 36) {
			UUID uuid = UUID.parse(value);
			if (uuid != null)
				return OSD.fromUUID(uuid);
		}
		return new OSDString(value);
	}

	public static OSD fromUUID(UUID value) {
		return new OSDUUID(value);
	}

	public static OSD fromDate(Date value) {
		return new OSDDate(value);
	}

	public static OSD fromUri(URI value) {
		return new OSDUri(value);
	}

	public static OSD fromBinary(byte[] value) {
		return new OSDBinary(value);
	}

	public static OSD fromBinary(long value) {
		return new OSDBinary(value);
	}

	public static OSD fromBinary(InetAddress value) {
		return new OSDBinary(value.getAddress());
	}

	public static OSD fromVector2(Vector2 value) {
		OSDArray array = new OSDArray();
		array.add(OSD.fromReal(value.X));
		array.add(OSD.fromReal(value.Y));
		return array;
	}

	public static OSD fromVector3(Vector3 value) {
		OSDArray array = new OSDArray();
		array.add(OSD.fromReal(value.X));
		array.add(OSD.fromReal(value.Y));
		array.add(OSD.fromReal(value.Z));
		return array;
	}

	public static OSD fromVector3d(Vector3d value) {
		OSDArray array = new OSDArray();
		array.add(OSD.fromReal(value.X));
		array.add(OSD.fromReal(value.Y));
		array.add(OSD.fromReal(value.Z));
		return array;
	}

	public static OSD fromVector4(Vector4 value) {
		OSDArray array = new OSDArray();
		array.add(OSD.fromReal(value.X));
		array.add(OSD.fromReal(value.Y));
		array.add(OSD.fromReal(value.Z));
		array.add(OSD.fromReal(value.S));
		return array;
	}

	public static OSD fromQuaternion(Quaternion value) {
		OSDArray array = new OSDArray();
		array.add(OSD.fromReal(value.X));
		array.add(OSD.fromReal(value.Y));
		array.add(OSD.fromReal(value.Z));
		array.add(OSD.fromReal(value.W));
		return array;
	}

	public static OSD fromColor4(Color4 value) {
		OSDArray array = new OSDArray();
		array.add(OSD.fromReal(value.R));
		array.add(OSD.fromReal(value.G));
		array.add(OSD.fromReal(value.B));
		array.add(OSD.fromReal(value.A));
		return array;
	}

	public static OSD fromObject(Object value) {
		if (value == null) {
			return new OSD();
		}
		if (value instanceof OSD) {
			return (OSD) value;
		}
		if (value instanceof Boolean) {
			return new OSDBoolean((Boolean) value);
		}
		if (value instanceof Integer) {
			return new OSDInteger((Integer) value);
		}
		if (value instanceof Short) {
			return new OSDInteger(((Short) value).intValue());
		}
		if (value instanceof Byte) {
			return new OSDInteger(((Byte) value).intValue());
		}
		if (value instanceof Double) {
			return new OSDReal(((Double) value).doubleValue());
		}
		if (value instanceof Float) {
			return new OSDReal(((Float) value).doubleValue());
		}
		if (value instanceof String) {
			return new OSDString((String) value);
		}
		if (value instanceof UUID) {
			return new OSDUUID((UUID) value);
		}
		if (value instanceof Date) {
			return new OSDDate((Date) value);
		}
		if (value instanceof URI) {
			return new OSDUri((URI) value);
		}
		if (value instanceof byte[]) {
			return new OSDBinary((byte[]) value);
		}
		if (value instanceof Long) {
			return new OSDBinary((Long) value);
		}
		if (value instanceof Vector2) {
			return fromVector2((Vector2) value);
		}
		if (value instanceof Vector3) {
			return fromVector3((Vector3) value);
		}
		if (value instanceof Vector3d) {
			return fromVector3d((Vector3d) value);
		}
		if (value instanceof Vector4) {
			return fromVector4((Vector4) value);
		}
		if (value instanceof Quaternion) {
			return fromQuaternion((Quaternion) value);
		}
		if (value instanceof Color4) {
			return fromColor4((Color4) value);
		}
		// We don't know this type
		return new OSD();
	}

	protected static Object toObject(Class<?> type, OSD value) {
		if (type == null || value == null) {
			return null;
		} else if (type.isAssignableFrom(Long.class) || type == long.class) {
			return value.asLong();
		} else if (type.isAssignableFrom(Integer.class) || type == int.class) {
			return value.asInteger();
		} else if (type.isAssignableFrom(Short.class) || type == short.class) {
			return (short) value.asInteger();
		} else if (type.isAssignableFrom(Byte.class) || type == byte.class) {
			return (byte) value.asInteger();
		} else if (type.isAssignableFrom(Boolean.class) || type == boolean.class) {
			return value.asBoolean();
		} else if (type.isAssignableFrom(Double.class) || type == double.class) {
			return value.asReal();
		} else if (type.isAssignableFrom(Float.class) || type == float.class) {
			return (float) value.asReal();
		} else if (type.isAssignableFrom(String.class)) {
			return value.asString();
		} else if (type.isAssignableFrom(Date.class)) {
			return value.asDate();
		} else if (type.isAssignableFrom(URI.class)) {
			return value.asUri();
		} else if (type.isAssignableFrom(UUID.class)) {
			return value.asUUID();
		} else if (type.isAssignableFrom(Vector3.class)) {
			if (value.getType() == OSDType.Array) {
				return ((OSDArray) value).asVector3();
			}
			return Vector3.Zero;
		} else if (type.isAssignableFrom(Vector4.class)) {
			if (value.getType() == OSDType.Array) {
				return ((OSDArray) value).asVector4();
			}
			return Vector4.Zero;
		} else if (type.isAssignableFrom(Quaternion.class)) {
			if (value.getType() == OSDType.Array) {
				return ((OSDArray) value).asQuaternion();
			}
			return Quaternion.Identity;
		} else if (type.isAssignableFrom(OSDArray.class)) {
			OSDArray newArray = new OSDArray();
			for (OSD o : (OSDArray) value)
				newArray.add(o);
			return newArray;
		} else if (type.isAssignableFrom(OSDMap.class)) {
			OSDMap old = (OSDMap) value, newMap = new OSDMap();
			for (String key : ((OSDMap) value).keySet())
				newMap.put(key, old.get(key));
			return newMap;
		}
		// We don't know this type
		return null;
	}

	/**
	 * Uses reflection to create an OSDMap from all of the OSD serializable types in
	 * an object
	 *
	 * @param obj
	 *            Class or struct containing serializable types
	 * @return An SDMap holding the serialized values from the container object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static OSDMap serializeMembers(Object obj) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = obj.getClass().getFields();
		OSDMap map = new OSDMap(fields.length);
		for (Field field : fields) {
			if (!Modifier.isTransient(field.getModifiers())) {
				OSD serializedField = OSD.fromObject(field.get(obj));

				if (serializedField.getType() != OSDType.Unknown) {
					map.put(field.getName(), serializedField);
				}
			}
		}
		return map;
	}

	/**
	 * Uses reflection to deserialize member variables in an object from an OSDMap
	 *
	 * @param obj
	 *            Reference to an object to fill with deserialized values
	 * @param serialized
	 *            Serialized values to put in the target object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object deserializeMembers(Object obj, OSDMap serialized)
			throws IllegalArgumentException, IllegalAccessException {
		for (Field field : obj.getClass().getFields()) {
			if (!Modifier.isTransient(field.getModifiers())) {
				OSD serializedField = serialized.get(field.getName());
				if (serializedField != null) {
					field.set(obj, toObject(field.getClass(), serializedField));
				}
			}
		}
		return obj;
	}
}