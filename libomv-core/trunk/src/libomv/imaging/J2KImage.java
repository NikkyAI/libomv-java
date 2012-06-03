/**
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
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
package libomv.imaging;

import icc.ICCProfileException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import jj2000.j2k.decoder.ImgDecoder;
import jj2000.j2k.encoder.ImgEncoder;
import jj2000.j2k.fileformat.reader.FileFormatReader;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.Coord;
import jj2000.j2k.image.DataBlkInt;
import jj2000.j2k.io.RandomAccessIO;
import jj2000.j2k.util.ISRandomAccessIO;
import jj2000.j2k.util.ParameterList;

public class J2KImage extends ManagedImage
{
	public class J2KLayerInfo
	{
        public int Start;
        public int End;
	}

	private class PixelScale
	{
		int ls, mv, fb;
	}

	/**
     * create a <seealso cref="ManagedImage"/> object from a JPEG2K stream
     *
     * @param is The input stream
     */
	public J2KImage(InputStream is) throws IllegalArgumentException, IOException, ICCProfileException
	{
		BlkImgDataSrc dataSrc = decodeInternal(is);
		
		int ncomps = dataSrc.getNumComps();

		// Check component sizes and bit depths
		Height = dataSrc.getCompImgHeight(0);
		Width = dataSrc.getCompImgWidth(0);
		for (int i = dataSrc.getNumComps() - 1; i >= 0; i--)
		{
			if (dataSrc.getCompImgHeight(i) != Height || dataSrc.getCompImgWidth(i) != Width)
			{
				throw new IllegalArgumentException("All components must have the same dimensions and no subsampling");
			}
			if (dataSrc.getNomRangeBits(i) > 8)
			{
				throw new IllegalArgumentException("Depths greater than 8 bits per component is not supported");
			}
		}
				
		PixelScale[] scale = new PixelScale[ncomps];
		
		for (int i = 0; i < ncomps; i++)
		{
			scale[i].ls = 1 << (dataSrc.getNomRangeBits(i) - 1);
			scale[i].mv = (1 << dataSrc.getNomRangeBits(i)) - 1;
			scale[i].fb = dataSrc.getFixedPoint(i);
		}
		
		switch (ncomps)
		{
			case 5:
				Channels = ManagedImage.ImageChannels.Bump;
			case 4:
				Channels |= ManagedImage.ImageChannels.Alpha;
			case 3:
				Channels |= ManagedImage.ImageChannels.Color;
				break;
			case 2:
				Channels = ManagedImage.ImageChannels.Alpha;
			case 1:
				Channels |= ManagedImage.ImageChannels.Gray;
				break;
			default:
				throw new IllegalArgumentException("Decoded image with unhandled number of components: " + ncomps);
		}		

		int height; // tile height
		int width; // tile width
		int tOffx, tOffy; // Active tile offset
		int tIdx = 0; // index of the current tile
		int off, l, x, y;
		Coord nT = dataSrc.getNumTiles(null);
		DataBlkInt block = new DataBlkInt();
		block.ulx = 0;
		block.h = 1;
	
		// Start the data delivery to the cached consumers tile by tile
		for (y = 0; y < nT.y; y++)
		{
			// Loop on horizontal tiles
			for (x = 0; x < nT.x; x++, tIdx++)
			{
				dataSrc.setTile(x, y);

				// Initialize tile
				height = dataSrc.getTileCompHeight(tIdx, 0);
				width = dataSrc.getTileCompWidth(tIdx, 0);

				// The offset of the active tiles is the same for all components,
				// since we don't support different component dimensions.
				tOffx = dataSrc.getCompULX(0) - (int) Math.ceil(dataSrc.getImgULX() / (double) dataSrc.getCompSubsX(0));
				tOffy = dataSrc.getCompULY(0) - (int) Math.ceil(dataSrc.getImgULY() / (double) dataSrc.getCompSubsY(0));
				off = tOffy * Width + tOffx;

				// Deliver in lines to reduce memory usage
				for (l = 0; l < height; l++)
				{
					block.uly = l;
					block.w = width;

					switch (ncomps)
					{
						case 5:
							dataSrc.getInternCompData(block, 4);
							fillLine(block, scale[4], Bump, off);
						case 4:
							dataSrc.getInternCompData(block, 3);
							fillLine(block, scale[3], Alpha, off);
						case 3:
							dataSrc.getInternCompData(block, 2);
							fillLine(block, scale[2], Blue, off);
							dataSrc.getInternCompData(block, 1);
							fillLine(block, scale[1], Green, off);
							dataSrc.getInternCompData(block, 0);
							fillLine(block, scale[0], Red, off);
							break;
						case 2:
							dataSrc.getInternCompData(block, 1);
							fillLine(block, scale[1], Alpha, off);
						case 1:
							dataSrc.getInternCompData(block, 0);
							fillLine(block, scale[0], Red, off);
							System.arraycopy(Red, off, Green, off, width);
							System.arraycopy(Red, off, Blue, off, width);
							break;
					}
				}
			}
		}
	}
	
    /**
     * Encode this <seealso cref="ManagedImage"/> object into a byte array
     *
     * @param image The <seealso cref="ManagedImage"/> object to encode
     */
    public int encode(OutputStream os)
    {
        return encode(os, this, false);
    }

    /**
     * Encode a <seealso cref="ManagedImage"/> object into a byte array
     *
     * @param image The <seealso cref="ManagedImage"/> object to encode
     */
    public static int encode(OutputStream os, ManagedImage image)
    {
        return encode(os, image, false);
    }

    /**
     * Encode a <seealso cref="ManagedImage"/> object into a byte array
     * 
     * @param image The <seealso cref="ManagedImage"/> object to encode
     * @param lossless true to enable lossless conversion, only useful for small images ie: sculptmaps
     * @return
     */
    public static int encode(OutputStream os, ManagedImage image, boolean lossless)
    {
        if (((image.Channels & ManagedImage.ImageChannels.Gray) != 0 && 
             ((image.Channels & ManagedImage.ImageChannels.Color) != 0) ||
             ((image.Channels & ManagedImage.ImageChannels.Bump) != 0))||
        	((image.Channels & ManagedImage.ImageChannels.Bump) != 0 && 
        	 (image.Channels & ManagedImage.ImageChannels.Alpha) == 0))
            throw new IllegalArgumentException("JPEG2000 encoding is not supported for this channel combination");

        int components = 1;
        if ((image.Channels & ManagedImage.ImageChannels.Color) != 0)
        	components = 3;
        if ((image.Channels & ManagedImage.ImageChannels.Alpha) != 0)
        	components++;
        if ((image.Channels & ManagedImage.ImageChannels.Bump) != 0)
        	components++;

        // Initialize default parameters
        ParameterList defpl = new ParameterList();
        String[][] param = ImgEncoder.getAllParameters();

        for (int i = param.length - 1; i >= 0; i--)
        {
        	if (param[i][3] != null)
        	{
        		defpl.put(param[i][0],param[i][3]);
            }
        }

        // Create parameter list using defaults
        ImgEncoder enc = new ImgEncoder(new ParameterList(defpl));
 
        boolean[] imsigned = new boolean[components];
//		BlkImgDataSrc imgsrc = new ImgReaderMI(image);

//  	enc.encode(imgsrc, imsigned, components, false, os, true, false);

        
        

        return 0;
    }

	private static void fillLine(DataBlkInt blk, PixelScale scale, byte[] data, int off)
	{
		int k1 = blk.offset + blk.w - 1;
		for (int i = blk.w - 1; i >= 0; i--)
		{
			int temp = (blk.data[k1--] >> scale.fb) + scale.ls;
			temp = (temp < 0) ? 0 : ((temp > scale.mv) ? scale.mv : temp);
			data[off + i] = (byte)temp;
		}
	}

	public static J2KLayerInfo[] decodeLayerBoundaries(byte[] encoded)
	{
		return null;
	}
	
	private static BlkImgDataSrc decodeInternal(InputStream is) throws IOException, ICCProfileException
	{
		ParameterList defpl = new ParameterList();
    	String[][] param = ImgDecoder.getAllParameters();

        for (int i = param.length - 1; i >= 0; i--)
        {
    	    if (param[i][3] != null)
    	    {
    	    	defpl.put(param[i][0], param[i][3]);
            }
        }

        ImgDecoder decoder = new ImgDecoder(new ParameterList(defpl));
        
        RandomAccessIO in = new ISRandomAccessIO(is);

		// **** File Format ****
		// If the codestream is wrapped in the jp2 fileformat, Read the file format wrapper
		FileFormatReader ff = new FileFormatReader(in);
		ff.readFileFormat();
		if (ff.JP2FFUsed)
		{
			in.seek(ff.getFirstCodeStreamPos());
		}
		return decoder.decode(in, ff, false);
	}
}
