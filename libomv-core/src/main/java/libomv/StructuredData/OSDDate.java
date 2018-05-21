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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import libomv.utils.Helpers;

public class OSDDate extends OSD {
	private long value;

	public OSDDate(Date value) {
		this.value = value.getTime();
	}

	@Override
	public OSDType getType() {
		return OSDType.Date;
	}

	@Override
	public String asString() {
		SimpleDateFormat df = new SimpleDateFormat(FRACT_DATE_FMT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(new Date(value));
	}

	@Override
	public int asInteger() {
		return (int) value / 1000;
	}

	@Override
	public int asUInteger() {
		return (int) (value / 1000) & 0xffffffff;
	}

	@Override
	public long asLong() {
		return (value / 1000);
	}

	@Override
	public long asULong() {
		return (value / 1000) & 0xffffffffffffffffl;
	}

	@Override
	public byte[] asBinary() {
		return Helpers.doubleToBytesL(value / 1000.0);
	}

	@Override
	public Date asDate() {
		return new Date(value);
	}

	@Override
	public int hashCode() {
		return (int) value | (int) (value >> 32);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof OSD && equals((OSD) obj);
	}

	public boolean equals(OSD osd) {
		return osd != null && osd.asLong() == value;
	}

	@Override
	public String toString() {
		return asString();
	}
}
