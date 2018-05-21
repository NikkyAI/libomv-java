package libomv.model.avatar;

import libomv.model.Simulator;
import libomv.model.appearance.AppearanceFlags;
import libomv.primitives.TextureEntry;
import libomv.types.UUID;
import libomv.utils.CallbackArgs;

public class AvatarAppearanceCallbackArgs implements CallbackArgs {
	private Simulator simulator;
	private UUID id;
	private boolean isTrial;
	private TextureEntry.TextureEntryFace defaultTexture;
	private TextureEntry.TextureEntryFace[] faceTextures;
	private byte[] parameters;
	private byte appearanceVersion;
	private int cofVersion;
	private AppearanceFlags appearanceFlags;

	public AvatarAppearanceCallbackArgs(Simulator simulator, UUID id, boolean isTrial,
			TextureEntry.TextureEntryFace defaultTexture, TextureEntry.TextureEntryFace[] faceTextures,
			byte[] parameters, byte appearanceVersion, int cofVersion, AppearanceFlags appearanceFlags) {
		this.simulator = simulator;
		this.id = id;
		this.isTrial = isTrial;
		this.defaultTexture = defaultTexture;
		this.faceTextures = faceTextures;
		this.parameters = parameters;
		this.appearanceVersion = appearanceVersion;
		this.cofVersion = cofVersion;
		this.appearanceFlags = appearanceFlags;
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public UUID getId() {
		return id;
	}

	public boolean getIsTrial() {
		return isTrial;
	}

	public TextureEntry.TextureEntryFace getDefaultTexture() {
		return defaultTexture;
	}

	public TextureEntry.TextureEntryFace[] getFaceTextures() {
		return faceTextures;
	}

	public byte[] getVisualParameters() {
		return parameters;
	}

	public byte getAppearanceVersion() {
		return appearanceVersion;
	}

	public int getCOFVersion() {
		return cofVersion;
	}

	public AppearanceFlags getAppearanceFlags() {
		return appearanceFlags;
	}

}