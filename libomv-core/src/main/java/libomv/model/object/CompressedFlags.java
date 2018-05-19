package libomv.model.object;

// Bitflag field for ObjectUpdateCompressed data blocks, describing
// which options are present for each object
public class CompressedFlags {
	public static final short None = 0x00;
	// Unknown
	public static final short ScratchPad = 0x01;
	// Whether the object has a TreeSpecies
	public static final short Tree = 0x02;
	// Whether the object has floating text ala llSetText
	public static final short HasText = 0x04;
	// Whether the object has an active particle system
	public static final short HasParticles = 0x08;
	// Whether the object has sound attached to it
	public static final short HasSound = 0x10;
	// Whether the object is attached to a root object or not
	public static final short HasParent = 0x20;
	// Whether the object has texture animation settings
	public static final short TextureAnimation = 0x40;
	// Whether the object has an angular velocity
	public static final short HasAngularVelocity = 0x80;
	// Whether the object has a name value pairs string
	public static final short HasNameValues = 0x100;
	// Whether the object has a Media URL set
	public static final short MediaURL = 0x200;

	private static final short _mask = 0x3FF;

	private CompressedFlags() {
	}

	public static short setValue(short value) {
		return (short) (value & _mask);
	}

	public static short getValue(int value) {
		return (short) (value & _mask);
	}

}