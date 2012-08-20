/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Portions Copyright (c) 2009-2012, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package libomv.StructuredData.LLSD;

/*
 * This implementation is based upon the description at
 * http://wiki.secondlife.com/wiki/LLSD
 * and (partially) tested against the (supposed) reference implementation at
 * http://svn.secondlife.com/svn/linden/release/indra/lib/python/indra/base/osd.py
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map.Entry;

import org.apache.commons.io.input.ReaderInputStream;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.PushbackInputStream;

public final class LLSDBinary extends OSDParser
{
	private static final int int32Length = 4;
	private static final int doubleLength = 8;

	private static final byte[] llsdBinaryHeader = { 'l','l','s','d','/','b','i','n','a','r','y'};
	private static final byte[] llsdBinaryHead = { '<','?','l','l','s','d','/','b','i','n','a','r','y','?','>'};

	private static final byte undefBinaryValue = (byte) '!';
	private static final byte trueBinaryValue = (byte) '1';
	private static final byte falseBinaryValue = (byte) '0';
	private static final byte integerBinaryMarker = (byte) 'i';
	private static final byte realBinaryMarker = (byte) 'r';
	private static final byte uuidBinaryMarker = (byte) 'u';
	private static final byte binaryBinaryMarker = (byte) 'b';
	private static final byte stringBinaryMarker = (byte) 's';
	private static final byte uriBinaryMarker = (byte) 'l';
	private static final byte dateBinaryMarker = (byte) 'd';
	private static final byte arrayBeginBinaryMarker = (byte) '[';
	private static final byte arrayEndBinaryMarker = (byte) ']';
	private static final byte mapBeginBinaryMarker = (byte) '{';
	private static final byte mapEndBinaryMarker = (byte) '}';
	private static final byte keyBinaryMarker = (byte) 'k';
	private static final byte doubleQuotesNotationMarker = '"';
	private static final byte singleQuotesNotationMarker = '\'';


	public static boolean isFormat(InputStream stream) throws IOException
	{
		int character = skipWhiteSpace(stream);
		if (character == '<')
		{
			return header(stream, llsdBinaryHeader, '>');
		}
		return false;
	}
	
	public static boolean isFormat(Reader reader) throws IOException
	{
		return isFormat(reader, null);
	}

	public static boolean isFormat(Reader reader, String encoding) throws IOException
	{
		int character = skipWhiteSpace(reader);
		if (character == '<')
		{
			return header(reader, new String(llsdBinaryHeader, encoding != null ? encoding : Helpers.ASCII_ENCODING), '>');
		}
		return false;
	}

	public static boolean isFormat(String string) throws UnsupportedEncodingException
	{
		return string.substring(string.indexOf('<'), string.indexOf('<')).contains(new String(llsdBinaryHeader, Helpers.ASCII_ENCODING));
	}
	
	public static boolean isFormat(String string, String encoding) throws UnsupportedEncodingException
	{
		return string.substring(string.indexOf('<'), string.indexOf('<')).contains(new String(llsdBinaryHeader, encoding));
	}
	
	public static boolean isFormat(byte[] data)
	{
		int character = skipWhiteSpace(data);
		if (character == '<')
		{
			return header(data, llsdBinaryHeader, '>');
		}
		return false;
	}
	
	/**
	 * Creates an OSD (object structured data) object from a LLSD binary data stream
	 * 
	 * @param stream The byte stream to read from
	 * @return and OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(Reader reader, String encoding) throws IOException, ParseException
	{
		return parse(new ReaderInputStream(reader, encoding != null ? encoding : Helpers.ASCII_ENCODING));
	}
	
	/**
	 * Creates an OSD (object structured data) object from a LLSD binary data stream
	 * 
	 * @param stream The byte stream to read from
	 * @return and OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(InputStream stream) throws IOException, ParseException
	{
		PushbackInputStream push = stream instanceof PushbackInputStream ? (PushbackInputStream)stream : new PushbackInputStream(stream);
		int marker = skipWhiteSpace(push);
		if (marker < 0)
		{
			return new OSD();
		}
		else if (marker == '<')
		{
			int offset = push.getBytePosition();
			if (!header(push, llsdBinaryHeader, '>'))
				throw new ParseException("Failed to decode binary LLSD", offset);	
		}		
		return parseElement(push);
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD binary stream
	 * 
	 * @param stream The binary byte stream to write the OSD object into
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	public static void serialize(OutputStream stream, OSD data, boolean prependHeader) throws IOException
	{
		if (prependHeader)
		{
			stream.write(llsdBinaryHead);
			stream.write('\n');
		}
		serializeElement(stream, data);
	}

	private static void serializeElement(OutputStream stream, OSD osd) throws IOException
	{
		byte[] rawBinary;

		switch (osd.getType())
		{
			case Unknown:
				stream.write(undefBinaryValue);
				break;
			case Boolean:
				stream.write(osd.AsBinary(), 0, 1);
				break;
			case Integer:
				stream.write(integerBinaryMarker);
				stream.write(osd.AsBinary(), 0, int32Length);
				break;
			case Real:
				stream.write(realBinaryMarker);
				stream.write(osd.AsBinary(), 0, doubleLength);
				break;
			case UUID:
				stream.write(uuidBinaryMarker);
				stream.write(osd.AsBinary(), 0, 16);
				break;
			case String:
				rawBinary = osd.AsBinary();
				stream.write(stringBinaryMarker);
				stream.write(Helpers.Int32ToBytesB(rawBinary.length));
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case Binary:
				rawBinary = osd.AsBinary();
				stream.write(binaryBinaryMarker);
				stream.write(Helpers.Int32ToBytesB(rawBinary.length));
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case Date:
				stream.write(dateBinaryMarker);
				stream.write(osd.AsBinary(), 0, doubleLength);
				break;
			case URI:
				rawBinary = osd.AsBinary();
				stream.write(uriBinaryMarker);
				stream.write(Helpers.Int32ToBytesB(rawBinary.length));
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case Array:
				serializeArray(stream, (OSDArray) osd);
				break;
			case Map:
				serializeMap(stream, (OSDMap) osd);
				break;
			default:
				throw new IOException("Binary serialization: Not existing element discovered.");
		}
	}

	private static void serializeArray(OutputStream stream, OSDArray osdArray) throws IOException
	{
		stream.write(arrayBeginBinaryMarker);
		stream.write(Helpers.Int32ToBytesB(osdArray.size()));

		for (OSD osd : osdArray)
		{
			serializeElement(stream, osd);
		}
		stream.write(arrayEndBinaryMarker);
	}

	private static void serializeMap(OutputStream stream, OSDMap osdMap) throws IOException
	{
		stream.write(mapBeginBinaryMarker);
		stream.write(Helpers.Int32ToBytesB(osdMap.size()));

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			stream.write(keyBinaryMarker);
			stream.write(Helpers.Int32ToBytesB(kvp.getKey().length()));
			stream.write(kvp.getKey().getBytes(Helpers.UTF8_ENCODING), 0, kvp.getKey().length());
			serializeElement(stream, kvp.getValue());
		}
		stream.write(mapEndBinaryMarker);
	}

	private static OSD parseElement(PushbackInputStream stream) throws IOException, ParseException
	{
		int marker = skipWhiteSpace(stream);
		if (marker < 0)
		{
			throw new ParseException("Binary LLSD parsing: Unexpected end of stream.", 1);
		}

		OSD osd;
		switch ((byte) marker)
		{
			case undefBinaryValue:
				osd = new OSD();
				break;
			case trueBinaryValue:
				osd = OSD.FromBoolean(true);
				break;
			case falseBinaryValue:
				osd = OSD.FromBoolean(false);
				break;
			case integerBinaryMarker:
				int integer = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				osd = OSD.FromInteger(integer);
				break;
			case realBinaryMarker:
				double dbl = Helpers.BytesToDoubleB(consumeBytes(stream, doubleLength), 0);
				osd = OSD.FromReal(dbl);
				break;
			case uuidBinaryMarker:
				osd = OSD.FromUUID(new UUID(consumeBytes(stream, 16)));
				break;
			case binaryBinaryMarker:
				int binaryLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				osd = OSD.FromBinary(consumeBytes(stream, binaryLength));
				break;
			case doubleQuotesNotationMarker:
			case singleQuotesNotationMarker:
				throw new ParseException("Binary LLSD parsing: LLSD Notation Format strings are not yet supported",	stream.getBytePosition());
			case stringBinaryMarker:
				int stringLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				osd = OSD.FromString(new String(consumeBytes(stream, stringLength), Helpers.UTF8_ENCODING));
				break;
			case uriBinaryMarker:
				int uriLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				URI uri;
				try
				{
					uri = new URI(new String(consumeBytes(stream, uriLength), Helpers.UTF8_ENCODING));
				}
				catch (URISyntaxException ex)
				{
					throw new ParseException("Binary LLSD parsing: Invalid Uri format detected: " + ex.getMessage(),
							stream.getBytePosition());
				}
				osd = OSD.FromUri(uri);
				break;
			case dateBinaryMarker:
				/* LLSD Wiki says that the double is also in network byte order, like the real numbers but Openmetaverse as well as the
				 * LLSDBinaryParser::doParse in llsdserialize.cpp clearly do not do any byteswapping. 
				 */
				double timestamp = Helpers.BytesToDoubleL(consumeBytes(stream, doubleLength), 0);
				osd = OSD.FromDate(Helpers.UnixTimeToDateTime(timestamp));
				break;
			case arrayBeginBinaryMarker:
				osd = parseArray(stream);
				break;
			case mapBeginBinaryMarker:
				osd = parseMap(stream);
				break;
			default:
				throw new ParseException("Binary LLSD parsing: Unknown type marker.", stream.getBytePosition());
		}
		return osd;
	}

	private static OSD parseArray(PushbackInputStream stream) throws IOException, ParseException
	{
		int numElements = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
		int crrElement = 0;
		OSDArray osdArray = new OSDArray();
		while (crrElement < numElements)
		{
			osdArray.add(parseElement(stream));
			crrElement++;
		}
		if (!find(stream, arrayEndBinaryMarker))
		{
			throw new ParseException("Binary LLSD parsing: Missing end marker in array.", stream.getBytePosition());
		}

		return osdArray;
	}

	private static OSD parseMap(PushbackInputStream stream) throws IOException, ParseException
	{
		int numElements = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
		int crrElement = 0;
		OSDMap osdMap = new OSDMap();
		while (crrElement < numElements)
		{
			if (!find(stream, keyBinaryMarker))
			{
				throw new ParseException("Binary LLSD parsing: Missing key marker in map.", stream.getBytePosition());
			}
			int keyLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
			String key = new String(consumeBytes(stream, keyLength));
			osdMap.put(key, parseElement(stream));
			crrElement++;
		}
		if (!find(stream, mapEndBinaryMarker))
		{
			throw new ParseException("Binary LLSD parsing: Missing end marker in map.", stream.getBytePosition());
		}
		return osdMap;
	}
}