package libomv.model.avatar;

import libomv.types.UUID;

/**
 * Holds group information for Avatars such as those you might find in a profile
 */
public final class AvatarGroup {
	/* true of Avatar accepts group notices */
	public boolean acceptNotices;
	/* Groups Key */
	public UUID groupID;
	/* Texture Key for groups insignia */
	public UUID groupInsigniaID;
	/* Name of the group */
	public String groupName;
	/* Powers avatar has in the group */
	public long groupPowers;
	/* Avatars Currently selected title */
	public String groupTitle;
	/* true of Avatar has chosen to list this in their profile */
	public boolean listInProfile;
}