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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import libomv.StructuredData.OSD.OSDType;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class OSDBinary extends OSD
{
	private byte[] value;

	@Override
	public OSDType getType()
	{
		return OSDType.Binary;
	}

	public OSDBinary(byte[] value)
	{
		if (value != null)
		{
			this.value = value;
		}
		else
		{
			this.value = Helpers.EmptyBytes;
		}
	}

	public OSDBinary(int value)
	{
		this.value = Helpers.Int32ToBytesB(value);
	}

	public OSDBinary(long value)
	{
		this.value = Helpers.Int64ToBytesB(value);
	}

	@Override
	public String AsString()
	{
		try {
			return Helpers.BytesToString(value);
		}
		catch (UnsupportedEncodingException e) { }
		return null;
	}

	@Override
	public byte[] AsBinary()
	{
		return value;
	}

	@Override
	public InetAddress AsInetAddress()
	{
		try
		{
			return InetAddress.getByAddress(value);
		}
		catch (UnknownHostException e)
		{
			return null;
		}
	}

	@Override
	public UUID AsUUID()
	{
		return new UUID(value);
	}

	@Override
	public int AsUInteger()
	{
		return (int) Helpers.BytesToUInt32B(value);
	}

	@Override
	public long AsLong()
	{
		return Helpers.BytesToInt64B(value);
	}

	@Override
	public long AsULong()
	{
		return Helpers.BytesToUInt64B(value);
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
		return osd != null && osd.getType() == OSDType.Binary && ((OSDBinary)osd).value.equals(value);
	}

	@Override
	public String toString()
	{
		return Helpers.BytesToHexString(value, null);
	}
}
