package libomv.model.agent;

import libomv.types.UUID;

// Represents muted object or resident
public class MuteEntry {
	// Type of the mute entry
	public MuteType Type;
	// UUID of the mute entry
	public UUID ID;
	// Mute entry name
	public String Name;
	// Mute flags
	public byte Flags;
}