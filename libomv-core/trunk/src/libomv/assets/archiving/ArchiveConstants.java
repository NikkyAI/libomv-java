package libomv.assets.archiving;

import java.util.HashMap;
import java.util.Map;

import libomv.assets.AssetItem.AssetType;

public class ArchiveConstants
{
    /// The location of the archive control file
    public static final String CONTROL_FILE_PATH = "archive.xml";

    /// Path for the assets held in an archive
    public static final String ASSETS_PATH = "assets/";

    /// Path for the prims file
    public static final String OBJECTS_PATH = "objects/";

    /// Path for terrains.  Technically these may be assets, but I think it's quite nice to split them out.
    public static final String TERRAINS_PATH = "terrains/";

    /// Path for region settings.
    public static final String SETTINGS_PATH = "settings/";

    ///   Path for region settings.
    public static final String LANDDATA_PATH = "landdata/";

    /// The character the separates the uuid from extension information in an archived asset filename
    public static final String ASSET_EXTENSION_SEPARATOR = "_";

    /// Extensions used for asset types in the archive
    public static final Map<AssetType, String> ASSET_TYPE_TO_EXTENSION = new HashMap<AssetType, String>();
    public static final Map<String, AssetType> EXTENSION_TO_ASSET_TYPE = new HashMap<String, AssetType>();

    static
    {
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Animation, ASSET_EXTENSION_SEPARATOR + "animation.bvh");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Bodypart, ASSET_EXTENSION_SEPARATOR + "bodypart.txt");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.CallingCard, ASSET_EXTENSION_SEPARATOR + "callingcard.txt");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Clothing, ASSET_EXTENSION_SEPARATOR + "clothing.txt");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Folder, ASSET_EXTENSION_SEPARATOR + "folder.txt");   // Not sure if we'll ever see this
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Gesture, ASSET_EXTENSION_SEPARATOR + "gesture.txt");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.ImageJPEG, ASSET_EXTENSION_SEPARATOR + "image.jpg");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.ImageTGA, ASSET_EXTENSION_SEPARATOR + "image.tga");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Landmark, ASSET_EXTENSION_SEPARATOR + "landmark.txt");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.LSLBytecode, ASSET_EXTENSION_SEPARATOR + "bytecode.lso");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.LSLText, ASSET_EXTENSION_SEPARATOR + "script.lsl");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Notecard, ASSET_EXTENSION_SEPARATOR + "notecard.txt");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Object, ASSET_EXTENSION_SEPARATOR + "object.xml");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Simstate, ASSET_EXTENSION_SEPARATOR + "simstate.bin");   // Not sure if we'll ever see this
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Sound, ASSET_EXTENSION_SEPARATOR + "sound.ogg");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.SoundWAV, ASSET_EXTENSION_SEPARATOR + "sound.wav");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.Texture, ASSET_EXTENSION_SEPARATOR + "texture.jp2");
        ASSET_TYPE_TO_EXTENSION.put(AssetType.TextureTGA, ASSET_EXTENSION_SEPARATOR + "texture.tga");

        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "animation.bvh", AssetType.Animation);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "bodypart.txt", AssetType.Bodypart);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "callingcard.txt", AssetType.CallingCard);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "clothing.txt", AssetType.Clothing);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "folder.txt", AssetType.Folder);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "gesture.txt", AssetType.Gesture);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "image.jpg", AssetType.ImageJPEG);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "image.tga", AssetType.ImageTGA);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "landmark.txt", AssetType.Landmark);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "bytecode.lso", AssetType.LSLBytecode);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "script.lsl", AssetType.LSLText);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "notecard.txt", AssetType.Notecard);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "object.xml", AssetType.Object);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "simstate.bin", AssetType.Simstate);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "sound.ogg", AssetType.Sound);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "sound.wav", AssetType.SoundWAV);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "texture.jp2", AssetType.Texture);
        EXTENSION_TO_ASSET_TYPE.put(ASSET_EXTENSION_SEPARATOR + "texture.tga", AssetType.TextureTGA);
    }
}
