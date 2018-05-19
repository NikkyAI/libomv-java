package libomv.model.group;

import libomv.types.UUID;

// Struct representing a member of a group chat session and their settings
public final class ChatSessionMember {
	// The <see cref="UUID"/> of the Avatar
	public UUID avatarKey;
	// True if user has voice chat enabled
	public boolean canVoiceChat;
	// True of Avatar has moderator abilities
	public boolean isModerator;
	// True if a moderator has muted this avatars chat
	public boolean muteText;
	// True if a moderator has muted this avatars voice
	public boolean muteVoice;

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ChatSessionMember))
			return false;
		return avatarKey.equals(((ChatSessionMember) obj).avatarKey);
	}

	public int hashCode() {
		return avatarKey.hashCode();
	}
}