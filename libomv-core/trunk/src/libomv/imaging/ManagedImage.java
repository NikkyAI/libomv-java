/**
 * Copyright (c) 2007-2008, openmetaverse.org
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

public class ManagedImage
{
	// [Flags]
	public class ImageChannels
	{
		public static final byte Gray = 1;
		public static final byte Color = 2;
		public static final byte Alpha = 4;
		public static final byte Bump = 8;

		public void setValue(int value)
		{
			_value = (byte) value;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;
	};

	public enum ImageResizeAlgorithm
	{
		NearestNeighbor
	}

	// Image width
	public int Width;

	// Image height
	public int Height;

	// Image channel flags
	public byte Channels;

	// Red channel data
	public byte[] Red;

	// Green channel data
	public byte[] Green;

	// Blue channel data
	public byte[] Blue;

	// Alpha channel data
	public byte[] Alpha;

	// Bump channel data
	public byte[] Bump;

	public ManagedImage()
	{
		
	}
	
	/**
	 * Create a new blank image
	 * 
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param channels
	 *            channel flags
	 */
	public ManagedImage(int width, int height, byte channels)
	{
		Width = width;
		Height = height;
		Channels = channels;
		initialize(this);
	}

	protected static int initialize(ManagedImage image)
	{
		int n = image.Width * image.Height;

		if ((image.Channels & ImageChannels.Gray) != 0)
		{
			image.Red = new byte[n];
		}
		else if ((image.Channels & ImageChannels.Color) != 0)
		{
			image.Red = new byte[n];
			image.Green = new byte[n];
			image.Blue = new byte[n];
		}

		if ((image.Channels & ImageChannels.Alpha) != 0)
			image.Alpha = new byte[n];

		if ((image.Channels & ImageChannels.Bump) != 0)
			image.Bump = new byte[n];
		
		return n;
	}

    /**
     * Convert the channels in the image. Channels are created or destroyed as required.
     *
     * @param channels new channel flags
     */
    public void convertChannels(byte channels)
    {
        if (Channels == channels)
            return;

        int n = Width * Height;
        byte add = (byte)(Channels ^ channels & channels);
        byte del = (byte)(Channels ^ channels & Channels);

        if ((add & ImageChannels.Color) != 0)
        {
            Red = new byte[n];
            Green = new byte[n];
            Blue = new byte[n];
        }
        else if ((del & ImageChannels.Color) != 0)
        {
            Red = null;
            Green = null;
            Blue = null;
        }

        if ((add & ImageChannels.Alpha) != 0)
        {
            Alpha = new byte[n];
            fillArray(Alpha, (byte)255);
        }
        else if ((del & ImageChannels.Alpha) != 0)
            Alpha = null;

        if ((add & ImageChannels.Bump) != 0)
            Bump = new byte[n];
        else if ((del & ImageChannels.Bump) != 0)
            Bump = null;

        Channels = channels;
    }

    /**
     * Resize or stretch the image using nearest neighbor (ugly) resampling
     *
     * @param width widt new width 
     * @param height new height
     */
    @SuppressWarnings("null")
	public void ResizeNearestNeighbor(int width, int height)
    {
        if (width == Width && height == Height)
            return;

        byte[]
            red = null, 
            green = null, 
            blue = null, 
            alpha = null, 
            bump = null;
        int n = width * height;
        int di = 0, si;

        if (Red != null) red = new byte[n];
        if (Green != null) green = new byte[n];
        if (Blue != null) blue = new byte[n];
        if (Alpha != null) alpha = new byte[n];
        if (Bump != null) bump = new byte[n];
        
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                si = (y * Height / height) * Width + (x * Width / width);
                if (Red != null) red[di] = Red[si];
                if (Green != null) green[di] = Green[si];
                if (Blue != null) blue[di] = Blue[si];
                if (Alpha != null) alpha[di] = Alpha[si];
                if (Bump != null) bump[di] = Bump[si];
                di++;
            }
        }

        Width = width;
        Height = height;
        Red = red;
        Green = green;
        Blue = blue;
        Alpha = alpha;
        Bump = bump;
    }

    @Override
	public ManagedImage clone()
	{
		return null;
	}

    private static void fillArray(byte[] array, byte value)
    {
        if (array != null)
        {
            for (int i = 0; i < array.length; i++)
                array[i] = value;
        }
    }

    public void clear()
    {
        fillArray(Red, (byte)0);
        fillArray(Green, (byte)0);
        fillArray(Blue, (byte)0);
        fillArray(Alpha, (byte)0);
        fillArray(Bump, (byte)0);
    }
}
