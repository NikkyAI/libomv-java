package libomv.model.agent;

/*
 * The action an avatar is doing when pointing at something, used in
 * ViewerEffect packets for the PointAt effect
 */
public enum PointAtType {
	/* */
	None,
	/* */
	Select,
	/* */
	Grab,
	/* */
	Clear;

	public static PointAtType setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}