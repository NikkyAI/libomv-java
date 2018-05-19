package libomv.model.group;

import libomv.types.UUID;

// Class to represent Group Title
public final class GroupTitle {
	// Key of the group
	public UUID groupID;
	// ID of the role title belongs to
	public UUID roleID;
	// Group Title
	public String title;
	// Whether title is Active
	public boolean selected;

	// Returns group title
	@Override
	public String toString() {
		return title;
	}
}