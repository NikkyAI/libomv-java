package libomv.model;

import libomv.types.UUID;

//Represents a group on the grid
public class Group {

	// Key of Group
	private UUID id;
	// Key of Group Insignia
	public UUID insigniaID;
	// Key of Group Founder
	public UUID founderID;
	// Key of Group Role for Owners
	public UUID ownerRole;
	// Name of Group
	public String name; // private
	// Text of Group Charter
	public String charter;
	// Title of "everyone" role
	public String memberTitle;
	// Is the group open for enrolement to everyone
	public boolean openEnrollment;
	// Will group show up in search
	public boolean showInList;
	// GroupPowers flags
	public long powers;
	//
	public boolean acceptNotices;
	//
	public boolean allowPublish;
	// Is the group Mature
	public boolean maturePublish;
	// Cost of group membership
	public int membershipFee;
	//
	public int money;
	//
	public int contribution;
	// The total number of current members this group has
	public int groupMembershipCount;
	// The number of roles this group has configured
	public int groupRolesCount;
	// Show this group in agent's profile
	public boolean listInProfile;

	public UUID getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		return (o != null && o instanceof Group) ? equals((Group) o) : false;
	}

	public boolean equals(Group o) {
		return o != null ? id.equals(o.getID()) : false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	// Returns the name of the group
	@Override
	public String toString() {
		return name;
	}

	public Group(UUID id) {
		this.id = id;
		this.insigniaID = new UUID();
	}

}
