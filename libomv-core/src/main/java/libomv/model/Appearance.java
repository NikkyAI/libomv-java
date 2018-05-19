package libomv.model;

import java.util.HashMap;

import org.apache.log4j.Logger;

import libomv.VisualParams.VisualColorParam;
import libomv.VisualParams.VisualParam;
import libomv.assets.AssetTexture;
import libomv.assets.AssetWearable;
import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.assets.AssetWearable.WearableType;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

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

	// Appearance Flags, introdued with server side baking, currently unused
	// [Flags]
	public enum AppearanceFlags {
		None;

		public static AppearanceFlags setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			Logger.getLogger(AppearanceFlags.class).warn("Unknown Appearance flag value" + value);
			return None;
		}

		public static byte getValue(BakeType value) {
			return (byte) (value.ordinal());
		}
	}

	// Bake layers for avatar appearance
	public enum BakeType {
		Unknown, Head, UpperBody, LowerBody, Eyes, Skirt, Hair;
		public static BakeType setValue(int value) {
			if (value <= 0 && value < Hair.ordinal())
				return values()[value + 1];
			return Unknown;
		}

		public static byte getValue(BakeType value) {
			return (byte) (value.ordinal() - 1);
		}

		public static int getNumValues() {
			return values().length - 1;
		}

		public byte getValue() {
			return (byte) (ordinal() - 1);
		}
	}

	// Contains information about a wearable inventory item
	public class WearableData {
		// Inventory ItemID of the wearable</summary>
		public UUID ItemID;
		// AssetID of the wearable asset</summary>
		public UUID AssetID;
		// WearableType of the wearable</summary>
		public WearableType WearableType;
		// AssetType of the wearable</summary>
		public libomv.assets.AssetItem.AssetType AssetType;
		// Asset data for the wearable</summary>
		public AssetWearable Asset;

		@Override
		public String toString() {
			return String.format("ItemID: %s, AssetID: %s, WearableType: %s, AssetType: %s, Asset: %s", ItemID, AssetID,
					WearableType, AssetType, Asset != null ? Asset.name : "(null)");
		}
	}

	// Data collected from visual params for each wearable needed for the
	// calculation of the color
	public class ColorParamInfo {
		public VisualParam VisualParam;
		public VisualColorParam VisualColorParam;
		public float Value;
		public WearableType WearableType;
	}

	// Holds a texture assetID and the data needed to bake this layer into an outfit
	// texture.
	// Used to keep track of currently worn textures and baking data
	public class TextureData {
		// A texture AssetID
		public UUID TextureID;
		// Asset data for the texture
		public AssetTexture Texture;
		// Collection of alpha masks that needs applying
		public HashMap<libomv.VisualParams.VisualAlphaParam, Float> AlphaMasks;
		// Tint that should be applied to the texture
		public Color4 Color;
		// The avatar texture index this texture is for
		public AvatarTextureIndex TextureIndex;
		// Host address for this texture
		public String Host;

		@Override
		public String toString() {
			return String.format("TextureID: %s, Texture: %s", TextureID,
					Texture != null ? Texture.getAssetData().length + " bytes" : "(null)");
		}
	}

	// #endregion Structs / Classes

	// #region Event delegates, Raise Events

	// Triggered when an AgentWearablesUpdate packet is received, telling us what
	// our avatar is currently wearing
	// <see cref="RequestAgentWearables"/> request.
	public class AgentWearablesReplyCallbackArgs implements CallbackArgs {
		// Construct a new instance of the AgentWearablesReplyEventArgs class
		public AgentWearablesReplyCallbackArgs() {
		}
	}

	// Raised when an AgentCachedTextureResponse packet is received, giving a list
	// of cached bakes that were found
	// on the simulator <see cref="RequestCachedBakes"/> request.
	public class AgentCachedBakesReplyCallbackArgs implements CallbackArgs {
		private final int serialNum;
		private final int numBakes;

		public int getSerialNum() {
			return serialNum;
		}

		public int getNumBakes() {
			return numBakes;
		}

		// Construct a new instance of the AgentCachedBakesReplyEventArgs class
		public AgentCachedBakesReplyCallbackArgs(int serialNum, int numBakes) {
			this.serialNum = serialNum;
			this.numBakes = numBakes;
		}
	}

	// Raised when appearance data is sent to the simulator, also indicates the main
	// appearance thread is finished.
	// <see cref="RequestAgentSetAppearance"/> request.
	public class AppearanceSetCallbackArgs implements CallbackArgs {
		private final boolean m_success;

		// Indicates whether appearance setting was successful
		public boolean getSuccess() {
			return m_success;
		}

		/**
		 * Triggered when appearance data is sent to the sim and the main appearance
		 * thread is done.
		 *
		 * @param success
		 *            Indicates whether appearance setting was successful
		 */
		public AppearanceSetCallbackArgs(boolean success) {
			this.m_success = success;
		}
	}

	// Triggered when the simulator requests the agent rebake its appearance.
	// <see cref="RebakeAvatarRequest"/>
	public class RebakeAvatarTexturesCallbackArgs implements CallbackArgs {
		private final UUID m_textureID;

		// The ID of the Texture Layer to bake
		public UUID getTextureID() {
			return m_textureID;
		}

		/**
		 * Triggered when the simulator sends a request for this agent to rebake its
		 * appearance
		 *
		 * @param textureID
		 *            The ID of the Texture Layer to bake
		 */
		public RebakeAvatarTexturesCallbackArgs(UUID textureID) {
			this.m_textureID = textureID;
		}
	}

}
