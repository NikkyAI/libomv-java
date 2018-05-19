package libomv.model.agent;

import org.apache.log4j.Logger;

/**
 * Flag in Instant Messages, whether the IM should be delivered to offline
 * avatars as well
 */
public enum InstantMessageOnline {
	/* Only deliver to online avatars */
	Online, // 0
	/*
	 * If the avatar is offline the message will be held until they login next, and
	 * possibly forwarded to their e-mail account
	 */
	Offline; // 1

	public static InstantMessageOnline setValue(int value) {
		if (values().length > value)
			return values()[value];
		Logger.getLogger(InstantMessageOnline.class).error("Invalid InstantMessageOnline value: " + value);
		return Offline;
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}