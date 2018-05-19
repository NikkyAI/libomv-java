package libomv.model.group;

import libomv.types.UUID;

// Avatar group management
public final class GroupMember {
	// Key of Group Member
	public UUID ID;
	// Total land contribution
	public int Contribution;
	// Online status information
	public String OnlineStatus;
	// Abilities that the Group Member has
	public long Powers;
	// Current group title
	public String Title;
	// Is a group owner
	public boolean IsOwner;

	public GroupMember(UUID agentID) {
		ID = agentID;
	}
}