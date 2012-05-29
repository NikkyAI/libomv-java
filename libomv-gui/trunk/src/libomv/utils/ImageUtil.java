package libomv.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import libomv.imaging.ManagedImage;

public class ImageUtil
{
	public static Image convert(ManagedImage textureImage)
	{
		int imageType;
		if ((textureImage.Channels & ManagedImage.ImageChannels.Color) != 0)
		{
			if ((textureImage.Channels & ManagedImage.ImageChannels.Alpha) != 0)
			{
				imageType = BufferedImage.TYPE_INT_ARGB;
			}
			else
			{
				imageType = BufferedImage.TYPE_INT_RGB;
			}
		}
		else
		{
			imageType = BufferedImage.TYPE_BYTE_GRAY;
		}		
		BufferedImage image = new BufferedImage(textureImage.Width, textureImage.Height, imageType);
		
		WritableRaster raster = image.getWritableTile(0, 0);
        SampleModel sampleModel = image.getSampleModel();
        
		if ((textureImage.Channels & ManagedImage.ImageChannels.Color) != 0)
        {
        	if (sampleModel != null && sampleModel.getDataType() == DataBuffer.TYPE_INT)
            {
            	int bdata[] = ((DataBufferInt)raster.getDataBuffer()).getData();

            	int len = textureImage.Height * textureImage.Width;
    			if ((textureImage.Channels & ManagedImage.ImageChannels.Alpha) != 0)
    			{
    				for (int i = 0; i < len; i++)
    				{
    					bdata[i] = (textureImage.Blue[i]) | (textureImage.Green[i] << 8) | (textureImage.Red[i] << 16) | (textureImage.Alpha[i] << 24);
    				}
    			}
            }
            else
            {
            	throw new IllegalArgumentException("Unsupported sample model for conversion of data");
            }
        }
		else if ((textureImage.Channels & ManagedImage.ImageChannels.Gray) != 0)
        {
        	if (sampleModel != null && sampleModel.getDataType() == DataBuffer.TYPE_BYTE)
            {
            	byte bdata[] = ((DataBufferByte)raster.getDataBuffer()).getData();

            	int len = textureImage.Height * textureImage.Width;
				for (int i = 0; i < len; i++)
				{
					bdata[i] = textureImage.Red[i];
				}
            }
            else
            {
            	throw new IllegalArgumentException("Unsupported sample model for conversion of data");
            }
		}
		else if ((textureImage.Channels & ManagedImage.ImageChannels.Alpha) != 0)
        {
        	if (sampleModel != null && sampleModel.getDataType() == DataBuffer.TYPE_BYTE)
            {
            	byte bdata[] = ((DataBufferByte)raster.getDataBuffer()).getData();

            	int len = textureImage.Height * textureImage.Width;
				for (int i = 0; i < len; i++)
				{
					bdata[i] = textureImage.Alpha[i];
				}
            }
            else
            {
            	throw new IllegalArgumentException("Unsupported sample model for conversion of data");
            }
		}
		return image;
	}
}
