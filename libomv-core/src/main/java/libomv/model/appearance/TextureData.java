package libomv.model.appearance;

import java.util.Map;

import libomv.VisualParams.VisualAlphaParam;
import libomv.assets.AssetTexture;
import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.types.Color4;
import libomv.types.UUID;

// Holds a texture assetID and the data needed to bake this layer into an outfit
// texture.
// Used to keep track of currently worn textures and baking data
public class TextureData {
	// A texture AssetID
	public UUID textureID;
	// Asset data for the texture
	public AssetTexture texture;
	// Collection of alpha masks that needs applying
	public Map<VisualAlphaParam, Float> alphaMasks;
	// Tint that should be applied to the texture
	public Color4 color;
	// The avatar texture index this texture is for
	public AvatarTextureIndex textureIndex;
	// Host address for this texture
	public String host;

	@Override
	public String toString() {
		return String.format("TextureID: %s, Texture: %s", textureID,
				texture != null ? texture.getAssetData().length + " bytes" : "(null)");
	}
}