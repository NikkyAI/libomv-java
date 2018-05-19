package libomv.model.group;

import libomv.types.UUID;

// Role manager for a group
public final class GroupRole {
	// Key of the group
	public UUID groupID;
	// Key of Role
	public UUID id;
	// Name of Role
	public String name;
	// Group Title associated with Role
	public String title;
	// Description of Role
	public String description;
	// Abilities Associated with Role
	public long powers;

	public GroupRole(UUID roleID) {
		id = roleID;
	}

	// Returns the role's title
	@Override
	public String toString() {
		return name;
	}

}