package libomv.model.group;

import libomv.types.UUID;

// Role manager for a group
public final class GroupRole {
	// Key of the group
	public UUID GroupID;
	// Key of Role
	public UUID ID;
	// Name of Role
	public String Name;
	// Group Title associated with Role
	public String Title;
	// Description of Role
	public String Description;
	// Abilities Associated with Role
	public long Powers;

	// Returns the role's title
	@Override
	public String toString() {
		return Name;
	}

	public GroupRole(UUID roleID) {
		ID = roleID;
	}
}