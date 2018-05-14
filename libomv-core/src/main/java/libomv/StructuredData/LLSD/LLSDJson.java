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
package libomv.StructuredData.LLSD;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;
import java.util.Map.Entry;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDBinary;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.utils.Helpers;
import libomv.utils.PushbackReader;

public final class LLSDJson extends OSDParser
{
	private static final String llsdJsonKey = "json";
	private static final String llsdJsonHeader = "<?llsd/json?>";

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

	public static boolean isFormat(String string)
	{
		int character = skipWhiteSpace(string);
		if (character == '<')
		{
			return isHeader(string, llsdJsonKey, '>');
		}
		return false;
	}
	
	public static boolean isFormat(byte[] data, String encoding) throws UnsupportedEncodingException
	{
		int character = skipWhiteSpace(data);
		if (character == '<')
		{
			if (encoding == null)
				encoding = OSD.OSDFormat.contentEncodingDefault(OSDFormat.Json);
			return isHeader(data, llsdJsonKey.getBytes(encoding), '>');
		}
		return false;
	}

	/**
	 * Parse an JSON byte stream and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param stream The JSON reader to parse from
	 * @param encoding The text encoding to use (not used)
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten(Reader reader, String encoding) throws ParseException, IOException
	{
		PushbackReader push = reader instanceof PushbackReader ? (PushbackReader)reader : new PushbackReader(reader);
		int marker = skipWhiteSpace(push);
		if (marker < 0)
		{
			return new OSD();
		}
		else if (marker == '<')
		{
			int offset = push.getBytePosition();
			if (!isHeader(push, llsdJsonKey, '>'))
				throw new ParseException("Failed to decode Json LLSD", offset);	
		}
		else
		{
			push.unread(marker);
		}	
		return parseElement(push);
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
	protected OSD unflatten(InputStream stream, String encoding) throws ParseException, IOException
	{
		if (encoding == null)
			encoding = OSD.OSDFormat.contentEncodingDefault(OSDFormat.Json);
		PushbackReader push = new PushbackReader(new InputStreamReader(stream, encoding));
		int marker = skipWhiteSpace(push);
		if (marker < 0)
		{
			return new OSD();
		}
		else if (marker == '<')
		{
			int offset = push.getBytePosition();
			if (!isHeader(push, llsdJsonKey, '>'))
				throw new ParseException("Failed to decode Json LLSD", offset);	
		}
		else
		{
			push.unread(marker);
		}	
		return parseElement(push);
	}

	/**
	 * Serialize an hierarchical OSD object into an JSON writer
	 * 
	 * @param writer The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @param encoding The text encoding to use (not used)
	 * @throws IOException
	 */
	protected void flatten(Writer writer, OSD data, boolean prependHeader, String encoding) throws IOException
	{
		if (prependHeader)
		{
			writer.write(llsdJsonHeader);
			writer.write('\n');
		}
		serializeElement(writer, data);
	}

