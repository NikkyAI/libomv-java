package libomv.model.agent;

/* Effect type used in ViewerEffect packets */
public enum EffectType {
	/* */
	Text, // 0
	/* */
	Icon, // 1
	/* */
	Connector, // 2
	/* */
	FlexibleObject, // 3
	/* */
	AnimalControls, // 4
	/* */
	AnimationObject, // 5
	/* */
	Cloth, // 6
	/*
	 * Project a beam from a source to a destination, such as the one used when
	 * editing an object
	 */
	Beam, // 7
	/* */
	Glow, // 8
	/* */
	Point, // 9
	/* */
	Trail, // 10
	/* Create a swirl of particles around an object */
	Sphere, // 11
	/* */
	Spiral, // 12
	/* */
	Edit, // 13
	/* Cause an avatar to look at an object */
	LookAt, // 14
	/* Cause an avatar to point at an object */
	PointAt; // 15

	public static EffectType setValue(byte value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}