package libomv.model.group;

import libomv.types.UUID;

// Avatar group management
public final class GroupMember {
	// Key of Group Member
	public UUID id;
	// Total land contribution
	public int contribution;
	// Online status information
	public String onlineStatus;
	// Abilities that the Group Member has
	public long powers;
	// Current group title
	public String title;
	// Is a group owner
	public boolean isOwner;

	public GroupMember(UUID agentID) {
		this.id = agentID;
	}
}