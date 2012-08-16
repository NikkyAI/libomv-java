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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.PushbackReader;

public final class LLSDJson
{
	private static final String baseIndent = "  ";

	private static final char[] newLine = { '\n' };

	private static final char[] nullNotationValue = { 'n', 'u', 'l', 'l' };
	private static final char[] trueNotationValue = { 't', 'r', 'u', 'e' };
	private static final char[] falseNotationValue = { 'f', 'a', 'l', 's', 'e' };

	private static final char arrayBeginNotationMarker = '[';
	private static final char arrayEndNotationMarker = ']';

	private static final char mapBeginNotationMarker = '{';
	private static final char mapEndNotationMarker = '}';
	private static final char kommaNotationDelimiter = ',';
	private static final char keyNotationDelimiter = ':';

	private static final char doubleQuotesNotationMarker = '"';

	/**
	 * Parse an JSON string and convert it into an hierarchical OSD object
	 * 
	 * @param string The JSON string to parse
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(String string) throws ParseException, IOException
	{
		StringReader reader = new StringReader(string);
		try
		{
			return parseElement(new PushbackReader(reader));
		}
		finally
		{
			reader.close();
		}
	}

	/**
	 * Parse an JSON byte stream and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param stream The JSON byte stream to parse
	 * @param encoding The text encoding to use when converting the stream to text
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(byte[] data, String encoding) throws ParseException, IOException
	{
		PushbackReader reader = new PushbackReader(new InputStreamReader(new ByteArrayInputStream(data), encoding));
		try
		{
			return parseElement(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/**
	 * Parse an JSON reader and convert it into an hierarchical OSD object
	 * 
	 * @param reader The JSON reader to parse
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(Reader reader) throws ParseException, IOException
	{
		return parseElement(reader instanceof PushbackReader ? (PushbackReader)reader : new PushbackReader(reader));
	}
	
	/**
	 * Parse an JSON byte stream and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param stream The JSON byte stream to parse
	 * @param encoding The text encoding to use when converting the stream to text
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(InputStream stream, String encoding) throws ParseException, IOException
	{
		return parseElement(new PushbackReader(new InputStreamReader(stream, encoding)));
	}

	/**
	 * Serialize an hierarchical OSD object into an JSON string
	 * 
	 * @param stream The hierarchical JSON byte stream
	 * @param data The hierarchical OSD object to serialize
	 * @param encoding The text encoding to use when converting the text into
	 *            a byte stream
	 * @throws IOException
	 */
	public static void serialize(OutputStream stream, OSD data, String encoding) throws IOException
	{
		serializeElement(new OutputStreamWriter(stream, encoding), data);
	}

	/**
	 * Serialize an hierarchical OSD object into an JSON writer
	 * 
	 * @param writer The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @throws IOException
	 */
	public static void serialize(Writer writer, OSD data) throws IOException
	{
		serializeElement(writer, data);
	}
	
	/**
	 * Serialize an hierarchical OSD object into an JSON string
	 * 
	 * @param data The hierarchical OSD object to serialize
	 * @return an JSON formatted string
	 * @throws IOException
	 */
	public static String serializeToString(OSD data) throws IOException
	{
		Writer writer = new StringWriter();
		try
		{
			serializeElement(writer, data);
			return writer.toString();
		}
		finally
		{
			writer.close();
		}
	}

	public static void serializeFormatted(Writer writer, OSD data) throws IOException
	{
		serializeElementFormatted(writer, "", data);
	}

	/**
	 * Read the next LLSD data element in and return the OSD structure for it
	 * 
	 * @param reader a pushback reader to read in data from
	 * @return the OSD data corresponding to the LLSD data element
	 * @throws IOException
	 */
	private static OSD parseElement(PushbackReader reader) throws ParseException, IOException
	{
		int character = skipWhitespace(reader);
		if (character <= 0)
		{
			return new OSD(); // server returned an empty file, so we're going
								// to pass along a null LLSD object
		}

		switch ((char) character)
		{
			case 'n':
				if (BufferCharactersEqual(reader, nullNotationValue, 1) == nullNotationValue.length)
				{
					return new OSD();
				}
				break;
			case 'f':
				if (BufferCharactersEqual(reader, falseNotationValue, 1) == falseNotationValue.length)
				{
					return OSD.FromBoolean(false);
				}
				break;
			case 't':
				if (BufferCharactersEqual(reader, trueNotationValue, 1) == trueNotationValue.length)
				{
					return OSD.FromBoolean(true);
				}
				break;
			case '-':
			case '+':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return parseNumber(reader, character);
			case doubleQuotesNotationMarker:
				return parseString(reader);
			case arrayBeginNotationMarker:
				return parseArray(reader);
			case mapBeginNotationMarker:
				return parseMap(reader);
		}
		throw new ParseException("LLSD JSON parsing: Unexpected character '" + (char) character + "'.",
				reader.getBytePosition());
	}

