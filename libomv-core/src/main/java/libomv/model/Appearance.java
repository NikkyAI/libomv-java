package libomv.model;

import libomv.assets.AssetWearable.WearableType;
import libomv.types.UUID;

public interface Appearance {

	// Total number of wearables allowed for each avatar
	public static final int WEARABLE_COUNT_MAX = 60;
	// Total number of wearables for each avatar
	public static final int WEARABLE_COUNT = 16;
	// Total number of baked textures on each avatar
	public static final int BAKED_TEXTURE_COUNT = 6;
	// Total number of wearables per bake layer
	public static final int WEARABLES_PER_LAYER = 9;
	// Map of what wearables are included in each bake
	public static final WearableType[][] WEARABLE_BAKE_MAP = new WearableType[][] {
			new WearableType[] { WearableType.Shape, WearableType.Skin, WearableType.Tattoo, WearableType.Hair,
					WearableType.Alpha, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid },
			new WearableType[] { WearableType.Shape, WearableType.Skin, WearableType.Tattoo, WearableType.Shirt,
					WearableType.Jacket, WearableType.Gloves, WearableType.Undershirt, WearableType.Alpha,
					WearableType.Invalid },
			new WearableType[] { WearableType.Shape, WearableType.Skin, WearableType.Tattoo, WearableType.Pants,
					WearableType.Shoes, WearableType.Socks, WearableType.Jacket, WearableType.Underpants,
					WearableType.Alpha },
			new WearableType[] { WearableType.Eyes, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid },
			new WearableType[] { WearableType.Skirt, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid },
			new WearableType[] { WearableType.Hair, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid } };

	// Magic values to finalize the cache check hashes for each bake
	public static final UUID[] BAKED_TEXTURE_HASH = new UUID[] { new UUID("18ded8d6-bcfc-e415-8539-944c0f5ea7a6"),
			new UUID("338c29e3-3024-4dbb-998d-7c04cf4fa88f"), new UUID("91b4a2c7-1b1a-ba16-9a16-1f8f8dcc1c3f"),
			new UUID("b2cf28af-b840-1071-3c6a-78085d8128b5"), new UUID("ea800387-ea1a-14e0-56cb-24f2022f969a"),
			new UUID("0af1ef7c-ad24-11dd-8790-001f5bf833e8") };
	// Default avatar texture, used to detect when a custom texture is not set for a
	// face
	public static final UUID DEFAULT_AVATAR_TEXTURE = new UUID("c228d1cf-4b5d-4ba8-84f4-899a0796aa97");

	// #endregion Structs / Classes

	// #region Event delegates, Raise Events

}
