package libomv.model.asset;

import libomv.utils.Helpers;

/** The different types of grid assets */
public enum AssetType {
	/** Unknown asset type */
	Unknown(-1),
	/** Texture asset, stores in JPEG2000 J2C stream format */
	Texture(0),
	/** Sound asset */
	Sound(1),
	/** Calling card for another avatar */
	CallingCard(2),
	/** Link to a location in world */
	Landmark(3),
	// [Obsolete] Legacy script asset, you should never see one of these
	Script(4),
	/** Collection of textures and parameters that can be worn by an avatar */
	Clothing(5),
	/** Primitive that can contain textures, sounds, scripts and more */
	Object(6),
	/** Notecard asset */
	Notecard(7),
	/** Holds a collection of inventory items. "Category" in the Linden viewer */
	Folder(8),
	/** Linden scripting language script */
	LSLText(10),
	/** LSO bytecode for a script */
	LSLBytecode(11),
	/** Uncompressed TGA texture */
	TextureTGA(12),
	/** Collection of textures and shape parameters that can be worn */
	Bodypart(13),
	/** Uncompressed sound */
	SoundWAV(17),
	/** Uncompressed TGA non-square image, not to be used as a texture */
	ImageTGA(18),
	/** Compressed JPEG non-square image, not to be used as a texture */
	ImageJPEG(19),
	/** Animation */
	Animation(20),
	/** Sequence of animations, sounds, chat, and pauses */
	Gesture(21),
	/** Simstate file */
	Simstate(22),
	/** Asset is a link to another inventory item */
	Link(24),
	/** Asset is a link to another inventory folder */
	LinkFolder(25),
	/** Marketplace Folder. Same as an Category but different display methods */
	MarketplaceFolder(26),
	/** Linden mesh format */
	Mesh(49);

	private static final String[] _AssetTypeNames = new String[] { "texture", // 0
			"sound", // 1
			"callcard", // 2
			"landmark", // 3
			"script", // 4
			"clothing", // 5
			"object", // 6
			"notecard", // 7
			"category", // 8
			Helpers.EmptyString, // 9
			"lsltext", // 10
			"lslbyte", // 11
			"txtr_tga", // 12
			"bodypart", // 13
			Helpers.EmptyString, // 14
			Helpers.EmptyString, // 15
			Helpers.EmptyString, // 16
			"snd_wav", // 17
			"img_tga", // 18
			"jpeg", // 19
			"animatn", // 20
			"gesture", // 21
			"simstate", // 22
			Helpers.EmptyString, // 23
			"link", // 24
			"link_f", // 25
			Helpers.EmptyString, // 26
			Helpers.EmptyString, // 27
			Helpers.EmptyString, // 28
			Helpers.EmptyString, // 29
			Helpers.EmptyString, // 30
			Helpers.EmptyString, // 31
			Helpers.EmptyString, // 32
			Helpers.EmptyString, // 33
			Helpers.EmptyString, // 34
			Helpers.EmptyString, // 35
			Helpers.EmptyString, // 36
			Helpers.EmptyString, // 37
			Helpers.EmptyString, // 38
			Helpers.EmptyString, // 39
			Helpers.EmptyString, // 40
			Helpers.EmptyString, // 41
			Helpers.EmptyString, // 42
			Helpers.EmptyString, // 43
			Helpers.EmptyString, // 44
			Helpers.EmptyString, // 45
			Helpers.EmptyString, // 46
			Helpers.EmptyString, // 47
			Helpers.EmptyString, // 48
			"mesh", // 49
	};

	/**
	 * Translate a string name of an AssetType into the proper Type
	 *
	 * @param type
	 *            A string containing the AssetType name
	 * @return The AssetType which matches the string name, or AssetType.Unknown if
	 *         no match was found
	 */
	public static AssetType setValue(String value) {
		if (value != null) {
			try {
				return setValue(Integer.parseInt(value, 10));
			} catch (NumberFormatException ex) {
			}

			int i = 0;
			for (String name : _AssetTypeNames) {
				i++;
				if (name.compareToIgnoreCase(value) == 0) {
					return values()[i];
				}
			}
		}
		return Unknown;
	}

	public static AssetType setValue(int value) {
		for (AssetType e : values()) {
			if (e._value == value)
				return e;
		}
		return Unknown;
	}

	public byte getValue() {
		return _value;
	}

	@Override
	public String toString() {
		int i = ordinal() - 1;
		if (i >= 0 && ordinal() < _AssetTypeNames.length)
			return _AssetTypeNames[i];
		return "unknown";
	}

	private final byte _value;

	private AssetType(int value) {
		this._value = (byte) value;
	}
}