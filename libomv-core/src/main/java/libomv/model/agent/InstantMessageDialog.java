package libomv.model.agent;

import org.apache.log4j.Logger;

/** Special commands used in Instant Messages */
public enum InstantMessageDialog {
	/*
	 * Indicates a regular IM from another agent, ID is meaningless, nothing in the
	 * binary bucket.
	 */
	MessageFromAgent, // 0
	/* Simple notification box with an OK button */
	MessageBox, // 1
	/* Used to show a countdown notification with an OK button, deprecated now */
	Deprecated_MessageBoxCountdown, // 2
	/*
	 * You've been invited to join a group. ID is the group id. The binary bucket
	 * contains a null terminated string representation of the officer/member status
	 * and join cost for the invitee. The format is 1 byte for officer/member (O for
	 * officer, M for member), and as many bytes as necessary for cost.
	 */
	GroupInvitation, // 3
	/*
	 * Inventory offer, ID is the transaction id, binary bucket is a list of
	 * inventory uuid and type.
	 */
	InventoryOffered, // 4
	/* Accepted inventory offer */
	InventoryAccepted, // 5
	/* Declined inventory offer */
	InventoryDeclined, // 6
	/*
	 * Group vote, Name is name of person who called vote, ID is vote ID used for
	 * internal tracking
	 */
	GroupVote, // 7
	/* A message to everyone in the agent's group, no longer used */
	Deprecated_GroupMessage, // 8
	/*
	 * An object is offering its inventory, ID is the transaction id, Binary bucket
	 * is a (mostly) complete packed inventory item
	 */
	TaskInventoryOffered, // 9
	/* Accept an inventory offer from an object */
	TaskInventoryAccepted, // 10
	/* Decline an inventory offer from an object */
	TaskInventoryDeclined, // 11
	/* Unknown */
	NewUserDefault, // 12
	/* Start a session, or add users to a session */
	SessionAdd, // 13
	/* Start a session, but don't prune offline users */
	SessionOfflineAdd, // 14
	/* Start a session with your group */
	SessionGroupStart, // 15
	/* Start a session without a calling card (finder or objects) */
	SessionCardlessStart, // 16
	/* Send a message to a session */
	SessionSend, // 17
	/* Leave a session */
	SessionDrop, // 18
	/* Indicates that the IM is from an object */
	MessageFromObject, // 19
	/* Sent an IM to a busy user, this is the auto response */
	BusyAutoResponse, // 20
	/* Shows the message in the console and chat history */
	ConsoleAndChatHistory, // 21
	/* Send a teleport lure */
	RequestTeleport, // 22
	/* Response sent to the agent which inititiated a teleport invitation */
	AcceptTeleport, // 23
	/* Response sent to the agent which inititiated a teleport invitation */
	DenyTeleport, // 24
	/* Only useful if you have Linden permissions */
	GodLikeRequestTeleport, // 25
	/* Request a teleport lure */
	RequestLure, // 26
	/* Notification of a new group election, this is depreciated */
	@Deprecated
	Deprecated_GroupElection, // 27
	/*
	 * IM to tell the user to go to an URL. Put a text message in the message field,
	 * and put the url with a trailing \0 in the binary bucket.
	 */
	GotoUrl, // 28
	/* IM for help */
	Session911Start, // 29
	/*
	 * IM sent automatically on call for help, sends a lure to each Helper reached
	 */
	Lure911, // 30
	/* Like an IM but won't go to email */
	FromTaskAsAlert, // 31
	/* IM from a group officer to all group members */
	GroupNotice, // 32
	/* Unknown */
	GroupNoticeInventoryAccepted, // 33
	/* Unknown */
	GroupNoticeInventoryDeclined, // 34
	/* Accept a group invitation */
	GroupInvitationAccept, // 35
	/* Decline a group invitation */
	GroupInvitationDecline, // 36
	/* Unknown */
	GroupNoticeRequested, // 37
	/* An avatar is offering you friendship */
	FriendshipOffered, // 38
	/* An avatar has accepted your friendship offer */
	FriendshipAccepted, // 39
	/* An avatar has declined your friendship offer */
	FriendshipDeclined, // 40
	/* Indicates that a user has started typing */
	StartTyping, // 41
	/* Indicates that a user has stopped typing */
	StopTyping; // 42

	public static InstantMessageDialog setValue(int value) {
		if (values().length > value)
			return values()[value];
		Logger.getLogger(InstantMessageDialog.class).error("Invalid InstantMessageDialog value: " + value);
		return MessageFromAgent;
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}