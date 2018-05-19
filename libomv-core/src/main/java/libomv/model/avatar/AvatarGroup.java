package libomv.model.avatar;

import libomv.types.UUID;

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