	private static OSD parseNumber(PushbackReader reader, int character) throws IOException
	{
		StringBuilder s = new StringBuilder();
		if ((char) character == '-' || (char) character == '+')
		{
			s.append((char) character);
			character = reader.read();
		}
		boolean isReal = false;
		while ((character >= 0)
				&& (Character.isDigit((char) character) || (char) character == '.' || (char) character == 'e'
						|| (char) character == 'E' || (char) character == '+' || (char) character == '-'))
		{
			if ((char) character == '.')
				isReal = true;
			s.append((char) character);
			character = reader.read();
		}
		if (character >= 0)
		{
			reader.unread(character);
		}
		if (isReal)
			return OSD.FromReal(new Double(s.toString()));
		return OSD.FromInteger(new Integer(s.toString()));

	}

	private static OSD parseString(PushbackReader reader) throws IOException, ParseException
	{
		String string = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
		UUID uuid = new UUID();
		if (uuid.FromString(string))
			return OSD.FromUUID(uuid);
		OSD osd = OSD.FromString(string);
		Date date = osd.AsDate();
		if (!date.equals(Helpers.Epoch))
			return OSD.FromDate(date);
		URI uri = osd.AsUri();
		if (uri != null)
			return OSD.FromUri(uri);
		return osd;
	}
	
	private static OSD parseArray(PushbackReader reader) throws IOException, ParseException
	{
		int character = kommaNotationDelimiter;
		OSDArray osdArray = new OSDArray();
		while (((char) character == kommaNotationDelimiter) && ((character = skipWhitespace(reader)) > 0))
		{
			reader.unread(character);
			osdArray.add(parseElement(reader));
			character = skipWhitespace(reader);
		}
		if (character < 0)
		{
			throw new ParseException("LLSD JSON parsing: Unexpected end of array discovered.",
					reader.getBytePosition());
		}
		else if (character != arrayEndNotationMarker)
		{
			throw new ParseException("LLSD JSON parsing: Array end expected.",
					reader.getBytePosition());
		}
		return osdArray;
	}

	private static OSD parseMap(PushbackReader reader) throws ParseException, IOException
	{
		int character;
		OSDMap osdMap = new OSDMap();
		while (((character = skipWhitespace(reader)) > 0) && ((char) character != mapEndNotationMarker))
		{
			reader.unread(character);
			OSD osdKey = parseElement(reader);
			if (!osdKey.getType().equals(OSD.OSDType.String))
			{
				throw new ParseException("LLSD JSON parsing: Invalid key in map", reader.getBytePosition());
			}
			String key = osdKey.AsString();

			character = skipWhitespace(reader);
			if ((char) character != keyNotationDelimiter)
			{
				throw new ParseException("LLSD JSON parsing: Invalid key delimiter in map.",
						reader.getBytePosition());
			}
			osdMap.put(key, parseElement(reader));
		}
		if (character < 0)
		{
			throw new ParseException("Notation LLSD parsing: Unexpected end of map discovered.",
					reader.getBytePosition());
		}
		return osdMap;
	}

