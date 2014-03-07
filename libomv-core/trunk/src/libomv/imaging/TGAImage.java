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
 package libomv.imaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import libomv.imaging.TGAHeader.TGAColorMap;

import org.apache.commons.io.input.SwappedDataInputStream;

public class TGAImage extends ManagedImage
{
    public TGAImage(ManagedImage image) throws IOException
	{
    	super(image);
	}
    
    public TGAImage(File file) throws Exception
	{
		SwappedDataInputStream bis = new SwappedDataInputStream(new FileInputStream(file));
		try
		{
			decode(bis);	
		}
		finally
		{
			bis.close();
		}
	}
		
    public TGAImage(InputStream is) throws Exception
	{
    	SwappedDataInputStream bis = is instanceof SwappedDataInputStream ? (SwappedDataInputStream)is : new SwappedDataInputStream(is);
    	try
    	{
    		decode(bis);
    	}
    	finally
    	{
    		if (!bis.equals(is))
    			bis.close();
    	}
	}
	
    private void UnpackColor(int[] values, int pixel, TGAColorMap cd)
    {
    	for (int x = 0; x < Width; x++, pixel++)
    	{
    		int val = values[x];
    		if (cd.RMask == 0 && cd.GMask == 0 && cd.BMask == 0 && cd.AMask == 0xFF)
    		{
    			// Special case to deal with 8-bit TGA files that we treat as alpha masks
    			Alpha[pixel] = (byte)val;
    		}
    		else if (cd.Length > x)
    		{ 	
            	if (Red != null)
            		Red[pixel] = cd.RedM[val];  
            	if (Green != null)
            		Green[pixel] = cd.GreenM[val];  
            	if (Blue != null)
            		Blue[pixel] = cd.BlueM[val];  
            	if (Alpha != null)
            		Alpha[pixel] = cd.AlphaM[val];          	
            }
            else
            {
            	if (Red != null)
            		Red[pixel] = (byte)((val >> cd.RShift) & cd.RMask);
            	if (Green != null)
            		Green[pixel] = (byte)((val >> cd.GShift) & cd.GMask);
            	if (Blue != null)
            		Blue[pixel] = (byte)((val >> cd.BShift) & cd.BMask);
            	if (Alpha != null)
            		Alpha[pixel] = (byte)((val >> cd.AShift) & cd.AMask);
            }
    	}
    }

    /**
     * Reads the pixmap as RLE encode stream
     * 
     * @param is the DataInputStream in little endian format
     * @param byp the number of bytes to read per pixel value
     * @param cd the color map structure that contains the information how to interpret the color value of the pixel entry 
     * @param bottomUp indicates if the bitmap is stored in bottemUp format
     * @throws IOException
     */
    private void decodeRle(SwappedDataInputStream is, int byp, TGAColorMap cd, boolean bottomUp) throws IOException
    {
    	int vals[] = new int[Width + 128];
		int x = 0, pixel = bottomUp ? (Height - 1) * Width : 0;
    	
		// RLE compressed
		for (int y = 0; y < Height; y++)
		{			
            while (x < Width)
            {
            	int nb = is.readUnsignedByte(); // num of pixels
            	if ((nb & 0x80) == 0)
            	{ // 0x80=dec 128, bits 10000000
    				for (int i = 0; i <= nb; i++, x++)
    				{
                        for (int k = 0; k < byp; k++)
                        {
                            vals[x] |= is.readUnsignedByte() << (k << 3);
                        }
    				}
    			}
    			else
    			{
    				int val = 0;
                    for (int k = 0; k < byp; k++)
                    {
                        val |= is.readUnsignedByte() << (k << 3);
                    }
    				nb &= 0x7f;
    				for (int j = 0; j <= nb; j++, x++)
    				{
    					vals[x] = val;
    				}
    			}
            }
            UnpackColor(vals, pixel, cd);        
            if (x > Width)
            {
                System.arraycopy(vals, Width, vals, 0, x - Width);
                x -= Width;
            }
            else
            {
                x = 0;
            }
            pixel += bottomUp ? -Width : Width; 
		}
    }
    
