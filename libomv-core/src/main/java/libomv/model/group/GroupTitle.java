package libomv.model.group;

import libomv.types.UUID;

// Class to represent Group Title
public final class GroupTitle {
	// Key of the group
	public UUID GroupID;
	// ID of the role title belongs to
	public UUID RoleID;
	// Group Title
	public String Title;
	// Whether title is Active
	public boolean Selected;

	// Returns group title
	@Override
	public String toString() {
		return Title;
	}

	public GroupTitle() {
	}
}