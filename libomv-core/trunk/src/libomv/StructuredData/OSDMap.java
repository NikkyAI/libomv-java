/**
 * Copyright (c) 2008, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;

public class OSDMap extends OSD implements Map<String, OSD>
{
	private HashMap<String, OSD> value;

	@Override
	// OSD
	public OSDType getType()
	{
		return OSDType.Map;
	}

	public OSDMap()
	{
		value = new HashMap<String, OSD>();
	}

	public OSDMap(int capacity)
	{
		value = new HashMap<String, OSD>(capacity);
	}

	public OSDMap(HashMap<String, OSD> value)
	{
		if (value != null)
		{
			this.value = value;
		}
		else
		{
			this.value = new HashMap<String, OSD>();
		}
	}

	@Override
	// Map
	public final int size()
	{
		return value.size();
	}

	@Override
	public boolean isEmpty()
	{
		return value.size() == 0;
	}

	@Override
	public boolean AsBoolean()
	{
		return !isEmpty();
	}

	@Override
	public Vector2 AsVector2()
	{
		Vector2 vec = new Vector2(Vector2.Zero);
		if (containsKey("X") && containsKey("Y"))
		{
			vec.X = (float)get("X").AsReal();
			vec.Y = (float)get("Y").AsReal();
		}
		return vec;
	}

	@Override
	public Vector3 AsVector3()
	{
		Vector3 vec = new Vector3(Vector3.Zero);
		if (containsKey("X") && containsKey("Y") && containsKey("Z"))
		{
			vec.X = (float)get("X").AsReal();
			vec.Y = (float)get("Y").AsReal();
			vec.Z = (float)get("Z").AsReal();
		}
		return vec;
	}

	@Override
	public Vector3d AsVector3d()
	{
		Vector3d vec = new Vector3d(Vector3d.Zero);
		if (containsKey("X") && containsKey("Y") && containsKey("Z"))
		{
			vec.X = get("X").AsReal();
			vec.Y = get("Y").AsReal();
			vec.Z = get("Z").AsReal();
		}
		return vec;
	}

	@Override
	public Vector4 AsVector4()
	{
		Vector4 vector = new Vector4(Vector4.Zero);

		if (this.size() == 4)
		{
			vector.X = (float) this.get("X").AsReal();
			vector.Y = (float) this.get("Y").AsReal();
			vector.Z = (float) this.get("Z").AsReal();
			vector.S = (float) this.get("S").AsReal();
		}
		return vector;
	}

	@Override
	public Quaternion AsQuaternion()
	{
		Quaternion quaternion = new Quaternion(Quaternion.Identity);

		if (this.size() == 4)
		{
			quaternion.X = (float) this.get("X").AsReal();
			quaternion.Y = (float) this.get("Y").AsReal();
			quaternion.Z = (float) this.get("Z").AsReal();
			quaternion.W = (float) this.get("W").AsReal();
		}
		return quaternion;
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj != null && obj instanceof OSD && equals((OSD)obj);
	}

	public boolean equals(OSD osd)
	{
		return osd != null && osd.getType() == OSDType.Map && ((OSDMap)osd).value.equals(value);
	}

	@Override
	public String toString()
	{
		try
		{
			return OSDParser.serializeToString(this, OSDFormat.Notation);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public final boolean getIsReadOnly()
	{
		return false;
	}

	@Override
	public final Set<String> keySet()
	{
		return value.keySet();
	}

	@Override
	public final Collection<OSD> values()
	{
		return value.values();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return value.containsKey(key);
	}

	@Override
	public final boolean containsValue(Object osd)
	{
		return value.containsValue(osd);
	}

	@Override
	public final OSD get(Object key)
	{
		OSD osd = value.get(key);
		return osd == null ? new OSD() : osd;
	}

	@Override
	public final OSD put(String key, OSD val)
	{
		return value.put(key, val);
	}

	public final OSD put(Entry<String, OSD> kvp)
	{
		return value.put(kvp.getKey(), kvp.getValue());
	}

	@Override
	public final OSD remove(Object key)
	{
		return value.remove(key);
	}

	@Override
	public final void clear()
	{
		value.clear();
	}

	@Override
	public Set<Entry<String, OSD>> entrySet()
	{
		return value.entrySet();
	}

	@Override
	public void putAll(Map<? extends String, ? extends OSD> m)
	{
		value.putAll(m);
	}

	/**
	 * Uses reflection to deserialize member variables in an object from this
	 * OSDMap
	 * 
	 * @param obj
	 *            Reference to an object to fill with deserialized values
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public Object deserializeMembers(Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		Field[] fields = obj.getClass().getFields();

		for (Field field : fields)
		{
			if (!Modifier.isTransient(field.getModifiers()))
			{
				OSD serializedField = get(field.getName());
				if (serializedField != null)
				{
					field.set(obj, ToObject(field.getType(), serializedField));
				}
			}
		}
		return obj;
	}
}
