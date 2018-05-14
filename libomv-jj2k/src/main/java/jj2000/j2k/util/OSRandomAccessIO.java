package jj2000.j2k.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;


import jj2000.j2k.io.EndianType;
import jj2000.j2k.io.RandomAccessIO;

public class OSRandomAccessIO implements RandomAccessIO
{
	/*
	 * Tha maximum size, in bytes, of the in memory buffer. The maximum size
	 * includes the EOF.
	 */
	private int maxsize;

	/* The in-memory buffer to cache received data */
	private byte buf[];

	/* The position of the next byte to be read/write in the memory buffer */
	private int pos;
	
	/* The position beyond the maximum written data */
	private int length;

	public OSRandomAccessIO()
	{
		this(1 << 18, Integer.MAX_VALUE);
	}
	
	public OSRandomAccessIO(int size, int maxsize)
	{
		if (size < 0 || maxsize <= 0)
		{
			throw new IllegalArgumentException();
		}
		// Increase size by one to count in EOF
		if (size < Integer.MAX_VALUE)
			size++;
		buf = new byte[size];
		// The maximum size is one byte more, to allow reading the EOF.
		if (maxsize < Integer.MAX_VALUE)
			maxsize++;
		this.maxsize = maxsize;
		pos = 0;
		length = 0;
	}

	public OSRandomAccessIO(byte[] data, int maxsize)
	{
		if (data == null)
		{
			throw new IllegalArgumentException();
		}

		buf = data;
		// The maximum size is one byte more, to allow reading the EOF.
		if (maxsize < Integer.MAX_VALUE)
			maxsize++;
		this.maxsize = maxsize;
		pos = 0;
		length = data.length;
	}

	/**
	 * Checks if the cache buffer can accept 'inc' bytes and if that is not the case, it grows
	 * the cache buffer by doubling the buffer size, upto a maximum of 'maxsize', making sure
	 * that at least 'inc' bytes are available after the growing of the buffer.
	 * 
	 * @exception IOException If the maximum cache size is reached or if not enough
	 *                memory is available to grow the buffer.
	 */
	private void growBuffer(int inc) throws IOException
	{
		if (pos + inc > buf.length)
		{
			byte newbuf[];
			int effinc = Math.max(buf.length << 1, inc);
			if (buf.length + effinc > maxsize)
				effinc = maxsize - buf.length;
			if (effinc <= inc)
			{
				throw new IOException("Reached maximum cache size (" + maxsize + ")");
			}
			try
			{
				newbuf = new byte[buf.length + effinc];
			}
			catch (OutOfMemoryError e)
			{
				throw new IOException("Out of memory to cache input data");
			}
			System.arraycopy(buf, 0, newbuf, 0, length);
			buf = newbuf;
		}
		
		if (pos + inc > length)
		{
			length = pos + inc;
		}
	}

