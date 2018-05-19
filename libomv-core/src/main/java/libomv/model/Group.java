package libomv.model;

import libomv.types.UUID;

//Represents a group on the grid
public class Group {

	// Key of Group
	private UUID ID;
	// Key of Group Insignia
	public UUID InsigniaID;
	// Key of Group Founder
	public UUID FounderID;
	// Key of Group Role for Owners
	public UUID OwnerRole;
	// Name of Group
	public String Name; // private
	// Text of Group Charter
	public String Charter;
	// Title of "everyone" role
	public String MemberTitle;
	// Is the group open for enrolement to everyone
	public boolean OpenEnrollment;
	// Will group show up in search
	public boolean ShowInList;
	// GroupPowers flags
	public long Powers;
	//
	public boolean AcceptNotices;
	//
	public boolean AllowPublish;
	// Is the group Mature
	public boolean MaturePublish;
	// Cost of group membership
	public int MembershipFee;
	//
	public int Money;
	//
	public int Contribution;
	// The total number of current members this group has
	public int GroupMembershipCount;
	// The number of roles this group has configured
	public int GroupRolesCount;
	// Show this group in agent's profile
	public boolean ListInProfile;

	public UUID getID() {
		return ID;
	}

	public String getName() {
		return Name;
	}

	@Override
	public boolean equals(Object o) {
		return (o != null && o instanceof Group) ? equals((Group) o) : false;
	}

	public boolean equals(Group o) {
		return o != null ? ID.equals(o.getID()) : false;
	}

	@Override
	public int hashCode() {
		return ID.hashCode();
	}

	// Returns the name of the group
	@Override
	public String toString() {
		return Name;
	}

	public Group(UUID id) {
		ID = id;
		InsigniaID = new UUID();
	}

}
