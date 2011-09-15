package libomv.assets;

import libomv.imaging.ManagedImage;
import libomv.imaging.J2KWrap;
import libomv.imaging.J2KWrap.J2KLayerInfo;
import libomv.types.UUID;

public class AssetTexture extends AssetItem
{
    // Override the base classes AssetType
	@Override
    public AssetType getAssetType()
    {
        return AssetType.Texture;
    }

    // A {@link Image} object containing image data
    public ManagedImage Image;

    public J2KLayerInfo[] LayerInfo;

    public int Components;

    // Initializes a new instance of an AssetTexture object
    public AssetTexture()
    {
    }

    /** 
     * Initializes a new instance of an AssetTexture object
     * 
     * @param assetID A unique <see cref="UUID"/> specific to this asset
     * @param assetData A byte array containing the raw asset data
     */
    public AssetTexture(UUID assetID, byte[] assetData)
    {
        super(assetID, assetData);
    }

    /**
     * Initializes a new instance of an AssetTexture object
     * 
     * @param image A {@link ManagedImage} object containing texture data
     */
    public AssetTexture(ManagedImage image)
    {
        Image = image;
        Components = 0;
        if ((Image.Channels & ManagedImage.ImageChannels.Color) != 0)
            Components += 3;
        if ((Image.Channels & ManagedImage.ImageChannels.Gray) != 0)
            ++Components;
        if ((Image.Channels & ManagedImage.ImageChannels.Bump) != 0)
            ++Components;
        if ((Image.Channels & ManagedImage.ImageChannels.Alpha) != 0)
            ++Components;
    }

    /**
     * Populates the {@link AssetData} byte array with a JPEG2000
     * encoded image created from the data in {@link Image}
     */
    @Override
    public void Encode()
    {
        AssetData = J2KWrap.Encode(Image);
    }

    /**
     * Decodes the JPEG2000 data in <code>AssetData</code> to the
     * {@link ManagedImage} object {@link Image}
     * 
     * @return True if the decoding was successful, otherwise false
     */
    @Override
    public boolean Decode()
    {
        Components = 0;

        Image = J2KWrap.DecodeToImage(AssetData);
        if (Image != null)
        {
            if ((Image.Channels & ManagedImage.ImageChannels.Color) != 0)
                Components += 3;
            if ((Image.Channels & ManagedImage.ImageChannels.Gray) != 0)
                ++Components;
            if ((Image.Channels & ManagedImage.ImageChannels.Bump) != 0)
                ++Components;
            if ((Image.Channels & ManagedImage.ImageChannels.Alpha) != 0)
                ++Components;

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Decodes the begin and end byte positions for each quality layer in
     * the image
     * 
     * @return 
     */
    public boolean DecodeLayerBoundaries()
    {
        Components =  J2KWrap.DecodeLayerBoundaries(AssetData, LayerInfo);
        return (Components > 0);
    }

}
