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
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDString;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.PushbackReader;

public final class LLSDNotation
{
	private static final String llsdNotationHeader = "<?llsd/notation?>";

	private static final String baseIndent = "  ";

	private static final char[] newLine = { '\n' };

	private static final char undefNotationValue = '!';

	private static final char trueNotationValueOne = '1';
	private static final char trueNotationValueTwo = 't';
	private static final char[] trueNotationValueTwoFull = { 't', 'r', 'u', 'e' };
	private static final char trueNotationValueThree = 'T';
	private static final char[] trueNotationValueThreeFull = { 'T', 'R', 'U', 'E' };

	private static final char falseNotationValueOne = '0';
	private static final char falseNotationValueTwo = 'f';
	private static final char[] falseNotationValueTwoFull = { 'f', 'a', 'l', 's', 'e' };
	private static final char falseNotationValueThree = 'F';
	private static final char[] falseNotationValueThreeFull = { 'F', 'A', 'L', 'S', 'E' };

	private static final char integerNotationMarker = 'i';
	private static final char realNotationMarker = 'r';
	private static final char uuidNotationMarker = 'u';
	private static final char binaryNotationMarker = 'b';
	private static final char stringNotationMarker = 's';
	private static final char uriNotationMarker = 'l';
	private static final char dateNotationMarker = 'd';

	private static final char arrayBeginNotationMarker = '[';
	private static final char arrayEndNotationMarker = ']';

	private static final char mapBeginNotationMarker = '{';
	private static final char mapEndNotationMarker = '}';
	private static final char kommaNotationDelimiter = ',';
	private static final char keyNotationDelimiter = ':';

	private static final char sizeBeginNotationMarker = '(';
	private static final char sizeEndNotationMarker = ')';
	private static final char doubleQuotesNotationMarker = '"';
	private static final char singleQuotesNotationMarker = '\'';

