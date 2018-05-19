package libomv.model.agent;

/*
 * Conversion type to denote Chat Packet types in an easier-to-understand format
 */
public enum ChatType {
	/* Whisper (5m radius) */
	Whisper(0),
	/* Normal chat (10/20m radius), what the official viewer typically sends */
	Normal(1),
	/* Shouting! (100m radius) */
	Shout(2),
	/*
	 * Say chat (10/20m radius) - The official viewer will print
	 * "[4:15] You say, hey" instead of "[4:15] You: hey"
	 */
	// Say = 3,
	/* Event message when an Avatar has begun to type */
	StartTyping(4),
	/* Event message when an Avatar has stopped typing */
	StopTyping(5),
	/* Send the message to the debug channel */
	Debug(6),
	/*  */
	Region(7),
	/* Event message when an object uses llOwnerSay */
	OwnerSay(8),
	/* Event message when an object uses llRegionSayTo() */
	RegionSayTo(9),
	/* Special value to support llRegionSay(), never sent to the client */
	RegionSay(255);

	private int val;

	private ChatType(int value) {
		val = value;
	}

	public static ChatType setValue(byte value) {
		for (ChatType e : values()) {
			if (e.val == value)
				return e;
		}
		return Normal;
	}

	public int getValue() {
		return val;
	}

}