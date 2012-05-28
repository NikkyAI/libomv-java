package libomv.imaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.input.SwappedDataInputStream;

public class TGAImage extends ManagedImage
{
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
		decode(new SwappedDataInputStream(is));	
	}
	
	protected void decode(SwappedDataInputStream bis) throws IOException
	{
		bis.skip(2);
		int type = bis.readUnsignedByte();
		bis.skip(9);
		Width = bis.readUnsignedShort(); // 00,04=1024
		Height = bis.readUnsignedShort(); // 40,02=576
		int pixelsize = bis.readUnsignedByte();
		int descriptor = bis.readUnsignedByte();

		if (pixelsize == 8)
		{
			Channels = ImageChannels.Gray;
		}
		else if (pixelsize == 16)
		{
			Channels = ImageChannels.Gray + ImageChannels.Alpha;			
		}
		else if (pixelsize == 24)
		{
			Channels = ImageChannels.Color;			
		}
		else if (pixelsize == 32)
		{
			Channels = ImageChannels.Color + ImageChannels.Alpha;			
		}
		int n = initialize(this);

		if (type == 0x02 && pixelsize == 0x20)
		{ // uncompressed BGRA
			for (int i = 0; i < n; i++)
			{
				Blue[i] = bis.readByte();
				Green[i] = bis.readByte();
				Red[i] = bis.readByte();
				Alpha[i] = bis.readByte();
			}
		}
		else if (type == 0x02 && pixelsize == 0x18)
		{ // uncompressed BGR
			for (int i = 0; i < n; i++)
			{
				Blue[i] = bis.readByte();
				Green[i] = bis.readByte();
				Red[i] = bis.readByte();
			}
		}
		else
		{
			// RLE compressed
			for (int i = 0; i <= n; i++)
			{
				int nb = bis.readUnsignedByte(); // num of pixels
				if ((nb & 0x80) == 0)
				{ // 0x80=dec 128, bits 10000000
					for (int j = 0; j <= nb; j++, i++)
					{
						Blue[i] = bis.readByte();
						Green[i] = bis.readByte();
						Red[i] = bis.readByte();
					}
				}
				else
				{
					nb &= 0x7f;
					byte b = bis.readByte();
					byte g = bis.readByte();
					byte r = bis.readByte();
					for (int j = 0; j <= nb; j++, i++)
					{
						Blue[i] = b;
						Green[i] = g;
						Red[i] = r;
					}
				}
			}
		}
	}
	
	public static int encode(OutputStream os, ManagedImage image) throws IOException
	{
        os.write(0); // idlength
        os.write(0); // colormaptype = 0: no colormap
        os.write(2); // image type = 2: uncompressed RGB
        os.write(0); // color map spec is five zeroes for no color map
        os.write(0); // color map spec is five zeroes for no color map
        os.write(0); // color map spec is five zeroes for no color map
        os.write(0); // color map spec is five zeroes for no color map
        os.write(0); // color map spec is five zeroes for no color map
        os.write(0); // x origin = two bytes
        os.write(0); // x origin = two bytes
        os.write(0); // y origin = two bytes
        os.write(0); // y origin = two bytes
        os.write(image.Width & 0xFF); // width - low byte
        os.write(image.Width >> 8); // width - hi byte
        os.write(image.Height & 0xFF); // height - low byte
        os.write(image.Height >> 8); // height - hi byte
        os.write((image.Channels & ImageChannels.Alpha) == 0 ? 24 : 32); // 24/32 bits per pixel
        os.write((image.Channels & ImageChannels.Alpha) == 0 ? 32 : 40); // image descriptor byte

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
                	os.write(image.Red[i]);
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
                	os.write(image.Alpha[i]);
                	os.write(image.Alpha[i]);
                	os.write(Byte.MAX_VALUE);
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
            		os.write(image.Red[i]);
            		os.write(image.Red[i]);
            	}            	
            }
            len += n * 3;
        }		
		return len;
	}
}