	/**
	 * Parse a LLSD Notation string and convert it into an hierarchical OSD object
	 * 
	 * @param string The LLSD Notation string to parse
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
	 * Parse an LLSD Notation byte stream and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param stream The LLSD Notation byte stream to parse
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
	 * Parse an LLSD Notation reader and convert it into an hierarchical OSD object
	 * 
	 * @param reader The LLSD Notation reader to parse
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	public static OSD parse(Reader reader) throws ParseException, IOException
	{
		return parseElement(reader instanceof PushbackReader ? (PushbackReader)reader : new PushbackReader(reader));
	}
	
	/**
	 * Parse an LLSD Notation byte stream and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param stream The LLSD Notation byte stream to parse
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
	 * Serialize an hierarchical OSD object into an LLSD Notation string
	 * 
	 * @param stream The hierarchical LLSD Notation byte stream
	 * @param data The hierarchical OSD object to serialize
	 * @param encoding The text encoding to use when converting the text into
	 *            a byte stream
	 * @throws IOException
	 */
	public static void serialize(OutputStream stream, OSD data, String encoding) throws IOException
	{
		serialize(new OutputStreamWriter(stream, encoding), data, true);
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation string
	 * 
	 * @param stream The hierarchical LLSD Notation byte stream
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @param encoding The text encoding to use when converting the text into
	 *            a byte stream
	 * @throws IOException
	 */
	public static void serialize(OutputStream stream, OSD data, boolean prependHeader, String encoding) throws IOException
	{
		serialize(new OutputStreamWriter(stream, encoding), data, prependHeader);
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation writer
	 * 
	 * @param writer The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @throws IOException
	 */
	public static void serialize(Writer writer, OSD data) throws IOException
	{
		serialize(writer, data, true);
	}
	
	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation writer
	 * 
	 * @param writer The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	public static void serialize(Writer writer, OSD data, boolean prependHeader) throws IOException
	{
		if (prependHeader)
		{
			writer.write(llsdNotationHeader);
			writer.write('\n');
		}
		serializeElement(writer, data);
	}
	
	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation string
	 * 
	 * @param data The hierarchical OSD object to serialize
	 * @return an LLSD Notation formatted string
	 * @throws IOException
	 */
	public static String serializeToString(OSD data) throws IOException
	{
		Writer writer = new StringWriter();
		try
		{
			serialize(writer, data, true);
			return writer.toString();
		}
		finally
		{
			writer.close();
		}
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation string
	 * 
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @return an JSON formatted string
	 * @throws IOException
	 */
	public static String serializeToString(OSD data, boolean prependHeader) throws IOException
	{
		Writer writer = new StringWriter();
		try
		{
			serialize(writer, data, prependHeader);
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
	 * @param reader
	 *            a pushback reader to read in data from
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

		int matching;
		switch ((char) character)
		{
			case undefNotationValue:
				return new OSD();
			case trueNotationValueOne:
				return OSD.FromBoolean(true);
			case trueNotationValueTwo:
				matching = BufferCharactersEqual(reader, trueNotationValueTwoFull, 1);
				if (matching > 1 && matching < trueNotationValueTwoFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(true);
			case trueNotationValueThree:
				matching = BufferCharactersEqual(reader, trueNotationValueThreeFull, 1);
				if (matching > 1 && matching < trueNotationValueThreeFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(true);
			case falseNotationValueOne:
				return OSD.FromBoolean(false);
			case falseNotationValueTwo:
				matching = BufferCharactersEqual(reader, falseNotationValueTwoFull, 1);
				if (matching > 1 && matching < falseNotationValueTwoFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(false);
			case falseNotationValueThree:
				matching = BufferCharactersEqual(reader, falseNotationValueThreeFull, 1);
				if (matching > 1 && matching < falseNotationValueThreeFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(false);
			case integerNotationMarker:
				return parseInteger(reader);
			case realNotationMarker:
				return parseReal(reader);
			case uuidNotationMarker:
				char[] uuidBuf = new char[36];
				if (reader.read(uuidBuf, 0, 36) < 36)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in UUID.",
							reader.getBytePosition());
				}
				return OSD.FromUUID(new UUID(uuidBuf.toString()));
			case binaryNotationMarker:
				byte[] bytes = Helpers.EmptyBytes;
				int bChar = reader.read();
				if (bChar < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
							reader.getBytePosition());
				}
				else if (bChar == sizeBeginNotationMarker)
				{
					throw new ParseException("Notation LLSD parsing: Raw binary encoding not supported.",
							reader.getBytePosition());
				}
				else if (Character.isDigit((char) bChar))
				{
					char[] charsBaseEncoding = new char[2];
					if (reader.read(charsBaseEncoding, 0, 2) < 2)
					{
						throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
								reader.getBytePosition());
					}
					int baseEncoding = new Integer(new String(charsBaseEncoding));
					if (baseEncoding == 64)
					{
						if (reader.read() < 0)
						{
							throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
									reader.getBytePosition());
						}
						String bytes64 = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
						bytes = Base64.decodeBase64(bytes64);
					}
					else
					{
						throw new ParseException("Notation LLSD parsing: Encoding base" + baseEncoding
								+ " + not supported.", reader.getBytePosition());
					}
				}
				return OSD.FromBinary(bytes);
			case stringNotationMarker:
				int numChars = GetLengthInBrackets(reader);
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				char[] chars = new char[numChars];
				if (reader.read(chars, 0, numChars) < numChars)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				return OSD.FromString(new String(chars));
			case singleQuotesNotationMarker:
				String sOne = GetStringDelimitedBy(reader, singleQuotesNotationMarker);
				return OSD.FromString(sOne);
			case doubleQuotesNotationMarker:
				String sTwo = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
				return OSD.FromString(sTwo);
			case uriNotationMarker:
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				URI uri;
				try
				{
					uri = new URI(GetStringDelimitedBy(reader, doubleQuotesNotationMarker));
				}
				catch (Throwable t)
				{
					throw new ParseException("Notation LLSD parsing: Invalid Uri format detected.",
							reader.getBytePosition());
				}
				return OSD.FromUri(uri);
			case dateNotationMarker:
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in date.",
							reader.getBytePosition());
				}
				String date = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
				return OSD.FromDate(new OSDString(date).AsDate());
			case arrayBeginNotationMarker:
				return parseArray(reader);
			case mapBeginNotationMarker:
				return parseMap(reader);
			default:
		}
		throw new ParseException("Notation LLSD parsing: Unknown type marker '" + (char) character + "'.",
				reader.getBytePosition());
	}

	private static OSD parseInteger(PushbackReader reader) throws IOException
	{
		int character;
		StringBuilder s = new StringBuilder();
		if (((character = reader.read()) > 0) && ((char) character == '-' || (char) character == '+'))
		{
			s.append((char) character);
		}
		while (character >= 0 && Character.isDigit((char) character))
		{
			s.append((char) character);
			character = reader.read();
		}
		if (character >= 0)
		{
			reader.unread(character);
		}
		return OSD.FromInteger(new Integer(s.toString()));
	}

	private static OSD parseReal(PushbackReader reader) throws IOException
	{
		int character;
		StringBuilder s = new StringBuilder();
		if (((character = reader.read()) > 0) && ((char) character == '-' || (char) character == '+'))
		{
			s.append((char) character);
		}
		while ((character >= 0)
				&& (Character.isDigit((char) character) || (char) character == '.' || (char) character == 'e'
						|| (char) character == 'E' || (char) character == '+' || (char) character == '-'))
		{
			s.append((char) character);
			character = reader.read();
		}
		if (character >= 0)
		{
			reader.unread(character);
		}
		return OSD.FromReal(new Double(s.toString()));
	}

	private static OSD parseArray(PushbackReader reader) throws IOException, ParseException
	{
		int character;
		OSDArray osdArray = new OSDArray();
		while (((character = skipWhitespace(reader)) > 0) && ((char) character != arrayEndNotationMarker))
		{
			reader.unread(character);
			osdArray.add(parseElement(reader));

			character = skipWhitespace(reader);
			if (character < 0)
			{
				throw new ParseException("Notation LLSD parsing: Unexpected end of array discovered.",
						reader.getBytePosition());
			}
			else if ((char) character == arrayEndNotationMarker)
			{
				break;
			}
		}
		if (character < 0)
		{
			throw new ParseException("Notation LLSD parsing: Unexpected end of array discovered.",
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
				throw new ParseException("Notation LLSD parsing: Invalid key in map", reader.getBytePosition());
			}
			String key = osdKey.AsString();

			character = skipWhitespace(reader);
			if ((char) character != keyNotationDelimiter)
			{
				throw new ParseException("Notation LLSD parsing: Invalid key delimiter in map.", reader.getBytePosition());
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
				writer.write(undefNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValueTwo);
				}
				else
				{
					writer.write(falseNotationValueTwo);
				}
				break;
			case Integer:
				writer.write(integerNotationMarker);
				writer.write(osd.AsString());
				break;
			case Real:
				writer.write(realNotationMarker);
				writer.write(osd.AsString());
				break;
			case UUID:
				writer.write(uuidNotationMarker);
				writer.write(osd.AsString());
				break;
			case String:
				writer.write(singleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), singleQuotesNotationMarker));
				writer.write(singleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(binaryNotationMarker);
				writer.write("64");
				writer.write(doubleQuotesNotationMarker);
				writer.write(Base64.encodeBase64String(osd.AsBinary()));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Date:
				writer.write(dateNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case URI:
				writer.write(uriNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), doubleQuotesNotationMarker));
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
			writer.write(singleQuotesNotationMarker);
			writer.write(escapeCharacter(kvp.getKey(), singleQuotesNotationMarker));
			writer.write(singleQuotesNotationMarker);
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
				writer.write(undefNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValueTwo);
				}
				else
				{
					writer.write(falseNotationValueTwo);
				}
				break;
			case Integer:
				writer.write(integerNotationMarker);
				writer.write(osd.AsString());
				break;
			case Real:
				writer.write(realNotationMarker);
				writer.write(osd.AsString());
				break;
			case UUID:
				writer.write(uuidNotationMarker);
				writer.write(osd.AsString());
				break;
			case String:
				writer.write(singleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), singleQuotesNotationMarker));
				writer.write(singleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(binaryNotationMarker);
				writer.write("64");
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case Date:
				writer.write(dateNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case URI:
				writer.write(uriNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), doubleQuotesNotationMarker));
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

	private static void serializeArrayFormatted(Writer writer, String intend, OSDArray osdArray) throws IOException
	{
		writer.write(newLine);
		writer.write(intend);
		writer.write(arrayBeginNotationMarker);
		int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			OSD.OSDType type = osdArray.get(idx).getType();
			if (type != OSD.OSDType.Array && type != OSD.OSDType.Map)
			{
				writer.write(newLine);
			}
			writer.write(baseIndent + intend);
			serializeElementFormatted(writer, intend, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
		}
		writer.write(newLine);
		writer.write(intend);
		writer.write(arrayEndNotationMarker);
	}

	private static void serializeMapFormatted(Writer writer, String intend, OSDMap osdMap) throws IOException
	{
		writer.write(newLine);
		writer.write(intend);
		writer.write(mapBeginNotationMarker);
		writer.write(newLine);
		int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(baseIndent + intend);
			writer.write(singleQuotesNotationMarker);
			writer.write(escapeCharacter(kvp.getKey(), singleQuotesNotationMarker));
			writer.write(singleQuotesNotationMarker);
			writer.write(keyNotationDelimiter);
			serializeElementFormatted(writer, intend, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(newLine);
				writer.write(baseIndent + intend);
				writer.write(kommaNotationDelimiter);
				writer.write(newLine);
			}

			idx++;
		}
		writer.write(newLine);
		writer.write(intend);
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

	public static int GetLengthInBrackets(PushbackReader reader) throws IOException, ParseException
	{
		int character;
		StringBuilder s = new StringBuilder();
		if (((character = skipWhitespace(reader)) > 0) && ((char) character == sizeBeginNotationMarker))
			;
		while ((character >= 0) && Character.isDigit((char) character) && ((char) character != sizeEndNotationMarker))
		{
			s.append((char) character);
			reader.read();
		}
		if (character < 0)
		{
			throw new ParseException("Notation LLSD parsing: Can't parse length value cause unexpected end of stream.",
					reader.getBytePosition());
		}
		reader.unread(character);
		return new Integer(s.toString());
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

	private static String escapeCharacter(String s, char c)
	{
		String oldOne = "" + c;
		String newOne = "\\" + c;

		String sOne = s.replace("\\", "\\\\").replace(oldOne, newOne);
		return sOne;
	}
}
