/**
 * Copyright (c) 2006-2016, openmetaverse.org
 * Copyright (c) 2016-2017, Frederick Martian
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
package libomv.assets.archiving;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import libomv.utils.Helpers;

public class TarArchiveReader {
	private static final Logger logger = Logger.getLogger(TarArchiveReader.class);

	public enum TarEntryType {
		TYPE_UNKNOWN, TYPE_NORMAL_FILE, TYPE_HARD_LINK, TYPE_SYMBOLIC_LINK, TYPE_CHAR_SPECIAL, TYPE_BLOCK_SPECIAL, TYPE_DIRECTORY, TYPE_FIFO, TYPE_CONTIGUOUS_FILE,
	}

	public class TarHeader {
		public String FilePath;
		public int FileSize;
		public TarArchiveReader.TarEntryType EntryType;
	}

	/// Binary reader for the underlying stream
	protected InputStream m_br;

	/// Used to trim off null chars
	protected static final char[] m_nullCharArray = new char[] { '\0' };

	/// Used to trim off space chars
	protected static final char[] m_spaceCharArray = new char[] { ' ' };

	/// Generate a tar reader which reads from the given stream.
	/// <param name="s"></param>
	public TarArchiveReader(InputStream s) {
		m_br = s;
	}

	public byte[] ReadEntry(TarHeader header) throws IOException {
		TarHeader hdr = ReadHeader();
		if (hdr != null && hdr.FileSize > 0) {
			byte[] data = ReadData(hdr.FileSize);
			header.EntryType = hdr.EntryType;
			header.FilePath = hdr.FilePath;
			header.FileSize = hdr.FileSize;
			return data;
		}
		return null;
	}

	/// Read the next 512 byte chunk of data as a tar header.
	/// <returns>A tar header struct. null if we have reached the end of the
	/// archive.</returns>
	protected TarHeader ReadHeader() throws IOException {
		byte[] header = ReadData(512);

		// If we've reached the end of the archive we'll be in null block territory,
		// which means
		// the next byte will be 0
		if (header[0] == 0)
			return null;

		TarHeader tarHeader = new TarHeader();

		// If we're looking at a GNU tar long link then extract the long name and pull
		// up the next header
		if (header[156] == (byte) 'L') {
			int longNameLength = ConvertOctalBytesToDecimal(header, 124, 11);
			byte[] nameBytes = ReadData(longNameLength);
			tarHeader.FilePath = Helpers.BytesToString(nameBytes, 0, longNameLength, Helpers.ASCII_ENCODING);
			// Logger.Log("[TAR ARCHIVE READER]: Got long file name " + tarHeader.FilePath,
			// Logger.LogLevel.Debug);
			header = ReadData(512);
		} else {
			tarHeader.FilePath = Helpers.BytesToString(header, 0, 100).trim();
			// Logger.Log("[TAR ARCHIVE READER]: Got short file name " + tarHeader.FilePath,
			// Logger.LogLevel.Debug);
		}
		tarHeader.FileSize = ConvertOctalBytesToDecimal(header, 124, 11);

		switch (header[156]) {
		case 0:
		case (byte) '0':
			tarHeader.EntryType = TarEntryType.TYPE_NORMAL_FILE;
			break;
		case (byte) '1':
			tarHeader.EntryType = TarEntryType.TYPE_HARD_LINK;
			break;
		case (byte) '2':
			tarHeader.EntryType = TarEntryType.TYPE_SYMBOLIC_LINK;
			break;
		case (byte) '3':
			tarHeader.EntryType = TarEntryType.TYPE_CHAR_SPECIAL;
			break;
		case (byte) '4':
			tarHeader.EntryType = TarEntryType.TYPE_BLOCK_SPECIAL;
			break;
		case (byte) '5':
			tarHeader.EntryType = TarEntryType.TYPE_DIRECTORY;
			break;
		case (byte) '6':
			tarHeader.EntryType = TarEntryType.TYPE_FIFO;
			break;
		case (byte) '7':
			tarHeader.EntryType = TarEntryType.TYPE_CONTIGUOUS_FILE;
			break;
		}
		return tarHeader;
	}

	/// Read data following a header
	/// <param name="fileSize"></param>
	/// <returns></returns>
	protected byte[] ReadData(int size) throws IOException {
		int offset = 0, read = 0;
		byte[] data = new byte[size];
		while (read >= 0 && size > offset) {
			read = m_br.read(data, offset, size - offset);
			if (read >= 0)
				offset += read;
		}
		// Logger.Log("[TAR ARCHIVE READER]: size " + size, );

		if (size % 512 > 0) {
			// Read the rest of the empty padding in the 512 byte block
			int paddingLeft = 512 - (size % 512);

			// Logger.DebugLog("[TAR ARCHIVE READER]: Reading " + paddingLeft + " padding
			// bytes");
			while (read >= 0 && paddingLeft > 0) {
				read = (int) m_br.skip(paddingLeft);
				if (read >= 0)
					paddingLeft -= read;
			}
		}
		if (read < 0)
			logger.error("[TAR ARCHIVE READER]: Premature end of archive stream encounterd");
		return data;
	}

	public void close() throws IOException {
		m_br = null;
	}

	/// Convert octal bytes to a decimal representation
	/// <param name="bytes"></param>
	/// <param name="count"></param>
	/// <param name="startIndex"></param>
	/// <returns></returns>
	protected static int ConvertOctalBytesToDecimal(byte[] bytes, int startIndex, int count)
			throws UnsupportedEncodingException {
		// Trim leading white space: ancient tars do that instead
		// of leading 0s :-( don't ask. really.
		String oString = Helpers.BytesToString(bytes, startIndex, count, Helpers.ASCII_ENCODING).trim();

		int d = 0;

		for (char c : oString.toCharArray()) {
			d <<= 3;
			d |= c - '0';
		}
		return d;
	}
}