	/**
	 * Serialize an hierarchical OSD object into an JSON writer
	 * 
	 * @param stream The output stream to write the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	protected void flatten(OutputStream stream, OSD data, boolean prependHeader, String encoding) throws IOException
	{
		if (encoding == null)
			encoding = OSD.OSDFormat.contentEncodingDefault(OSDFormat.Json);
		Writer writer = new OutputStreamWriter(stream, encoding);
		if (prependHeader)
		{
			writer.write(llsdJsonHeader);
			writer.write('\n');
		}
		serializeElement(writer, data);
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
		int character = skipWhiteSpace(reader);
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
			default:
				break;
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
		String string = getStringDelimitedBy(reader, doubleQuotesNotationMarker);
		OSD osd = OSD.FromUUID(string);
		if (string.length() > 16)
		{
			Date date = osd.AsDate();
			if (!date.equals(Helpers.Epoch))
				return OSD.FromDate(date);
		}
		return osd;
	}
	
	private static OSD parseArray(PushbackReader reader) throws IOException, ParseException
	{
		int character = kommaNotationDelimiter;
		OSDArray osdArray = new OSDArray();
		while (((char) character == kommaNotationDelimiter) && ((character = skipWhiteSpace(reader)) > 0) && character != arrayEndNotationMarker)
		{
			reader.unread(character);
			osdArray.add(parseElement(reader));
			character = skipWhiteSpace(reader);
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
		int character = kommaNotationDelimiter;
		OSDMap osdMap = new OSDMap();
		while (((char) character == kommaNotationDelimiter) && ((character = skipWhiteSpace(reader)) > 0) && ((char) character != mapEndNotationMarker))
		{
			if (character != doubleQuotesNotationMarker)
			{
				throw new ParseException("LLSD JSON parsing: Invalid key in map", reader.getBytePosition());
			}
			String key = getStringDelimitedBy(reader, doubleQuotesNotationMarker);
			character = skipWhiteSpace(reader);
			if ((char) character != keyNotationDelimiter)
			{
				throw new ParseException("LLSD JSON parsing: Invalid key delimiter in map.",
						reader.getBytePosition());
			}
			osdMap.put(key, parseElement(reader));
			character = skipWhiteSpace(reader);
		}
		if (character < 0)
		{
			throw new ParseException("Json LLSD parsing: Unexpected end of map discovered.",
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
			case Real:
				if (Double.isNaN(osd.AsReal()) || Double.isInfinite(osd.AsReal()))
				{
					writer.write(nullNotationValue);
				}
				else
				{
				    String str = osd.AsString();
				    writer.write(str);
				    if (str.indexOf('.') == -1 && str.indexOf('E') == -1)
				    	writer.write(".0");
				}
				break;
			case Integer:
			    writer.write(osd.AsString());
			    break;
			case String:
			case UUID:
			case Date:
			case URI:
				serializeString(writer, osd.AsString());
				break;
			case Binary:
				serializeBinary(writer, (OSDBinary)osd);
				break;
			case Array:
				serializeArray(writer, (OSDArray) osd);
				break;
			case Map:
				serializeMap(writer, (OSDMap) osd);
				break;
			default:
				throw new IOException("Json serialization: Not existing element discovered.");
		}
	}

	private static void serializeString(Writer writer, String string) throws IOException
	{
		writer.write(doubleQuotesNotationMarker);
        if (string != null && string.length() > 0)
        {
            char b, c = 0;
            String hhhh;
            int i, len = string.length();

            for (i = 0; i < len; i += 1)
            {
                b = c;
                c = string.charAt(i);
                switch (c)
                {
                	case '\\':
                    case '"':
                        writer.write('\\');
                        writer.write(c);
                        break;
                    case '/':
                        if (b == '<') {
                            writer.write('\\');
                        }
                        writer.write(c);
                        break;
                    case '\b':
                        writer.write("\\b");
                        break;
                    case '\t':
                        writer.write("\\t");
                        break;
                    case '\n':
                        writer.write("\\n");
                        break;
                    case '\f':
                        writer.write("\\f");
                        break;
                    case '\r':
                        writer.write("\\r");
                        break;
                    default:
                        if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                                    || (c >= '\u2000' && c < '\u2100'))
                        {
                            hhhh = "000" + Integer.toHexString(c);
                            writer.write("\\u" + hhhh.substring(hhhh.length() - 4));
                        }
                        else
                        {
                            writer.write(c);
                        }
                }
            }
        }
		writer.write(doubleQuotesNotationMarker);
	}
	
	private static void serializeBinary(Writer writer, OSDBinary osdBinary) throws IOException
	{
		writer.write(arrayBeginNotationMarker);
		byte[] bytes = osdBinary.AsBinary();
		int lastIndex = bytes.length;

		for (int idx = 0; idx < lastIndex; idx++)
		{
			if (idx > 0)
			{
				writer.write(kommaNotationDelimiter);
			}
			writer.write(Integer.toString(bytes[idx]));
		}
		writer.write(arrayEndNotationMarker);
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
			serializeString(writer, kvp.getKey());
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
				serializeString(writer, osd.AsString());
				break;
			case Binary:
				serializeBinary(writer, (OSDBinary)osd);
				break;
			case Array:
				serializeArrayFormatted(writer, indent + baseIndent, (OSDArray) osd);
				break;
			case Map:
				serializeMapFormatted(writer, indent + baseIndent, (OSDMap) osd);
				break;
			default:
				throw new IOException("Json serialization: Not existing element discovered.");

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
			serializeString(writer, kvp.getKey());
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

	private static int BufferCharactersEqual(PushbackReader reader, char[] buffer, int offset) throws IOException
	{

		boolean charactersEqual = true;
		int character;

		while (offset < buffer.length && (character = reader.read()) >= 0 && charactersEqual)
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
}
