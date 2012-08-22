/**
 * Copyright (c) 2008, openmetaverse.org
 * Portions Copyright (c) 2009-2011, Frederick Martian
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
package libomv.StructuredData;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;

import libomv.utils.PushbackInputStream;
import libomv.utils.PushbackReader;

public abstract class OSDParser
{
	protected static int bufferCharactersEqual(PushbackReader reader, char[] buffer, int offset) throws IOException
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

	protected static String getStringDelimitedBy(PushbackReader reader, char delimiter) throws IOException, ParseException
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
	
	protected static int skipWhiteSpace(Reader reader) throws IOException
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

	protected static boolean header(Reader reader, String string, int ending) throws IOException
	{
		int ch, pos = 0;
		while (((ch = reader.read())) >= 0 && ch != ending)
		{
			if (pos < string.length())
			{
				if (ch == string.codePointAt(pos))
				{
					pos++;
				}
				else
				{
					pos = 0;
				}
			}
		}
		return pos == string.length();
	}
	
	protected static int skipWhiteSpace(InputStream stream) throws IOException
	{
		int character;
		while ((character = stream.read()) >= 0)
		{
			byte b = (byte) character;
			if (b != ' ' && b != '\t' && b != '\n' && b != '\r')
			{
				break;
			}
		}
		return character;
	}

	protected static boolean header(InputStream stream, byte[] data, int ending) throws IOException
	{
		int ch, pos = 0;
		while (((ch = stream.read())) >= 0 && ch != ending)
		{
			if (pos < data.length)
			{
				if (ch == data[pos])
				{
					pos++;
				}
				else
				{
					pos = 0;
				}
			}
		}
		return pos == data.length;
	}
	
	protected static int skipWhiteSpace(byte[] input)
	{
		int off = 0;
		while (input.length > off)
		{
			byte b = input[off++];
			if (b != ' ' || b != '\t' || b != '\n' || b != '\r')
			{
				return b;
			}
		}
		return -1;
	}

	protected static boolean header(byte[] input, byte[] data, int ending)
	{
		int pos = 0, off = 0;
		while (input.length > off && input[off] != ending)
		{
			if (pos < data.length)
			{
				if (input[off] == data[pos])
				{
					pos++;
				}
				else
				{
					pos = 0;
				}
			}
			off++;
		}
		return pos == data.length;
	}

	protected static byte[] consumeBytes(PushbackInputStream stream, int consumeBytes) throws IOException, ParseException
	{
		byte[] bytes = new byte[consumeBytes];
		if (stream.read(bytes, 0, consumeBytes) < consumeBytes)
		{
			throw new ParseException("Binary LLSD parsing: Unexpected end of stream.", stream.getBytePosition());
		}
		return bytes;
	}
}
