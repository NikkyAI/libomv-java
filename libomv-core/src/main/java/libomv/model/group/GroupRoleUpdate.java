package libomv.model.group;

// Role update flags
public enum GroupRoleUpdate {
	//
	NoUpdate,
	//
	UpdateData,
	//
	UpdatePowers,
	//
	UpdateAll,
	//
	Create,
	//
	Delete;

	public GroupRoleUpdate setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}