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
        	_value = (byte)value;
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

    /** 
     * Create a new blank image
     * 
     * @param width width
     * @param height height
     * @param channels channel flags
     */
    public ManagedImage(int width, int height, byte channels)
    {
        Width = width;
        Height = height;
        Channels = channels;

        int n = width * height;

        if ((channels & ImageChannels.Gray) != 0)
        {
            Red = new byte[n];
        }
        else if ((channels & ImageChannels.Color) != 0)
        {
            Red = new byte[n];
            Green = new byte[n];
            Blue = new byte[n];
        }

        if ((channels & ImageChannels.Alpha) != 0)
            Alpha = new byte[n];

        if ((channels & ImageChannels.Bump) != 0)
            Bump = new byte[n];
    }

}
