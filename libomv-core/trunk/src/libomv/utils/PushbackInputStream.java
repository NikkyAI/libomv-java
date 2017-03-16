/**
 * Copyright (c) 2009-2014, Frederick Martian
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
package libomv.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PushbackInputStream extends FilterInputStream
{
	/**
	 * The pushback buffer.
	 */
	private byte[] buf;

	/**
	 * The position within the pushback buffer from which the next byte will be
	 * read. When the buffer is empty, <code>bufSpace</code> is equal to
	 * <code>buf.length</code>; when the buffer is full, <code>bufSpace</code> is
	 * equal to zero.
	 */
	private int bufStart;
	private int bufEnd;

	/**
	 * The number of bytes read so far
	 */
	private long bytesRead;

	public long getBytePosition()
	{
		return bytesRead;
	}

	/**
	 * Constructs a new {@code PushbackInputStream} with {@code in} as source stream.
	 * The size of the pushback buffer is set to 1.
	 * 
	 * @param in
	 *            the source stream.
	 * @param offset
	 *            the offset in bytes into the original source data
	 */
	public PushbackInputStream(InputStream in)
	{
		this(in, 0, 1);
	}

	/**
	 * Constructs a new {@code PushbackInputStream} with {@code in} as source stream.
	 * The size of the pushback buffer is set to 1.
	 * 
	 * @param in
	 *            the source stream.
	 * @param offset
	 *            the offset in bytes into the original source data
	 * @throws IllegalArgumentException
	 *            if {@code offset} is negative..
	 */
	public PushbackInputStream(InputStream in, int offset)
	{
		this(in, offset, 1);
	}

	/**
	 * Constructs a new {@code PushbackInputStream} with {@code in} as source stream.
	 * The size of the pushback buffer is set to {@code size}.
	 * 
	 * @param in
	 *            the source stream.
	 * @param offset
	 *            the offset in bytes into the original source data
	 * @param size
	 *            the size of the pushback buffer.
	 * @throws IllegalArgumentException
	 *            if {@code size} is zero or negative or if {@code offset} is negative..
	 */
	public PushbackInputStream(InputStream in, int offset, int size)
	{
		super(in);
		if (size <= 0)
		{
			throw new IllegalArgumentException("size <= 0");
		}
		if (offset < 0)
		{
			throw new IllegalArgumentException("offset < 0");
		}
		this.buf = new byte[size];
		this.bufStart = 0;
		this.bufEnd = 0;
		this.bytesRead = offset;
	}

	@Override
	public int available() throws IOException
	{
		if (buf == null)
			throw new IOException("Stream closed");

		return (bufEnd - bufStart) + super.available();
	}

	/**
	 * Closes this input stream and releases any system resources associated
	 * with the stream.
	 * 
	 * @throws IOException
	 *             if an error occurs while closing this reader.
	 */
	@Override
	public synchronized void close() throws IOException
	{
		if (buf == null)
			return;
		super.close();
		in = null;
		buf = null;
	}

	/**
	 * Indicates whether this reader supports the {@code mark(int)} and
	 * {@code reset()} methods. {@code PushbackReader} does not support them, so
	 * it returns {@code false}.
	 * 
	 * @return always {@code false}.
	 * @see #mark(int)
	 * @see #reset()
	 */
	@Override
	public boolean markSupported()
	{
		return false;
	}

	/**
	 * Marks the current position in this stream. Setting a mark is not
	 * supported in this class; this implementation always throws an
	 * {@code IOException}.
	 * 
	 * @param readLimit
	 *            the number of character that can be read from this reader
	 *            before the mark is invalidated; this parameter is ignored.
	 */
	@Override
	public synchronized void mark(int readLimit)
	{
	}

	/**
	 * Reads a single byte from this stream and returns it as an integer.
	 * Returns -1 if the end of the reader has been reached. If the pushback
	 * buffer does not contain any available characters then a character from
	 * the source reader is returned. Blocks until one character has been read,
	 * the end of the source reader is detected or an exception is thrown.
	 * 
	 * @return the byte read or -1 if the end of the source reader has been
	 *         reached.
	 * 
	 * @throws IOException
	 *             if this stream is closed or an I/O error occurs while reading
	 *             from this stream.
	 */
	@Override
	public int read() throws IOException
	{
		if (buf == null)
			throw new IOException("Stream closed");

		if (bufEnd > bufStart)
		{
			bytesRead++;
			return buf[bufStart++] & 0xff;
		}
		int read = super.read();
		if (read > 0)
			bytesRead++;
		return read;
	}

	/**
	 * Reads at most {@code length} bytes from this stream and stores them in
	 * the byte array {@code buffer} starting at {@code offset}. Characters are
	 * read from the pushback buffer first, then from the source reader if more
	 * bytes are required. Blocks until {@code count} characters have been read,
	 * the end of the source reader is detected or an exception is thrown.
	 * 
	 * @param buffer
	 *            the array in which to store the characters read from this
	 *            reader.
	 * @param offset
	 *            the initial position in {@code buffer} to store the characters
	 *            read from this reader.
	 * @param count
	 *            the maximum number of bytes to store in {@code buffer}.
	 * @return the number of bytes read or -1 if the end of the source reader
	 *         has been reached.
	 * @throws IndexOutOfBoundsException
	 *             if {@code offset < 0} or {@code count < 0}, or if
	 *             {@code offset + count} is greater than the length of
	 *             {@code buffer}.
	 * @throws IllegalArgumentException
	 *             when a null byte buffer has been passed in
	 * @throws IOException
	 *             if this reader is closed or another I/O error occurs while
	 *             reading from this reader.
	 */
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException
	{
		if (buf == null)
			throw new IOException("Stream closed");

		if (buffer == null)
			throw new IllegalArgumentException("Null buffer");

		if ((offset < 0) || (offset > buffer.length) || (length < 0) || ((offset + length) > buffer.length) || ((offset + length) < 0))
		{
			throw new IndexOutOfBoundsException();
		}

		if (length == 0)
		{
			return 0;
		}

		int bufAvail = bufEnd - bufStart;
		if (bufAvail > 0)
		{
			if (length <bufAvail)
			{
				bufAvail = length;
			}
			System.arraycopy(buf, bufStart, buffer, offset, bufAvail);
			bufStart += bufAvail;
			if (bufStart == bufEnd)
			{
				bufStart = 0;
				bufEnd = 0;
			}
			bytesRead += bufAvail;
			offset +=bufAvail;
			length -= bufAvail;
		}

		if (length > 0)
		{
			length = super.read(buffer, offset, length);
			if (length == -1)
			{
				return bufAvail == 0 ? -1 : bufAvail;
			}
			bytesRead += length;
		}
		return bufAvail + length;
	}

	/**
	 * Resets this stream to the last marked position. Resetting the stream is
	 * not supported in this class; this implementation always throws an
	 * {@code IOException}.
	 * 
	 * @throws IOException
	 *             if this method is called.
	 */
	@Override
	public synchronized void reset() throws IOException
	{
		throw new IOException("mark/reset not supported");
	}

	/**
	 * Pushes the specified character {@code oneChar} back to this reader. This
	 * is done in such a way that the next character read from this reader is
	 * {@code (char) oneChar}.
	 * <p>
	 * If this reader's internal pushback buffer cannot store the character, an
	 * {@code IOException} is thrown.
	 * 
	 * @param oneChar
	 *            the character to push back to this stream.
	 * @throws IOException
	 *             if this reader is closed or the internal pushback buffer is
	 *             full.
	 */
	public void unread(int b) throws IOException
	{
		if (buf == null)
			throw new IOException("Stream closed");

		if (buf.length == bufEnd)
		{
			if (bufStart > 0)
			{
				System.arraycopy(buf, bufStart, buf, 0, bufEnd - bufStart);
				bufEnd -= bufStart;
				bufStart = 0;
			}
			else
			{
				throw new IOException("Push back buffer is full");
			}
		}
		buf[bufEnd++] = (byte) b;
		bytesRead--;
	}

	/**
	 * Pushes a subset of the bytes in {@code buffer} back to this stream. The
	 * subset is defined by the start position {@code offset} within
	 * {@code buffer} and the number of characters specified by {@code length}.
	 * The bytes are pushed back in such a way that the next byte read from this
	 * stream is {@code buffer[offset]}, then {@code buffer[1]} and so on.
	 * <p>
	 * If this stream's internal pushback buffer cannot store the selected
	 * subset of {@code buffer}, an {@code IOException} is thrown.
	 * 
	 * @param buffer
	 *            the buffer containing the characters to push back to this
	 *            reader.
	 * @param offset
	 *            the index of the first byte in {@code buffer} to push back.
	 * @param length
	 *            the number of bytes to push back.
	 * @throws IndexOutOfBoundsException
	 *             if {@code offset < 0} or {@code count < 0}, or if
	 *             {@code offset + count} is greater than the length of
	 *             {@code buffer}.
	 * @throws IOException
	 *             if this reader is closed or the free space in the internal
	 *             pushback buffer is not sufficient to store the selected
	 *             contents of {@code buffer}.
	 * @throws NullPointerException
	 *             if {@code buffer} is {@code null}.
	 */
	public void unread(byte[] buffer, int offset, int length) throws IOException
	{
		if (buf == null)
			throw new IOException("Stream closed");

		// Force buffer null check first!
		if (offset > buffer.length - length || offset < 0)
		{
			throw new ArrayIndexOutOfBoundsException("Offset out of bounds");
		}

		if (length < 0)
		{
			throw new ArrayIndexOutOfBoundsException("Length out of bounds");
		}

		if (length > buf.length - bufEnd + bufStart)
		{
			throw new IOException("Push back buffer is full");
		}

		if (length > buf.length - bufEnd)
		{
			System.arraycopy(buf, bufStart, buf, 0, bufEnd - bufStart);
			bufEnd -= bufStart;
			bufStart = 0;
		}	
		System.arraycopy(buffer, offset, buf, bufEnd, length);
		bufEnd += length;
		bytesRead -= length;
	}

	/**
	 * Pushes all the bytes in {@code buffer} back to this reader. The bytes are
	 * pushed back in such a way that the next character read from this stream
	 * is buffer[0], then buffer[1] and so on.
	 * <p>
	 * If this streams's internal pushback buffer cannot store the entire
	 * contents of {@code buffer}, an {@code IOException} is thrown.
	 * 
	 * @param buffer
	 *            the buffer containing the characters to push back to this
	 *            reader.
	 * @throws IOException
	 *             if this reader is closed or the free space in the internal
	 *             pushback buffer is not sufficient to store the contents of
	 *             {@code buffer}.
	 */
	public void unread(byte[] buffer) throws IOException
	{
		unread(buffer, 0, buffer.length);
	}

	/**
	 * Skips {@code count} bytes in this stream. This implementation skips bytes
	 * in the pushback buffer first and then in the source reader if necessary.
	 * 
	 * @param count
	 *            the number of characters to skip.
	 * @return the number of characters actually skipped.
	 * @throws IllegalArgumentException
	 *             if {@code count < 0}.
	 * @throws IOException
	 *             if this reader is closed or another I/O error occurs.
	 */
	@Override
	public long skip(long count) throws IOException
	{
		if (count < 0)
		{
			throw new IllegalArgumentException();
		}

		if (buf == null)
			throw new IOException("Stream closed");

		if (count == 0)
		{
			return 0;
		}

		int bufAvail = bufEnd - bufStart;
		if (bufAvail > 0)
		{
			if (count < bufAvail)
			{
				bufStart += count;
				count = 0;
			}
			else
			{
				bufStart = 0;
				bufEnd = 0;
				count -= bufAvail;
			}
		}
		if (count > 0)
		{
			bufAvail += super.skip(count);
		}
		bytesRead += bufAvail;
		return bufAvail;
	}
}
