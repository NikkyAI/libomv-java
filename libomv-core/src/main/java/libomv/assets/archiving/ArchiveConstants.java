package libomv.assets.archiving;

import java.util.HashMap;
import java.util.Map;

import libomv.model.Asset.AssetType;
import libomv.utils.Helpers;

public class ArchiveConstants {
	/// The location of the archive control file
	public static final String CONTROL_FILE_PATH = "archive.xml";

	/// Path for the assets held in an archive
	public static final String ASSETS_PATH = "assets/";

	/// Path for the prims file
	public static final String OBJECTS_PATH = "objects/";

	/// Path for terrains. Technically these may be assets, but I think it's quite
	/// nice to split them out.
	public static final String TERRAINS_PATH = "terrains/";

	/// Path for region settings.
	public static final String SETTINGS_PATH = "settings/";

	/// Path for region settings.
	public static final String LANDDATA_PATH = "landdata/";

	/// The character the separates the uuid from extension information in an
	/// archived asset filename
	public static final char ASSET_EXTENSION_SEPARATOR = '_';

	/// Extensions used for asset types in the archive
	private static final Map<AssetType, String> ASSET_TYPE_TO_EXTENSION = new HashMap<AssetType, String>();
	private static final Map<String, AssetType> EXTENSION_TO_ASSET_TYPE = new HashMap<String, AssetType>();

	static {
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Animation, "animation.bvh");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Bodypart, "bodypart.txt");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.CallingCard, "callingcard.txt");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Clothing, "clothing.txt");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Folder, "folder.txt"); // Not sure if we'll ever see this
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Gesture, "gesture.txt");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.ImageJPEG, "image.jpg");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.ImageTGA, "image.tga");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Landmark, "landmark.txt");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.LSLBytecode, "bytecode.lso");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.LSLText, "script.lsl");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Notecard, "notecard.txt");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Object, "object.xml");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Simstate, "simstate.bin"); // Not sure if we'll ever see this
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Sound, "sound.ogg");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.SoundWAV, "sound.wav");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.Texture, "texture.jp2");
		ASSET_TYPE_TO_EXTENSION.put(AssetType.TextureTGA, "texture.tga");

		EXTENSION_TO_ASSET_TYPE.put("animation.bvh", AssetType.Animation);
		EXTENSION_TO_ASSET_TYPE.put("bodypart.txt", AssetType.Bodypart);
		EXTENSION_TO_ASSET_TYPE.put("callingcard.txt", AssetType.CallingCard);
		EXTENSION_TO_ASSET_TYPE.put("clothing.txt", AssetType.Clothing);
		EXTENSION_TO_ASSET_TYPE.put("folder.txt", AssetType.Folder);
		EXTENSION_TO_ASSET_TYPE.put("gesture.txt", AssetType.Gesture);
		EXTENSION_TO_ASSET_TYPE.put("image.jpg", AssetType.ImageJPEG);
		EXTENSION_TO_ASSET_TYPE.put("image.tga", AssetType.ImageTGA);
		EXTENSION_TO_ASSET_TYPE.put("landmark.txt", AssetType.Landmark);
		EXTENSION_TO_ASSET_TYPE.put("bytecode.lso", AssetType.LSLBytecode);
		EXTENSION_TO_ASSET_TYPE.put("script.lsl", AssetType.LSLText);
		EXTENSION_TO_ASSET_TYPE.put("notecard.txt", AssetType.Notecard);
		EXTENSION_TO_ASSET_TYPE.put("object.xml", AssetType.Object);
		EXTENSION_TO_ASSET_TYPE.put("simstate.bin", AssetType.Simstate);
		EXTENSION_TO_ASSET_TYPE.put("sound.ogg", AssetType.Sound);
		EXTENSION_TO_ASSET_TYPE.put("sound.wav", AssetType.SoundWAV);
		EXTENSION_TO_ASSET_TYPE.put("texture.jp2", AssetType.Texture);
		EXTENSION_TO_ASSET_TYPE.put("texture.tga", AssetType.TextureTGA);
	}

	public static String getExtensionForType(AssetType type) {
		String extension = ASSET_TYPE_TO_EXTENSION.get(type);
		if (extension != null)
			return ASSET_EXTENSION_SEPARATOR + extension;
		return Helpers.EmptyString;
	}

	public static AssetType getAssetTypeForExtenstion(String extension) {
		return EXTENSION_TO_ASSET_TYPE.get(extension);
	}
}