    /**
     * Reads the pixmap as unencoded stream
     * 
     * @param is the DataInputStream in little endian format
     * @param byp the number of bytes to read per pixel value
     * @param cd the color map structure that contains the information how to interpret the color value of the pixel entry 
     * @param bottomUp indicates if the bitmap is stored in bottemUp format
     * @throws IOException
     */
    private void decodePlain(SwappedDataInputStream is, int byp, TGAColorMap cd, boolean bottomUp) throws IOException
    {
    	int vals[] = new int[Width];
		int pixel = bottomUp ? (Height - 1) * Width : 0;
		for (int y = 0; y < Height; y++)
		{
			for (int x = 0; x < Width; x++)
			{
				for (int k = 0; k < byp; k++)
				{
					vals[x] |= is.readUnsignedByte() << (k << 3);
				}
			}
            UnpackColor(vals, pixel, cd);        
            pixel += bottomUp ? -Width : Width; 
		}    	
    }

    protected void decode(SwappedDataInputStream is) throws Exception
	{
		TGAHeader header = new TGAHeader(is);

		if (header.ImageSpec.Width > 4096 ||
            header.ImageSpec.Height > 4096)
            throw new IllegalArgumentException("Image too large.");

        if (header.ImageSpec.PixelDepth != 8 &&
            header.ImageSpec.PixelDepth != 16 &&
            header.ImageSpec.PixelDepth != 24 &&
            header.ImageSpec.PixelDepth != 32)
            throw new IllegalArgumentException("Not a supported tga file.");

        if (header.ColorMap.alphaBits > 0)
        {
   			Channels = ImageChannels.Alpha;
        }
        if (header.ColorMap.colorBits > 0)
        {
   			Channels += ImageChannels.Color;
        }
        else if (header.ColorMap.EntrySize > header.ColorMap.alphaBits)
        {
   			Channels += ImageChannels.Gray;        		
        }
               
		Width = header.ImageSpec.Width;
        Height = header.ImageSpec.Height;
		initialize(this);

		if (header.getRleEncoded())
			decodeRle(is, header.ImageSpec.PixelDepth / 8, header.ColorMap, header.ImageSpec.getBottomUp());
		else
	        decodePlain(is, header.ImageSpec.PixelDepth / 8, header.ColorMap, header.ImageSpec.getBottomUp());
	}
	
    @Override
	public int encode(OutputStream os) throws Exception
	{
    	return TGAImage.encode(os, this);
	}
    
    public static int encode(OutputStream os, ManagedImage image) throws Exception
	{
		TGAHeader header = new TGAHeader(image);
		header.writeHeader(os);
		
        int len = 18, n = image.Width * image.Height;

        if ((image.Channels & ImageChannels.Alpha) != 0)
        {
            if ((image.Channels & ImageChannels.Color) != 0)
            {
                // RGBA
                for (int i = 0; i < n; i++)
                {
                	os.write(image.Blue[i]);
                	os.write(image.Green[i]);
                	os.write(image.Red[i]);
                	os.write(image.Alpha[i]);
                }
            }
            else if ((image.Channels & ImageChannels.Gray) != 0)
            {
                for (int i = 0; i < n; i++)
                {
                	os.write(image.Red[i]);
                	os.write(image.Alpha[i]);
                }
            }
            else
            {
                // Alpha only
                for (int i = 0; i < n; i++)
                {
                	os.write(image.Alpha[i]);
                }
            }
            len += n * 4;
        }
        else
        {
            if ((image.Channels & ImageChannels.Color) != 0)
            {
            	// RGB
            	for (int i = 0; i < n; i++)
            	{
            		os.write(image.Blue[i]);
            		os.write(image.Green[i]);
            		os.write(image.Red[i]);
            	}
            }
            else if ((image.Channels & ImageChannels.Gray) != 0)
            {
            	for (int i = 0; i < n; i++)
            	{
            		os.write(image.Red[i]);
            	}            	
            }
            len += n * 3;
        }		
		return len;
	}
}
