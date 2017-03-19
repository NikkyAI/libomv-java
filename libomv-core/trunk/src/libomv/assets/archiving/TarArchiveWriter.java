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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import libomv.utils.Helpers;

public class TarArchiveWriter
{
    /// Binary writer for the underlying stream
    protected OutputStream m_bw;

    public TarArchiveWriter(OutputStream s)
    {
        m_bw = s;
    }

    /// Write a directory entry to the tar archive.  We can only handle one path level right now!
    /// <param name="dirName"></param>
    public void writeDir(String dirName) throws IOException
    {
        // Directories are signalled by a final /
    	if (!dirName.endsWith("/"))
    		dirName += "/";
        writeFile(dirName, new byte[0]);
    }

    /// Write a file to the tar archive
    /// <param name="filePath"></param>
    /// <param name="data"></param>
    public void writeFile(String filePath, String data) throws IOException
    {
        writeFile(filePath, data.getBytes(Helpers.ASCII_ENCODING));
    }

    public void writeFile(String filePath, File file) throws IOException
    {
    	InputStream stream = new FileInputStream(file);
        byte[] data = IOUtils.toByteArray(stream);
        writeFile(filePath, data);
        stream.close();
    }

    /// Write a file to the tar archive
    /// <param name="filePath"></param>
    /// <param name="data"></param>
    public void writeFile(String filePath, byte[] data) throws IOException
    {
        if (filePath.length() > 100)
            writeEntry("././@LongLink", filePath.getBytes(Helpers.ASCII_ENCODING), 'L');

        char fileType;

        if (filePath.endsWith("/"))
        {
            fileType = '5';
        }
        else
        {
            fileType = '0';
        }
        writeEntry(filePath, data, fileType);
    }

    /// Finish writing the raw tar archive data to a stream.  The stream will be closed on completion.
    public void close() throws IOException
    {
        //m_log.Debug("[TAR ARCHIVE WRITER]: Writing final consecutive 0 blocks");

        // Write two consecutive 0 blocks to end the archive
        byte[] finalZeroPadding = new byte[1024];
        m_bw.write(finalZeroPadding);

        m_bw.flush();
        m_bw.close();
    }

    public static byte[] ConvertDecimalToPaddedOctalBytes(int d, int padding)
    {
    	char [] oString = new char[padding];
    	int pos = padding - 1;

        for (;  pos >= 0 && d > 0; pos--)
        {
            oString[pos] = (char)('0' + d & 7);
            d >>= 3;
        }

        for (; pos >= 0; pos--)
        {
            oString[pos] = (char)'0';
        }

        return Helpers.StringToBytes(new String(oString), Helpers.ASCII_ENCODING);
    }

    /// Write a particular entry
    /// <param name="filePath"></param>
    /// <param name="data"></param>
    /// <param name="fileType"></param>
    protected void writeEntry(String filePath, byte[] data, char fileType) throws IOException
    {
        byte[] header = new byte[512];        	

        // file path field (100)
        byte[] nameBytes = Helpers.StringToBytes(filePath, Helpers.ASCII_ENCODING);
        int nameSize = (nameBytes.length >= 100) ? 100 : nameBytes.length;
        System.arraycopy(header, 0, nameBytes, 0, nameSize);

        // file mode (8)
        byte[] modeBytes = Helpers.StringToBytes("0000777", Helpers.ASCII_ENCODING);
        System.arraycopy(header, 100, modeBytes, 0, 7);

        // owner user id (8)
        byte[] ownerIdBytes = Helpers.StringToBytes("0000764", Helpers.ASCII_ENCODING);
        System.arraycopy(header, 108, ownerIdBytes, 0, 7);

        // group user id (8)
        byte[] groupIdBytes = Helpers.StringToBytes("0000764", Helpers.ASCII_ENCODING);
        System.arraycopy(header, 116, groupIdBytes, 0, 7);

        // file size in bytes (12)
        int fileSize = data.length;
        //Logger.DebugLog(String.format("[TAR ARCHIVE WRITER]: File size of %s is %d", filePath, fileSize));

        byte[] fileSizeBytes = ConvertDecimalToPaddedOctalBytes(fileSize, 11);

        System.arraycopy(header, 124, fileSizeBytes, 0, 11);

        // last modification time (12)
        byte[] lastModTimeBytes = Helpers.StringToBytes("11017037332", Helpers.ASCII_ENCODING);
        System.arraycopy(header, 136, lastModTimeBytes, 0, 11);

        // entry type indicator (1)
        header[156] = (byte)fileType;

        System.arraycopy(header, 329, Helpers.StringToBytes("0000000", Helpers.ASCII_ENCODING), 0, 7);
        System.arraycopy(header, 337, Helpers.StringToBytes("0000000", Helpers.ASCII_ENCODING), 0, 7);

        // check sum for header block (8) [calculated last]
        System.arraycopy(header, 148, Helpers.StringToBytes("        ", Helpers.ASCII_ENCODING), 0, 8);

        int checksum = 0;
        for (byte b : header)
        {
            checksum += b;
        }

        //Logger.DebugLog(String.format("[TAR ARCHIVE WRITER]: Decimal header checksum is %d", checksum);

        byte[] checkSumBytes = ConvertDecimalToPaddedOctalBytes(checksum, 6);
        System.arraycopy(header, 148, checkSumBytes, 0, 6);

        header[154] = 0;

        // Write out header
        m_bw.write(header);

        // Write out data
        m_bw.write(data);

        if (data.length % 512 != 0)
        {
            int paddingRequired = 512 - (data.length % 512);

            //Logger.DebugLog(String.format("[TAR ARCHIVE WRITER]: Padding data with %d bytes", paddingRequired);

            byte[] padding = new byte[paddingRequired];
            m_bw.write(padding);
        }
    }

}
