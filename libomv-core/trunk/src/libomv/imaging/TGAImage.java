/**
 * Copyright (c) 2009-2012, Frederick Martian
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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.input.SwappedDataInputStream;

public class TGAImage extends ManagedImage
{
	public class TGAColorMap
    {
        public int FirstEntryIndex;
        public int Length;
        public byte EntrySize;
        int alphaBits;
        int colorBits;
        
        public int RMask, GMask, BMask, AMask;
        public int RShift, GShift, BShift, AShift;

        private byte[] RedM;
        private byte[] GreenM;
        private byte[] BlueM;
        private byte[] AlphaM;

        public TGAColorMap(ManagedImage image)
        {
        	
        }
        	
        public TGAColorMap(SwappedDataInputStream is) throws EOFException, IOException
        {
            FirstEntryIndex = is.readUnsignedShort();
            Length = is.readUnsignedShort();
            EntrySize = is.readByte();
        }
        
        public void writeHeader(OutputStream os) throws IOException
        {
        	os.write(FirstEntryIndex & 0xFF);
        	os.write((FirstEntryIndex >> 8) & 0xFF);
        	os.write(Length & 0xFF);
        	os.write((Length >> 8) & 0xFF);
        	os.write(EntrySize);
        }
        
        public void readMap(SwappedDataInputStream is, boolean gray) throws IOException
        {
        	if (EntrySize == 8 || EntrySize == 32)
        	{
        		alphaBits = 8;
        	}
        	else if (EntrySize == 16)
        	{
        		alphaBits = 1;
        	}

    		colorBits = EntrySize - alphaBits;
        	if (!gray)
        	{
        		colorBits /= 3;

                BMask = ((2 ^ colorBits) - 1);
                GShift = BShift + colorBits;
                GMask = ((2 ^ colorBits) - 1);
                RShift = GShift + colorBits;

                if (GMask > 0)
                	GreenM = new byte[Length];
                if (BMask > 0)
                	BlueM = new byte[Length];
        	}

            RMask = ((2 ^ colorBits) - 1);
            AShift = RShift + colorBits;
            AMask = ((2 ^ alphaBits) - 1);

            if (RMask > 0)
            	RedM = new byte[Length];
            if (AMask > 0)
            	AlphaM = new byte[Length];
        	
        	
        	for (int i = 0; i < Length; i++)
        	{
                long x = 0;
                for (int k = 0; k < EntrySize; ++k)
                {
                    x |= is.readUnsignedByte() << (k << 3);
                }

        		if (RedM != null)
        			RedM[i] = (byte)((x >> RShift) & RMask);
        		if (GreenM != null)
        			GreenM[i] = (byte)((x >> GShift) & GMask);
        		if (BlueM != null)
        			BlueM[i] = (byte)((x >> BShift) & BMask);
        		if (AlphaM != null)
        			AlphaM[i] = (byte)((x >> AShift) & AMask);
            }
        }
        
        public void setBits(TGAImageSpec spec, boolean gray)
        {
            // Treat 8 bit images as alpha channel
            if (alphaBits == 0 && (spec.PixelDepth == 8 || spec.PixelDepth == 32))
            {
            	alphaBits = 8;
            }

            Length = 0;
            EntrySize = spec.PixelDepth;
    		colorBits = EntrySize - alphaBits;
        	if (!gray)
        	{
        		colorBits /= 3;
            
        		BMask = ((2 ^ Math.round(colorBits)) - 1);
        		GShift = BShift + Math.round(colorBits);
        		GMask = ((2 ^ (int)Math.ceil(colorBits)) - 1);
        		RShift = GShift + (int)Math.ceil(colorBits);
        	}
            RMask = ((2 ^ (int)Math.floor(colorBits)) - 1);
            AShift = RShift + (int)Math.floor(colorBits);
            AMask = ((2 ^ alphaBits) - 1);
        }
    }

	public class TGAImageSpec
    {
        public int XOrigin;
        public int YOrigin;
        public int Width;
        public int Height;
        public byte PixelDepth;
        public byte Descriptor;

    	public TGAImageSpec(ManagedImage image)
        {
    		Width = image.Width;
    		Height = image.Height;
    		
    		if (image.Channels == ImageChannels.Gray)
    		{
        		PixelDepth = 8;
    		}
    		else if (image.Channels == ImageChannels.Color)
    		{
        		PixelDepth = 24;
    		}
    		if (image.Channels == ImageChannels.Alpha)
    		{
        		Descriptor = PixelDepth > 0 ? (byte)8 : (byte)0; 
        		PixelDepth += 8;
    		}
        }
    	
    	public TGAImageSpec(SwappedDataInputStream is) throws EOFException, IOException
        {
            XOrigin = is.readUnsignedShort();
            YOrigin = is.readUnsignedShort();
            Width = is.readUnsignedShort();
            Height = is.readUnsignedShort();
            PixelDepth = is.readByte();
            Descriptor = is.readByte();
        }

        public void writeHeader(OutputStream os) throws IOException
        {
        	os.write(XOrigin & 0xFF);
        	os.write((XOrigin >> 8) & 0xFF);
        	os.write(YOrigin & 0xFF);
        	os.write((YOrigin >> 8) & 0xFF);
        	os.write(Width & 0xFF);
        	os.write((Width >> 8) & 0xFF);
        	os.write(Height & 0xFF);
        	os.write((Height >> 8) & 0xFF);
        	os.write(PixelDepth);
        	os.write(Descriptor);
        }

        public byte getAlphaBits()
        {
            return (byte)(Descriptor & 0xF);
        }

        public void setAlphaBits(int value)
        {
            Descriptor = (byte)((Descriptor & ~0xF) | (value & 0xF));
        }

        public boolean getBottomUp()
        {
            return (Descriptor & 0x20) == 0x20;
        }
        
        public void setBottomUp(boolean value)
        {
            Descriptor = (byte)((Descriptor & ~0x20) | (value ? 0x20 : 0));
        }
    }

    public class TGAHeader
    {
        public byte IdLength;
        public byte ColorMapType;
        public byte ImageType;

        public TGAColorMap ColorMap;
        public TGAImageSpec ImageSpec;

        public TGAHeader(ManagedImage image)
        {
            ColorMap = new TGAColorMap(image);
            ImageSpec = new TGAImageSpec(image);      	
        }
        
        public TGAHeader(SwappedDataInputStream is) throws EOFException, IOException
        {
            IdLength = is.readByte();
            ColorMapType = is.readByte();
            ImageType = is.readByte();
            ColorMap = new TGAColorMap(is);
            ImageSpec = new TGAImageSpec(is);
            ColorMap.alphaBits = ImageSpec.getAlphaBits();

    		is.skipBytes(IdLength); // Skip any ID Length data

    		if (ColorMapType != 0)
            {
            	if (ColorMap.EntrySize != 8 &&
            		ColorMap.EntrySize != 16 &&
            		ColorMap.EntrySize != 24 &&
            		ColorMap.EntrySize != 24)
                    throw new IllegalArgumentException("Not a supported tga file.");        		

            	ColorMap.readMap(is, ImageType % 8 == 3);
            }
            else
            {
            	ColorMap.setBits(ImageSpec, ImageType % 8 == 3);
            }
        }

        public void writeHeader(OutputStream os) throws IOException
        {
        	os.write(IdLength);
        	os.write(ColorMapType);
        	os.write(ImageType);
        	ColorMap.writeHeader(os);
        	ImageSpec.writeHeader(os);
        }

        public boolean getRleEncoded()
        {
             return ImageType >= 8;
        }
    }

    public TGAImage(File file) throws IOException
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
		
    public TGAImage(InputStream is) throws IOException
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

    protected void decode(SwappedDataInputStream is) throws IOException
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
		initialize();

		if (header.getRleEncoded())
			decodeRle(is, header.ImageSpec.PixelDepth / 8, header.ColorMap, header.ImageSpec.getBottomUp());
		else
	        decodePlain(is, header.ImageSpec.PixelDepth / 8, header.ColorMap, header.ImageSpec.getBottomUp());
	}
	
	public int encode(OutputStream os, ManagedImage image) throws IOException
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