	/**
	 * Reads a signed byte (8 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned signed byte (8 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public byte readByte() throws EOFException
	{
		if (pos >= length)
		{
			throw new EOFException();
		}
		return buf[pos++];
	}

	/**
	 * Reads an unsigned byte (8 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned unsigned byte (8 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public int readUnsignedByte() throws EOFException
	{
		if (pos >= length)
		{
			throw new EOFException();
		}
		return 0xFF & buf[pos++];
	}

	/**
	 * Reads a signed short (16 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned signed short (16 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public short readShort() throws EOFException
	{
		if (pos + 1 >= length)
		{
			throw new EOFException();
		}
		return (short) ((buf[pos++] << 8) | (0xFF & buf[pos++]));
	}

	/**
	 * Reads an unsigned short (16 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned unsigned short (16 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public int readUnsignedShort() throws EOFException
	{
		if (pos + 1 >= length)
		{
			throw new EOFException();
		}
		return ((0xFF & buf[pos++]) << 8) | (0xFF & buf[pos++]);
	}

	/**
	 * Reads a signed int (32 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned signed int (32 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public int readInt() throws EOFException
	{
		if (pos + 3 >= length)
		{
			throw new EOFException();
		}
		return ((buf[pos++] << 24) | ((0xFF & buf[pos++]) << 16) | ((0xFF & buf[pos++]) << 8) | (0xFF & buf[pos++]));
	}

	/**
	 * Reads a unsigned int (32 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned unsigned int (32 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public long readUnsignedInt() throws EOFException
	{
		if (pos + 3 >= length)
		{
			throw new EOFException();
		}
		return (0xFFFFFFFFL & ((buf[pos++] << 24) | ((0xFF & buf[pos++]) << 16) | ((0xFF & buf[pos++]) << 8) | (0xFF & buf[pos++])));
	}

	/**
	 * Reads a signed long (64 bit) from the internal buffer.
	 * 
	 * @return The next byte-aligned signed long (64 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public long readLong() throws EOFException
	{
		if (pos + 7 >= length)
		{
			throw new EOFException();			
		}
		return (((long) buf[pos++] << 56) | ((long) (0xFF & buf[pos++]) << 48) | ((long) (0xFF & buf[pos++]) << 40)
				| ((long) (0xFF & buf[pos++]) << 32) | ((long) (0xFF & buf[pos++]) << 24)
				| ((long) (0xFF & buf[pos++]) << 16) | ((long) (0xFF & buf[pos++]) << 8) | (0xFF & buf[pos++]));
	}

	/**
	 * Reads an IEEE single precision (i.e., 32 bit) floating-point number from
	 * the internal buffer.
	 * 
	 * @return The next byte-aligned IEEE float (32 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public float readFloat() throws EOFException
	{
		return Float.intBitsToFloat(readInt());
	}

	/**
	 * Reads an IEEE double precision (i.e., 64 bit) floating-point number from
	 * the input.
	 * 
	 * @return The next byte-aligned IEEE double (64 bit) from the input.
	 * 
	 * @exception EOFException If the end-of file was reached before getting
	 *                all the necessary data.
	 */
	@Override
	public double readDouble() throws EOFException
	{
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Returns the endianess (i.e., byte ordering) of multi-byte I/O operations.
	 * Always EndianType.BIG_ENDIAN since this class implements only big-endian.
	 * 
	 * @return Always EndianType.BIG_ENDIAN.
	 * 
	 * @see EndianType
	 */
	@Override
	public int getByteOrdering()
	{
		return EndianType.BIG_ENDIAN;
	}

	/**
	 * Skips 'n' bytes from the input.
	 * 
	 * @param n
	 *            The number of bytes to skip
	 * 
	 * @return Always n.
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public int skipBytes(int n) throws IOException
	{
		growBuffer(n);
		pos += n;
		return n;
	}

	/**
	 * Write a signed byte (8 bit) to the output.
	 * 
	 * @param The next byte-aligned signed byte (8 bit).
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public void writeByte(int v) throws IOException
	{
		growBuffer(1);
		buf[pos++] = (byte)v;
	}

	/**
	 * Write a signed short (16 bit) to the output.
	 * 
	 * @param The next byte-aligned signed short (16 bit).
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public void writeShort(int v) throws IOException
	{
		growBuffer(2);
		buf[pos++] = (byte)(v >> 8);	
		buf[pos++] = (byte)(0xFF & v);	
	}

	/**
	 * Write a signed int (32 bit) to the output.
	 * 
	 * @param The next byte-aligned signed int (32 bit).
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public void writeInt(int v) throws IOException
	{
		growBuffer(4);
		buf[pos++] = (byte)(0xFF & (v >> 24));	
		buf[pos++] = (byte)(0xFF & (v >> 16));	
		buf[pos++] = (byte)(0xFF & (v >> 8));	
		buf[pos++] = (byte)(0xFF & v);	
	}

	/**
	 * Write a signed long (64 bit) to the output.
	 * 
	 * @param The next byte-aligned signed long (64 bit).
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public void writeLong(long v) throws IOException
	{
		growBuffer(8);
		buf[pos++] = (byte)(0xFF & (v >> 56));	
		buf[pos++] = (byte)(0xFF & (v >> 48));	
		buf[pos++] = (byte)(0xFF & (v >> 40));	
		buf[pos++] = (byte)(0xFF & (v >> 32));	
		buf[pos++] = (byte)(0xFF & (v >> 24));	
		buf[pos++] = (byte)(0xFF & (v >> 16));	
		buf[pos++] = (byte)(0xFF & (v >> 8));	
		buf[pos++] = (byte)(0xFF & v);	
	}

	/**
	 * Write a 32 bit floating point to the output.
	 * 
	 * @param The next byte-aligned signed long (64 bit).
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public void writeFloat(float v) throws IOException
	{
		writeInt(Float.floatToIntBits(v));
	}

	/**
	 * Write a 64 bit floating point to the output.
	 * 
	 * @param The next byte-aligned signed long (64 bit).
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	@Override
	public void writeDouble(double v) throws IOException
	{
		writeLong(Double.doubleToLongBits(v));
	}

	/**
	 * Flush the output. Does nothing since this class stores all the data internally.
	 */
	@Override
	public void flush()
	{
	}

	/**
	 * Closes this object for reading and writing. The memory used by the cache is released.
	 */
	@Override
	public void close()
	{
		buf = null;
	}

	/**
	 * Returns the current position in the stream, which is the position from
	 * where the next byte of data would be read or written to. The first byte
	 * in the stream is in position 0.
	 */
	@Override
	public int getPos()
	{
		return pos;
	}

	/**
	 * Returns the current length of the stream, that is the position just beyond
	 * the furthest byte written to it so far.
	 * 
	 * @return The length of the stream, in bytes.
	 */
	@Override
	public int length()
	{
		return length;
	}

	/**
	 * Moves the current position for the next read/write operation to offset. The
	 * offset is measured from the beginning of the stream. If the offset is set
	 * beyond the currently cached data, the missing data will be uninitialized.
	 * Setting the offset beyond the end of the internal buffer will cause this
	 * buffer to be grown accordingly.
	 * 
	 * @param off
	 *            The offset where to move to.
	 * 
	 * @exception IOException
	 *                If an I/O error occurred.
	 */
	@Override
	public void seek(int off) throws IOException
	{
        if (off > pos)
        	growBuffer(off - pos);
		pos = off;
	}

	/**
	 * Reads one byte of data from the internal buffer.
	 * 
	 * @return the byte read
	 * 
	 * @exception EOFException If the end-of file was reached before getting all the
	 *                necessary data.
	 */
	@Override
	public int read() throws EOFException, IOException
	{
		if (pos >= length)
		{
			throw new EOFException();
		}
		return 0xFF & buf[pos++];
	}

	/**
	 * Reads 'n' bytes of data from the internal buffer into an array of bytes.
	 * 
	 * @param b The buffer into which the data is to be read. It must be long
	 *            enough.
	 * 
	 * @param off The index in 'b' where to place the first byte read.
	 * 
	 * @param len The number of bytes to read.
	 * 
	 * @exception EOFException If the end-of currently defined data was reached
	 *            before gettingall the requested data.
	 * @exception IOException If an I/O error occurred.
	 * @exception RuntimeException If an error occurred during array copy.
	 */
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException, RuntimeException
	{
		if (pos + len > length)
		{
			throw new EOFException();
		}
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
	}
	
	/**
	 * Same as writeByte()
	 */
	@Override
	public void write(int b) throws IOException
	{
		growBuffer(1);
		buf[pos++] = (byte)b;	
	}

	/**
	 * Write a byte array to the internal buffer.
	 * 
	 * @param b The byte array buffer
	 * 
	 * @exception IOException If an I/O error occurred.
	 * @exception RuntimeException If an error occurred during array copy.
	 */
	@Override
	public void write(byte[] b) throws IOException, RuntimeException
	{
		growBuffer(b.length);
		System.arraycopy(b, 0, buf, pos, b.length);
	}

	/**
	 * Write a byte array to the internal buffer.
	 * 
	 * @param b The byte array buffer
	 * @param off The offset into the byte array buffer from which to start
	 * @param len The number of bytes to copy to the internal buffer
	 * 
	 * @exception IOException If an I/O error occurred.
	 * @exception RuntimeException If an error occurred during array copy.
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException, RuntimeException
	{
		growBuffer(len);
		System.arraycopy(b, off, buf, pos, len);
	}
	
	/**
	 * Write the entire output buffer to the output stream
	 * 
	 * @param b The byte array buffer
	 * 
	 * @exception IOException If an I/O error occurred.
	 */
	public int writeTo(OutputStream os) throws IOException
	{
		os.write(buf, 0, length);
		return length;
	}
	
	public byte[] toByteArray()
	{
		return buf;
	}

}
