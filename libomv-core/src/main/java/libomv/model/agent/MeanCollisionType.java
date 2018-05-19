package libomv.model.agent;

public enum MeanCollisionType {
	/* */
	None,
	/* */
	Bump,
	/* */
	LLPushObject,
	/* */
	SelectedObjectCollide,
	/* */
	ScriptedObjectCollide,
	/* */
	PhysicalObjectCollide;

	public static MeanCollisionType setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}