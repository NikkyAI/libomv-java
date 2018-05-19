package libomv.model;

import java.util.ArrayList;
import java.util.HashMap;

import libomv.model.Agent.AgentDisplayName;
import libomv.model.Agent.EffectType;
import libomv.model.Appearance.AppearanceFlags;
import libomv.primitives.TextureEntry;
import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.CallbackArgs;

public interface Avatar {
	/**
	 * Contains an animation currently being played by an agent
	 */
	public class Animation {
		// The ID of the animation asset
		public UUID animationID;
		// A number to indicate start order of currently playing animations
		// On Linden Grids this number is unique per region, with OpenSim it is
		// per client
		public int animationSequence;
		//
		public UUID animationSourceObjectID;
	}

	/**
	 * Holds group information on an individual profile pick
	 */
	public class ProfilePick {
		public UUID PickID;
		public UUID CreatorID;
		public boolean TopPick;
		public UUID ParcelID;
		public String Name;
		public String Desc;
		public UUID SnapshotID;
		public String User;
		public String OriginalName;
		public String SimName;
		public Vector3d PosGlobal;
		public int SortOrder;
		public boolean Enabled;
	}

	public class ClassifiedAd {
		public UUID ClassifiedID;
		public int Catagory;
		public UUID ParcelID;
		public int ParentEstate;
		public UUID SnapShotID;
		public Vector3d Position;
		public byte ClassifiedFlags;
		public int Price;
		public String Name;
		public String Desc;
	}

	/**
	 * Holds group information for Avatars such as those you might find in a profile
	 */
	public final class AvatarGroup {
		/* true of Avatar accepts group notices */
		public boolean AcceptNotices;
		/* Groups Key */
		public UUID GroupID;
		/* Texture Key for groups insignia */
		public UUID GroupInsigniaID;
		/* Name of the group */
		public String GroupName;
		/* Powers avatar has in the group */
		public long GroupPowers;
		/* Avatars Currently selected title */
		public String GroupTitle;
		/* true of Avatar has chosen to list this in their profile */
		public boolean ListInProfile;
	}

	// #region Event Callbacks
	public class AgentNamesCallbackArgs implements CallbackArgs {
		private HashMap<UUID, String> names;

		public HashMap<UUID, String> getNames() {
			return names;
		}

		public AgentNamesCallbackArgs(HashMap<UUID, String> names) {
			this.names = names;
		}
	}

	/**
	 * Event args class for display name notification messages
	 */
	public class DisplayNameUpdateCallbackArgs implements CallbackArgs {
		private String oldDisplayName;
		private AgentDisplayName displayName;

		public String getOldDisplayName() {
			return oldDisplayName;
		}

		public AgentDisplayName getDisplayName() {
			return displayName;
		}

		public DisplayNameUpdateCallbackArgs(String oldDisplayName, AgentDisplayName displayName) {
			this.oldDisplayName = oldDisplayName;
			this.displayName = displayName;
		}
	}

	public class AvatarAnimationCallbackArgs implements CallbackArgs {
		private UUID agentID;
		private ArrayList<Animation> animations;

		public UUID getAgentID() {
			return agentID;
		}

		public ArrayList<Animation> getAnimations() {
			return animations;
		}

		public AvatarAnimationCallbackArgs(UUID agentID, ArrayList<Animation> animations) {
			this.agentID = agentID;
			this.animations = animations;
		}
	}

	public class AvatarAppearanceCallbackArgs implements CallbackArgs {
		private Simulator simulator;
		private UUID id;
		private boolean isTrial;
		private TextureEntry.TextureEntryFace defaultTexture;
		private TextureEntry.TextureEntryFace[] faceTextures;
		private byte[] parameters;
		private byte appearanceVersion;
		private int COFVersion;
		private AppearanceFlags appearanceFlags;

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
			return COFVersion;
		}

		public AppearanceFlags getAppearanceFlags() {
			return appearanceFlags;
		}