	private static void serializeElement(Writer writer, OSD osd) throws IOException
	{
		switch (osd.getType())
		{
			case Unknown:
				writer.write(nullNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValue);
				}
				else
				{
					writer.write(falseNotationValue);
				}
				break;
			case Integer:
			case Real:
				writer.write(osd.AsString());
				break;
			case String:
			case UUID:
			case Date:
			case URI:
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacters(osd.AsString()));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case Array:
				serializeArray(writer, (OSDArray) osd);
				break;
			case Map:
				serializeMap(writer, (OSDMap) osd);
				break;
			default:
				throw new IOException("Notation serialization: Not existing element discovered.");
		}
	}

	private static void serializeArray(Writer writer, OSDArray osdArray) throws IOException
	{
		writer.write(arrayBeginNotationMarker);
		int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			serializeElement(writer, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
		}
		writer.write(arrayEndNotationMarker);
	}

	private static void serializeMap(Writer writer, OSDMap osdMap) throws IOException
	{
		writer.write(mapBeginNotationMarker);
		int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(doubleQuotesNotationMarker);
			writer.write(escapeCharacters(kvp.getKey()));
			writer.write(doubleQuotesNotationMarker);
			writer.write(keyNotationDelimiter);
			serializeElement(writer, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
			idx++;
		}
		writer.write(mapEndNotationMarker);
	}

	private static void serializeElementFormatted(Writer writer, String indent, OSD osd) throws IOException
	{
		switch (osd.getType())
		{
			case Unknown:
				writer.write(nullNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValue);
				}
				else
				{
					writer.write(falseNotationValue);
				}
				break;
			case Integer:
			case Real:
				writer.write(osd.AsString());
				break;
			case String:
			case UUID:
			case Date:
			case URI:
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacters(osd.AsString()));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(doubleQuotesNotationMarker);
				writer.write(Base64.encodeBase64String(osd.AsBinary()));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Array:
				serializeArrayFormatted(writer, indent + baseIndent, (OSDArray) osd);
				break;
			case Map:
				serializeMapFormatted(writer, indent + baseIndent, (OSDMap) osd);
				break;
			default:
				throw new IOException("Notation serialization: Not existing element discovered.");

		}
	}

	private static void serializeArrayFormatted(Writer writer, String indent, OSDArray osdArray) throws IOException
	{
		writer.write(arrayBeginNotationMarker);
		int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			writer.write(newLine);
			writer.write(indent);
			serializeElementFormatted(writer, indent + baseIndent, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
		}
		writer.write(newLine);
		writer.write(indent);
		writer.write(arrayEndNotationMarker);
	}

	private static void serializeMapFormatted(Writer writer, String indent, OSDMap osdMap) throws IOException
	{
		writer.write(mapBeginNotationMarker);
		int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(newLine);
			writer.write(indent);
			writer.write(doubleQuotesNotationMarker);
			writer.write(escapeCharacters(kvp.getKey()));
			writer.write(doubleQuotesNotationMarker);
			writer.write(keyNotationDelimiter);
			serializeElementFormatted(writer, indent + baseIndent, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}

			idx++;
		}
		writer.write(newLine);
		writer.write(indent);
		writer.write(mapEndNotationMarker);
	}

	public static int skipWhitespace(PushbackReader reader) throws IOException
	{
		int character;
		while ((character = reader.read()) >= 0)
		{
			char c = (char) character;
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r')
			{
				break;
			}
		}
		return character;
	}

	public static String GetStringDelimitedBy(PushbackReader reader, char delimiter) throws IOException, ParseException
	{
		int character;
		boolean foundEscape = false;
		StringBuilder s = new StringBuilder();
		while (((character = reader.read()) >= 0)
				&& (((char) character != delimiter) || ((char) character == delimiter && foundEscape)))
		{
			if (foundEscape)
			{
				foundEscape = false;
				switch ((char) character)
				{
					case 'a':
						s.append('\005');
						break;
					case 'b':
						s.append('\b');
						break;
					case 'f':
						s.append('\f');
						break;
					case 'n':
						s.append('\n');
						break;
					case 'r':
						s.append('\r');
						break;
					case 't':
						s.append('\t');
						break;
					case 'v':
						s.append('\013');
						break;
					default:
						s.append((char) character);
						break;
				}
			}
			else if ((char) character == '\\')
			{
				foundEscape = true;
			}
			else
			{
				s.append((char) character);
			}
		}
		if (character < 0)
		{
			throw new ParseException(
					"Notation LLSD parsing: Can't parse text because unexpected end of stream while expecting a '"
							+ delimiter + "' character.", reader.getBytePosition());
		}
		return s.toString();
	}

	public static int BufferCharactersEqual(PushbackReader reader, char[] buffer, int offset) throws IOException
	{

		boolean charactersEqual = true;
		int character;

		while ((character = reader.read()) >= 0 && offset < buffer.length && charactersEqual)
		{
			if (((char) character) != buffer[offset])
			{
				charactersEqual = false;
				reader.unread(character);
				break;
			}
			offset++;
		}
		return offset;
	}

	private static String escapeCharacters(String s)
	{
		char[] escapes = {'"','/','\b','\f','\n','\r','\t'};

		String string = s.replace("\\", "\\\\");
		for (int i = 0; i < escapes.length; i++)
		{
		    string.replace("" + escapes[i], "\\" + escapes[i]);
		}
		return string;
	}
}
