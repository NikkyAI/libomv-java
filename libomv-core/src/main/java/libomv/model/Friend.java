package libomv.model;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.CallbackArgs;

public interface Friend {

	public static class FriendRights {
		/** The avatar has no rights */
		public static final byte None = 0;
		/** The avatar can see the online status of the target avatar */
		public static final byte CanSeeOnline = 1;
		/** The avatar can see the location of the target avatar on the map */
		public static final byte CanSeeOnMap = 2;
		/** The avatar can modify the ojects of the target avatar */
		public static final byte CanModifyObjects = 4;

		private static final String[] _names = new String[] { "None", "SeeOnline", "SeeOnMap", "ModifyObjects" };

		public static String toString(byte value) {
			if ((value & _mask) == 0)
				return _names[0];

			String rights = "";
			for (int i = 1; i < _names.length; i++) {
				if ((value & (1 << (i - 1))) != 0) {
					rights.concat(_names[i] + ", ");
				}
			}
			return rights.substring(0, rights.length() - 2);
		}

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static int getValue(int value) {
			return value;
		}

		private static final byte _mask = 0x7;
	}

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

	// #region callback handlers

	// Triggered whenever a friend comes online or goes offline
	public class FriendNotificationCallbackArgs implements CallbackArgs {
		private final UUID[] agentIDs;
		private final boolean online;

		public UUID[] getAgentID() {
			return agentIDs;
		}

		public boolean getOnline() {
			return online;
		}

		public FriendNotificationCallbackArgs(UUID[] agentIDs, boolean online) {
			this.agentIDs = agentIDs;
			this.online = online;
		}
	}

	public abstract class FriendNotificationCallback implements Callback<FriendNotificationCallbackArgs> {
		@Override
		public abstract boolean callback(FriendNotificationCallbackArgs params);
	}

	public class FriendListChangedCallbackArgs implements CallbackArgs {
		private final FriendInfo info;
		private final boolean added;

		public FriendInfo getFriendInfo() {
			return info;
		}

		public boolean getIsAdded() {
			return added;
		}

		public FriendListChangedCallbackArgs(FriendInfo info, boolean added) {
			this.info = info;
			this.added = added;
		}
	}

	public abstract class FriendListChangedCallback implements Callback<FriendListChangedCallbackArgs> {
		@Override
		public abstract boolean callback(FriendListChangedCallbackArgs params);
	}

	// Triggered when a friends rights changed
	public class FriendRightsCallbackArgs implements CallbackArgs {
		private final FriendInfo friendInfo;

		public FriendInfo getFriendInfo() {
			return friendInfo;
		}

		public FriendRightsCallbackArgs(FriendInfo friendInfo) {
			this.friendInfo = friendInfo;
		}
	}

	public abstract class FriendRightsCallback implements Callback<FriendRightsCallbackArgs> {
		@Override
		public abstract boolean callback(FriendRightsCallbackArgs params);
	}

	// Triggered when a map request for a friend is answered
	public class FriendFoundReplyCallbackArgs implements CallbackArgs {
		private final UUID agentID;
		private final long regionHandle;
		private final Vector3 location;

		public UUID getAgentID() {
			return agentID;
		}

		public long getRegionHandle() {
			return regionHandle;
		}

		public Vector3 getLocation() {
			return location;
		}

		public FriendFoundReplyCallbackArgs(UUID agentID, long regionHandle, Vector3 location) {
			this.agentID = agentID;
			this.regionHandle = regionHandle;
			this.location = location;
		}
	}

	public abstract class FriendFoundReplyCallback implements Callback<FriendFoundReplyCallbackArgs> {
		@Override
		public abstract boolean callback(FriendFoundReplyCallbackArgs params);
	}

	/* Triggered when someone offers us friendship */
	public class FriendshipOfferedCallbackArgs implements CallbackArgs {
		private final UUID friendID;
		private final String name;
		private final UUID sessionID;

		public UUID getFriendID() {
			return friendID;
		}

		public String getName() {
			return name;
		}

		public UUID getSessionID() {
			return sessionID;
		}

		public FriendshipOfferedCallbackArgs(UUID friendID, String name, UUID sessionID) {
			this.friendID = friendID;
			this.name = name;
			this.sessionID = sessionID;
		}
	}

	public abstract class FriendshipOfferedCallback implements Callback<FriendshipOfferedCallbackArgs> {
		@Override
		public abstract boolean callback(FriendshipOfferedCallbackArgs params);
	}

	/* Triggered when someone accepts or rejects a friendship request */
	public class FriendshipResponseCallbackArgs implements CallbackArgs {
		private final UUID agentID;
		private final String name;
		private final boolean accepted;

		public UUID getAgentID() {
			return agentID;
		}

		public String getName() {
			return name;
		}

		public boolean getAccepted() {
			return accepted;
		}

		public FriendshipResponseCallbackArgs(UUID agentID, String name, boolean accepted) {
			this.agentID = agentID;
			this.name = name;
			this.accepted = accepted;
		}
	}

	public abstract class FriendshipResponseCallback implements Callback<FriendshipResponseCallbackArgs> {
		@Override
		public abstract boolean callback(FriendshipResponseCallbackArgs params);
	}

	/* Triggered when someone terminated friendship with us */
	public class FriendshipTerminatedCallbackArgs implements CallbackArgs {
		private final UUID otherID;
		private final String name;

		public UUID getOtherID() {
			return otherID;
		}

		public String getName() {
			return name;
		}

		public FriendshipTerminatedCallbackArgs(UUID otherID, String name) {
			this.otherID = otherID;
			this.name = name;
		}
	}

	public abstract class FriendshipTerminatedCallback implements Callback<FriendshipTerminatedCallbackArgs> {
		@Override
		public abstract boolean callback(FriendshipTerminatedCallbackArgs params);
	}

}
