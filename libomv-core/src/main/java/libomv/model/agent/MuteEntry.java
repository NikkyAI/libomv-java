package libomv.model.agent;

import libomv.types.UUID;

// Represents muted object or resident
public class MuteEntry {
	// Type of the mute entry
	public MuteType type;
	// UUID of the mute entry
	public UUID id;
	// Mute entry name
	public String name;
	// Mute flags
	public byte flags;
}