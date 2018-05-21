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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import libomv.types.Color4;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;
import libomv.utils.Helpers;

public class OSDString extends OSD {
	private String value;

	public OSDString(String value) {
		// Refuse to hold null pointers
		if (value != null)
			this.value = value;
		else
			this.value = Helpers.EmptyString;
	}

	@Override
	public OSDType getType() {
		return OSDType.String;
	}

	@Override
	public boolean asBoolean() {
		if (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))
			return false;

		try {
			if (Double.parseDouble(value) == 0.0)
				return false;
		} catch (NumberFormatException ex) {
		}

		return true;
	}

	@Override
	public int asInteger() {
		try {
			return (int) Math.floor(Double.parseDouble(value));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	@Override
	public int asUInteger() {
		try {
			return ((int) Math.floor(Double.parseDouble(value)) & 0xffffffff);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	@Override
	public long asLong() {
		try {
			return (long) Math.floor(Double.parseDouble(value));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	@Override
	public long asULong() {
		try {
			return ((long) Math.floor(Double.parseDouble(value)) & 0xffffffffffffffffl);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	@Override
	public double asReal() {
		return Helpers.TryParseDouble(value);
	}

	@Override
	public Vector2 asVector2() {
		try {
			return OSDParser.deserialize(this.value, OSDFormat.Notation).asVector2();
		} catch (Exception ex) {
			return Vector2.Zero;
		}
	}

	@Override
	public Vector3 asVector3() {
		try {
			return OSDParser.deserialize(this.value, OSDFormat.Notation).asVector3();
		} catch (Exception ex) {
			return Vector3.Zero;
		}
	}

	@Override
	public Vector3d asVector3d() {
		try {
			return OSDParser.deserialize(this.value, OSDFormat.Notation).asVector3d();
		} catch (Exception ex) {
			return Vector3d.Zero;
		}
	}

	@Override
	public Vector4 asVector4() {
		try {
			return OSDParser.deserialize(this.value, OSDFormat.Notation).asVector4();
		} catch (Exception ex) {
			return Vector4.Zero;
		}
	}

	@Override
	public Quaternion asQuaternion() {
		try {
			return OSDParser.deserialize(this.value, OSDFormat.Notation).asQuaternion();
		} catch (Exception ex) {
			return Quaternion.Identity;
		}
	}

	@Override
	public Color4 asColor4() {
		try {
			return OSDParser.deserialize(this.value, OSDFormat.Notation).asColor4();
		} catch (Exception ex) {
			return Color4.Black;
		}
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public byte[] asBinary() {
		return Helpers.stringToBytes(value);
	}

	@Override
	public InetAddress asInetAddress() {
		try {
			int i = value.indexOf(':');
			if (i < 0)
				return InetAddress.getByName(value);
			return InetAddress.getByName(value.substring(0, i));
		} catch (UnknownHostException ex) {
			return null;
		}
	}

	@Override
	public UUID asUUID() {
		return new UUID(value);
	}

	@Override
	public Date asDate() {
		SimpleDateFormat df = new SimpleDateFormat(FRACT_DATE_FMT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return df.parse(value);
		} catch (ParseException ex1) {
			try {
				df.applyPattern(WHOLE_DATE_FMT);
				return df.parse(value);
			} catch (ParseException ex2) {
				return Helpers.Epoch;
			}
		}
	}

	@Override
	public URI asUri() {
		try {
			return new URI(value);
		} catch (URISyntaxException ex) {
			return null;
		}
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof OSD && equals((OSD) obj);
	}

	public boolean equals(OSD osd) {
		return osd != null && osd.asString().equals(value);
	}

	@Override
	public String toString() {
		return asString();
	}
}