		public AvatarAppearanceCallbackArgs(Simulator simulator, UUID id, boolean isTrial,
				TextureEntry.TextureEntryFace defaultTexture, TextureEntry.TextureEntryFace[] faceTextures,
				byte[] parameters, byte appearanceVersion, int COFVersion, AppearanceFlags appearanceFlags) {
			this.simulator = simulator;
			this.id = id;
			this.isTrial = isTrial;
			this.defaultTexture = defaultTexture;
			this.faceTextures = faceTextures;
			this.parameters = parameters;
			this.appearanceVersion = appearanceVersion;
			this.COFVersion = COFVersion;
			this.appearanceFlags = appearanceFlags;
		}
	}

	public class AvatarInterestsReplyCallbackArgs implements CallbackArgs {
		private libomv.primitives.Avatar avatar;

		public libomv.primitives.Avatar getAvatar() {
			return avatar;
		}

		public AvatarInterestsReplyCallbackArgs(libomv.primitives.Avatar avatar) {
			this.avatar = avatar;
		}
	}

	public class AvatarPropertiesReplyCallbackArgs implements CallbackArgs {
		private libomv.primitives.Avatar avatar;

		public libomv.primitives.Avatar getAvatar() {
			return avatar;
		}

		public AvatarPropertiesReplyCallbackArgs(libomv.primitives.Avatar avatar) {
			this.avatar = avatar;
		}
	}

	public class AvatarGroupsReplyCallbackArgs implements CallbackArgs {
		private UUID avatarID;
		private ArrayList<AvatarGroup> avatarGroups;

		public UUID getAvatarID() {
			return avatarID;
		}

		public ArrayList<AvatarGroup> getAvatarGroups() {
			return avatarGroups;
		}

		public AvatarGroupsReplyCallbackArgs(UUID avatarID, ArrayList<AvatarGroup> avatarGroups) {
			this.avatarID = avatarID;
			this.avatarGroups = avatarGroups;
		}
	}

	public class AvatarPickerReplyCallbackArgs implements CallbackArgs {
		private UUID queryID;
		private HashMap<UUID, String> avatars;

		public UUID getQueryID() {
			return queryID;
		}

		public HashMap<UUID, String> getAvatars() {
			return avatars;
		}

		public AvatarPickerReplyCallbackArgs(UUID queryID, HashMap<UUID, String> avatars) {
			this.queryID = queryID;
			this.avatars = avatars;
		}
	}

	public class ViewerEffectCallbackArgs implements CallbackArgs {
		private EffectType type;
		private Simulator simulator;
		private UUID sourceAvatar;
		private UUID targetObject;
		private Vector3d targetPos;
		private byte target;
		private float duration;
		private UUID dataID;

		public EffectType getType() {
			return type;
		}

		public Simulator getSimulator() {
			return simulator;
		}

		public UUID getSourceAvatar() {
			return sourceAvatar;
		}

		public UUID getTargetObject() {
			return targetObject;
		}

		public Vector3d getTargetPos() {
			return targetPos;
		}

		public byte getTarget() {
			return target;
		}

		public float getDuration() {
			return duration;
		}

		public UUID getDataID() {
			return dataID;
		}

		public ViewerEffectCallbackArgs(EffectType type, Simulator simulator, UUID sourceAvatar, UUID targetObject,
				Vector3d targetPos, byte target, float duration, UUID dataID) {
			this.type = type;
			this.simulator = simulator;
			this.sourceAvatar = sourceAvatar;
			this.targetObject = targetObject;
			this.targetPos = targetPos;
			this.target = target;
			this.duration = duration;
			this.dataID = dataID;
		}
	}

	// #endregion Event Callbacks

	public class DisplayNamesCallbackArgs {
		private boolean success;
		private AgentDisplayName[] names;
		private UUID[] badIDs;

		public boolean getSuccess() {
			return success;
		}

		public AgentDisplayName[] getNames() {
			return names;
		}

		public UUID[] getBadIDs() {
			return badIDs;
		}

		public DisplayNamesCallbackArgs(boolean success, AgentDisplayName[] names, UUID[] badIDs) {
			this.success = success;
			this.names = names;
			this.badIDs = badIDs;
		}
	}

}
