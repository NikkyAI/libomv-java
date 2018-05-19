package libomv.model.group;

import libomv.types.UUID;

// Struct representing a member of a group chat session and their settings
public final class ChatSessionMember {
	// The <see cref="UUID"/> of the Avatar
	public UUID AvatarKey;
	// True if user has voice chat enabled
	public boolean CanVoiceChat;
	// True of Avatar has moderator abilities
	public boolean IsModerator;
	// True if a moderator has muted this avatars chat
	public boolean MuteText;
	// True if a moderator has muted this avatars voice
	public boolean MuteVoice;

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ChatSessionMember))
			return false;
		return AvatarKey.equals(((ChatSessionMember) obj).AvatarKey);
	}

	public int hashCode() {
		return AvatarKey.hashCode();
	}
}