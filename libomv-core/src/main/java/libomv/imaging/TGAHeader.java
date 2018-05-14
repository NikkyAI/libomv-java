/**
 * Copyright (c) 2010-2017, Frederick Martian
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
import java.io.IOException;
import java.io.OutputStream;

import libomv.imaging.ManagedImage.ImageChannels;

import org.apache.commons.io.input.SwappedDataInputStream;

public class TGAHeader
{
	protected class TGAColorMap
    {
        public int offset;
        public int length;
        public byte bits;
        int alphaBits;
        int colorBits;
        
        public int RMask, GMask, BMask, AMask;
        public int RShift, GShift, BShift, AShift;

        byte[] RedM;
        byte[] GreenM;
        byte[] BlueM;
        byte[] AlphaM;

        public TGAColorMap()
        {
        }

        public TGAColorMap(SwappedDataInputStream is) throws EOFException, IOException
        {
            offset = is.readUnsignedShort();
            length = is.readUnsignedShort();
            bits = is.readByte();
        }
        
        public void writeHeader(OutputStream os) throws IOException
        {
        	os.write((byte)(offset & 0xFF));
        	os.write((byte)((offset >> 8) & 0xFF));
        	os.write((byte)(length & 0xFF));
        	os.write((byte)((length >> 8) & 0xFF));
        	os.write((byte)bits);
        }
        
        public void readMap(SwappedDataInputStream is, boolean gray) throws IOException
        {
        	if (bits == 8 || bits == 32)
        	{
        		alphaBits = 8;
        	}
        	else if (bits == 16)
        	{
        		alphaBits = 1;
        	}

    		colorBits = bits - alphaBits;
        	if (!gray)
        	{
        		colorBits /= 3;

                BMask = ((2 ^ colorBits) - 1);
                GShift = BShift + colorBits;
                GMask = ((2 ^ colorBits) - 1);
                RShift = GShift + colorBits;

                if (GMask > 0)
                	GreenM = new byte[length];
                if (BMask > 0)
                	BlueM = new byte[length];
        	}

            RMask = ((2 ^ colorBits) - 1);
            AShift = RShift + colorBits;
            AMask = ((2 ^ alphaBits) - 1);

            if (RMask > 0)
            	RedM = new byte[length];
            if (AMask > 0)
            	AlphaM = new byte[length];
        	
        	
        	for (int i = 0; i < length; i++)
        	{
                long x = 0;
                for (int k = 0; k < bits; k += 8)
                {
                    x |= is.readUnsignedByte() << k;
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

            length = 0;
            bits = spec.PixelDepth;
    		colorBits = bits - alphaBits;
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

	protected class TGAImageSpec
    {
        public int XOrigin;
        public int YOrigin;
        public int Width;
        public int Height;
        public byte PixelDepth;
        public byte Descriptor;

    	public TGAImageSpec(ManagedImage image)
        {
    		Width = image.getWidth();
    		Height = image.getHeight();
    		
    		if (image.getChannels() == ImageChannels.Gray)
    		{
        		PixelDepth = 8;
    		}
    		else if (image.getChannels() == ImageChannels.Color)
    		{
        		PixelDepth = 24;
    		}
    		if (image.getChannels() == ImageChannels.Alpha)
    		{
        		Descriptor = PixelDepth > 0 ? (byte)0x28 : (byte)0x20; 
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

        public void write(OutputStream os) throws IOException
        {
        	os.write((byte)(XOrigin & 0xFF));
        	os.write((byte)((XOrigin >> 8) & 0xFF));
        	os.write((byte)(YOrigin & 0xFF));
        	os.write((byte)((YOrigin >> 8) & 0xFF));
        	os.write((byte)(Width & 0xFF));
        	os.write((byte)((Width >> 8) & 0xFF));
        	os.write((byte)(Height & 0xFF));
        	os.write((byte)((Height >> 8) & 0xFF));
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
            Descriptor = (byte)((Descriptor & ~0x20) | (value ? 0x0 : 0x20));
        }
    }

    public byte IdLength;
    public byte ColorMapType;
    public byte ImageType;

    public TGAColorMap ColorMap;
    public TGAImageSpec ImageSpec;

    public TGAHeader(ManagedImage image)
    {
        ColorMap = new TGAColorMap();
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
        	if (ColorMap.bits != 8 &&
        		ColorMap.bits != 16 &&
        		ColorMap.bits != 24 &&
        		ColorMap.bits != 24)
                throw new IllegalArgumentException("Not a supported tga file.");        		

        	ColorMap.readMap(is, ImageType % 8 == 3);
        }
        else
        {
        	ColorMap.setBits(ImageSpec, ImageType % 8 == 3);
        }
    }

    public void write(OutputStream os) throws IOException
    {
    	os.write(IdLength);
    	os.write(ColorMapType);
    	os.write(ImageType);
    	ColorMap.writeHeader(os);
    	ImageSpec.write(os);
    }

    public boolean getRleEncoded()
    {
         return ImageType >= 8;
    }
}
