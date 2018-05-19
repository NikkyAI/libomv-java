package libomv.model.appearance;

import java.util.HashMap;

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