package libomv.model.friend;

import libomv.types.UUID;

/**
 * This class holds information about an avatar in the friends list. There are
 * two ways to interface to this class. The first is through the set of boolean
 * properties. This is the typical way clients of this class will use it. The
 * second interface is through two bitflag properties, TheirFriendsRights and
 * MyFriendsRights
 */
// TODO:FIXME Changing several fields to public, they need getters instead!
public class FriendInfo {
	private UUID ID;
	private String name;
	private boolean isOnline;
	public byte myRights; // private
	public byte theirRights; // private

	/* System ID of the avatar */
	public final UUID getID() {
		return ID;
	}

	/* full name of the avatar */
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	/* True if the avatar is online */
	public final boolean getIsOnline() {
		return isOnline;
	}

	public final void setIsOnline(boolean value) {
		isOnline = value;
	}

	/* True if the friend can see if I am online */
	public final boolean getCanSeeMeOnline() {
		return (theirRights & FriendRights.CanSeeOnline) != 0;
	}

	public final void setCanSeeMeOnline(boolean value) {
		if (value) {
			theirRights |= FriendRights.CanSeeOnline;
		} else {
			// if they can't see me online, then they also can't see me on
			// the map
			theirRights &= ~(FriendRights.CanSeeOnline | FriendRights.CanSeeOnMap);
		}
	}

	/* True if the friend can see me on the map */
	public final boolean getCanSeeMeOnMap() {
		return (theirRights & FriendRights.CanSeeOnMap) != 0;
	}

	public final void setCanSeeMeOnMap(boolean value) {
		if (value)
			theirRights |= FriendRights.CanSeeOnMap;
		else
			theirRights &= ~FriendRights.CanSeeOnMap;

	}

	/* True if the friend can modify my objects */
	public final boolean getCanModifyMyObjects() {
		return (theirRights & FriendRights.CanModifyObjects) != 0;
	}

	public final void setCanModifyMyObjects(boolean value) {
		if (value)
			theirRights |= FriendRights.CanModifyObjects;
		else
			theirRights &= ~FriendRights.CanModifyObjects;
	}

	/* True if I can see if my friend is online */
	public final boolean getCanSeeThemOnline() {
		return (myRights & FriendRights.CanSeeOnline) != 0;
	}

	/* True if I can see if my friend is on the map */
	public final boolean getCanSeeThemOnMap() {
		return (myRights & FriendRights.CanSeeOnMap) != 0;
	}

	/* True if I can modify my friend's objects */
	public final boolean getCanModifyTheirObjects() {
		return (myRights & FriendRights.CanModifyObjects) != 0;
	}

	/**
	 * Used internally when building the initial list of friends at login time
	 *
	 * @param id
	 *            System ID of the avatar being prepesented
	 * @param buddy_rights_given
	 *            Rights the friend has to see you online and to modify your objects
	 * @param buddy_rights_has
	 *            Rights you have to see your friend online and to modify their
	 *            objects
	 */
	public FriendInfo(UUID id, int buddy_rights_given, int buddy_rights_has) {
		ID = id;
		this.theirRights = (byte) buddy_rights_given;
		this.myRights = (byte) buddy_rights_has;
	}

	public boolean equals(FriendInfo o) {
		return ID.equals(o.getID());
	}

	@Override
	public boolean equals(Object o) {
		return (o != null && o instanceof FriendInfo) ? equals((FriendInfo) o) : false;
	}

	@Override
	public int hashCode() {
		return ID.hashCode();
	}

	/**
	 * FriendInfo represented as a string
	 *
	 * @return A string reprentation of both my rights and my friends rights
	 */
	@Override
	public String toString() {
		return String.format("%f (Their Rights: %1x, My Rights: %1x)", getName(),
				FriendRights.toString(theirRights), FriendRights.toString(myRights));
	}
}