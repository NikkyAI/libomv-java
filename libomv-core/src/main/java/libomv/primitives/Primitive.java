/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.primitives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDMap;
import libomv.primitives.TextureEntry.TextureAnimMode;
import libomv.primitives.TextureEntry.TextureAnimation;
import libomv.types.Color4;
import libomv.types.NameValue;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.utils.Helpers;

public class Primitive {
	private static final Logger logger = Logger.getLogger(Primitive.class);

	protected static float CUT_QUANTA = 0.00002f;
	protected static float SCALE_QUANTA = 0.01f;
	protected static float SHEAR_QUANTA = 0.01f;
	protected static float TAPER_QUANTA = 0.01f;
	protected static float REV_QUANTA = 0.015f;
	protected static float HOLLOW_QUANTA = 0.00002f;

	/** Identifier code for primitive types */
	public enum PCode {
		/* None */
		None(0),
		/* A Primitive */
		Prim(9),
		/* A Avatar */
		Avatar(47),
		/* Linden grass */
		Grass(95),
		/* Linden tree */
		NewTree(111),
		/* A primitive that acts as the source for a particle stream */
		ParticleSystem(143),
		/* A Linden true */
		Tree(255);

		private byte value;

		private PCode(int value) {
			this.value = (byte) value;
		}

		public static PCode setValue(int value) {
			for (PCode e : values()) {
				if (e.value == value)
					return e;
			}
			return null;
		}

		public byte getValue() {
			return value;
		}

	}

	// Primary parameters for primitives such as Physics Enabled or Phantom
	// [Flags]
	public static class PrimFlags {
		// Deprecated
		public static final int None = 0;
		// Whether physics are enabled for this object
		public static final int Physics = 0x00000001;
		public static final int CreateSelected = 0x00000002;
		public static final int ObjectModify = 0x00000004;
		public static final int ObjectCopy = 0x00000008;
		public static final int ObjectAnyOwner = 0x00000010;
		public static final int ObjectYouOwner = 0x00000020;
		public static final int Scripted = 0x00000040;
		public static final int Touch = 0x00000080;
		public static final int ObjectMove = 0x00000100;
		public static final int Money = 0x00000200;
		public static final int Phantom = 0x00000400;
		public static final int InventoryEmpty = 0x00000800;
		public static final int JointHinge = 0x00001000;
		public static final int JointP2P = 0x00002000;
		public static final int JointLP2P = 0x00004000;
		// Deprecated */
		public static final int JointWheel = 0x00008000;
		public static final int AllowInventoryDrop = 0x00010000;
		public static final int ObjectTransfer = 0x00020000;
		public static final int ObjectGroupOwned = 0x00040000;
		// Deprecated */
		public static final int ObjectYouOfficer = 0x00080000;
		public static final int CameraDecoupled = 0x00100000;
		public static final int AnimSource = 0x00200000;
		public static final int CameraSource = 0x00400000;
		public static final int CastShadows = 0x00800000;
		// Server flag, will not be sent to clients. Specifies that the object
		// is
		// destroyed when it touches a simulator edge
		public static final int DieAtEdge = 0x01000000;
		// Server flag, will not be sent to clients. Specifies that the object
		// will
		// be returned to the owner's inventory when it touches a simulator edge
		public static final int ReturnAtEdge = 0x02000000;
		// Server flag, will not be sent to clients.
		public static final int Sandbox = 0x04000000;
		// Server flag, will not be sent to client. Specifies that the object is
		// hovering/flying
		public static final int Flying = 0x08000000;
		public static final int ObjectOwnerModify = 0x10000000;
		public static final int TemporaryOnRez = 0x20000000;
		public static final int Temporary = 0x40000000;
		public static final int ZlibCompressed = 0x80000000;

		private static int MASK = 0xFFFFFFFF;

		public static int setValue(int value) {
			return value & MASK;
		}

		public static int getValue(int value) {
			return value & MASK;
		}

	}

	// Sound flags for sounds attached to primitives
	// [Flags]
	public static class SoundFlags {
		public static final byte None = 0;
		public static final byte Loop = 0x01;
		public static final byte SyncMaster = 0x02;
		public static final byte SyncSlave = 0x04;
		public static final byte SyncPending = 0x08;
		public static final byte LinkedList = 0x10;
		public static final byte Stop = 0x20;

		private static byte MASK = (byte) 0xFF;

		public static byte setValue(int value) {
			return (byte) (value & MASK);
		}

		public static byte getValue(byte value) {
			return (byte) (value & MASK);
		}

	}

	public enum ProfileCurve {
		// Circle, Square, IsoTriangle, EqualTriangle, RightTriangle, HalfCircle;
		Circle, Square, IsometricTriangle, EquilateralTriangle, RightTriangle, HalfCircle;

		public static ProfileCurve setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	public enum HoleType {
		Same(0x00), Circle(0x10), Square(0x20), Triangle(0x30);

		private byte value;

		private HoleType(int value) {
			this.value = (byte) value;
		}

		public static HoleType setValue(int value) {
			for (HoleType e : values()) {
				if (e.value == value)
					return e;
			}
			return null;
		}

		public byte getValue() {
			return value;
		}

	}

	public enum PathCurve {
		Line(0x10), Circle(0x20), Circle2(0x30), Test(0x40), Flexible(0x80);

		private byte value;

		private PathCurve(int value) {
			this.value = (byte) value;
		}

		public static PathCurve setValue(int value) {
			for (PathCurve e : values()) {
				if (e.value == value)
					return e;
			}
			return null;
		}

		public byte getValue() {
			return value;
		}

	}

	// Material type for a primitive
	public enum Material {
		Stone, Metal, Glass, Wood, Flesh, Plastic, Rubber, Light;

		public static Material setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// Used in a helper function to roughly determine prim shape
	public enum PrimType {
		Unknown, Box, Cylinder, Prism, Sphere, Torus, Tube, Ring, Sculpt, Mesh;

		public static PrimType setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return Unknown;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// Extra parameters for primitives, these flags are for features that have been
	// added after the original ObjectFlags that has all eight bits reserved already
	public enum ExtraParamType {
		// Whether this object has flexible parameters
		Flexible(0x10),
		// Whether this object has light parameters
		Light(0x20),
		// Whether this object is a sculpted prim
		Sculpt(0x30),
		// Wether this object is a light map
		LightImage(0x40),
		// Whether this object is a mesh
		Mesh(0x60);

		private byte value;

		private ExtraParamType(int value) {
			this.value = (byte) value;
		}

		public static ExtraParamType setValue(int value) {
			for (ExtraParamType e : values()) {
				if (e.value == value)
					return e;
			}
			logger.warn(String.format("Unknown ExtraParamType value %x", value));
			return null;
		}

		public byte getValue() {
			return value;
		}

	}

	public enum JointType {
		Invalid, Hinge, Point,
		// [Obsolete]
		// LPoint,
		// [Obsolete]
		// Wheel
		;

		public static JointType setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			logger.warn(String.format("Unknown JointType value %x", value));
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	public enum SculptType {
		None(0), Sphere(1), Torus(2), Plane(3), Cylinder(4), Mesh(5), Invert(64), Mirror(128);

		private byte value;

		private SculptType(int value) {
			this.value = (byte) value;
		}

		public static SculptType setValue(int value) {
			for (SculptType e : values()) {
				if (e.value == value)
					return e;
			}
			logger.warn(String.format("Unknown SculptType value %x", value));
			return null;
		}

		public byte getValue() {
			return value;
		}

	}

	public enum ObjectCategory {
		Invalid(-1), None(0), Owner(1), Group(2), Other(3), Selected(4), Temporary(5);

		private byte value;

		private ObjectCategory(int value) {
			this.value = (byte) value;
		}

		public static ObjectCategory setValue(int value) {
			for (ObjectCategory e : values()) {
				if (e.value == value)
					return e;
			}
			logger.warn(String.format("Unknown ObjectCategory value %x", value));
			return null;
		}

		public byte getValue() {
			return value;
		}

	}

	/**
	 * Attachment points for objects on avatar bodies
	 *
	 * Both InventoryObject and InventoryAttachment types can be attached
	 *
	 */
	public enum AttachmentPoint {
		/** Right hand if object was not previously attached */
		Default,
		/** Chest */
		Chest,
		/** Skull */
		Skull,
		/** Left shoulder */
		LeftShoulder,
		/** Right shoulder */
		RightShoulder,
		/** Left hand */
		LeftHand,
		/** Right hand */
		RightHand,
		/** Left foot */
		LeftFoot,
		/** Right foot */
		RightFoot,
		/** Spine */
		Spine,
		/** Pelvis */
		Pelvis,
		/** Mouth */
		Mouth,
		/** Chin */
		Chin,
		/** Left ear */
		LeftEar,
		/** Right ear */
		RightEar,
		/** Left eyeball */
		LeftEyeball,
		/** Right eyeball */
		RightEyeball,
		/** Nose */
		Nose,
		/** Right upper arm */
		RightUpperArm,
		/** Right forearm */
		RightForearm,
		/** Left upper arm */
		LeftUpperArm,
		/** Left forearm */
		LeftForearm,
		/** Right hip */
		RightHip,
		/** Right upper leg */
		RightUpperLeg,
		/** Right lower leg */
		RightLowerLeg,
		/** Left hip */
		LeftHip,
		/** Left upper leg */
		LeftUpperLeg,
		/** Left lower leg */
		LeftLowerLeg,
		/** Stomach */
		Stomach,
		/** Left pectoral */
		LeftPec,
		/** Right pectoral */
		RightPec,
		/** HUD Center position 2 */
		HUDCenter2,
		/** HUD Top-right */
		HUDTopRight,
		/** HUD Top */
		HUDTop,
		/** HUD Top-left */
		HUDTopLeft,
		/** HUD Center */
		HUDCenter,
		/** HUD Bottom-left */
		HUDBottomLeft,
		/** HUD Bottom */
		HUDBottom,
		/** HUD Bottom-right */
		HUDBottomRight,
		/** Neck */
		Neck,
		/** Avatar Center */
		Root,
		/** Left Ring Finger */
		LeftHandRing,
		/** Right Ring Finger */
		RightHandRing,
		/** Tail Base */
		TailBase,
		/** Tail Tip */
		TailTip,
		/** Left Wing */
		LeftWing,
		/** Right Wing */
		RightWing,
		/** Jaw */
		Jaw,
		/** Alt Left Ear */
		AltLeftEar,
		/** Alt Right Ear */
		AltRightEar,
		/** Alt Left Eye */
		AltLeftEye,
		/** Alt Right Eye */
		AltRightEye,
		/** Tongue */
		Tongue,
		/** Groin */
		Groin,
		/** Left Hind Foot */
		LeftHindFoot,
		/** Right Hind Foot */
		RightHindFoot;

		private static String[] STRINGS = { "Default", "Chest", "Head", "Left Shoulder", "Right Shoulder", "Left Hand",
				"Right Hand", "Left Foot", "Right Foot", "Back", "Pelvis", "Mouth", "Chin", "Left Ear", "Right Ear",
				"Left Eye", "Right Eye", "Nose", "Right Upper Arm", "Right Lower Arm", "Left Upper Arm",
				"Left Lower Arm", "Right Hip", "Right Upper Leg", "Right Lower Leg", "Left Hip", "Left Upper Leg",
				"Left Lower Leg", "Belly", "Left Pec", "Right Pec", "HUD Center 2", "HUD Top Right", "HUD Top Center",
				"HUD Top Left", "HUD Center 1", "HUD Bottom Left", "HUD Bottom", "HUD Bottom Right", "Neck",
				"Avatar Center", "Left Ring Finger", "Right Ring Finger", "Tail Base", "Tail Tip", "Left Wing",
				"Right Wing", "Jaw", "Alt Left Ear", "Alt Right Ear", "Alt Left Eye", "Alt Right Eye", "Tongue",
				"Groin", "Left Hind Foot", "Right Hind Foot" };

		public static AttachmentPoint setValue(String value) {
			for (int i = 0; i < values().length; i++) {
				if (value.equals(STRINGS[i])) {
					return values()[i];
				}
			}
			return Default;
		}

		public static AttachmentPoint setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return Default;
		}

		public static byte getValue(AttachmentPoint att, boolean replace) {
			return att.getValue(replace);
		}

		public byte getValue(boolean replace) {
			byte value = (byte) ordinal();
			if (!replace)
				value |= 0x80;
			return value;
		}

		public byte getValue() {
			return (byte) ordinal();
		}

		@Override
		public String toString() {
			return toString(this);
		}

		public static String toString(AttachmentPoint point) {
			return STRINGS[point.ordinal()];
		}
	}

	/** Tree foliage types */
	public enum Tree {
		// Pine1 tree
		Pine1,
		// Oak tree
		Oak,
		// Tropical Bush1
		TropicalBush1,
		// Palm1 tree
		Palm1,
		// Dogwood tree
		Dogwood,
		// Tropical Bush2
		TropicalBush2,
		// Palm2 tree
		Palm2,
		// Cypress1 tree
		Cypress1,
		// Cypress2 tree
		Cypress2,
		// Pine2 tree
		Pine2,
		// Plumeria
		Plumeria,
		// Winter pinetree1
		WinterPine1,
		// Winter Aspen tree
		WinterAspen,
		// Winter pinetree2
		WinterPine2,
		// Eucalyptus tree
		Eucalyptus,
		// Fern
		Fern,
		// Eelgrass
		Eelgrass,
		// Sea Sword
		SeaSword,
		// Kelp1 plant
		Kelp1,
		// Beach grass
		BeachGrass1,
		// Kelp2 plant
		Kelp2;

		public static Tree setValue(byte value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	/** Grass foliage types */
	public enum Grass {
		Grass0, Grass1, Grass2, Grass3, Grass4, Undergrowth1;

		public static Grass setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}

	}

	/** Action associated with clicking on an object */
	public enum ClickAction {
		/** Touch object */
		Touch,
		/** Sit on object */
		Sit,
		/** Purchase object or contents */
		Buy,
		/** Pay the object */
		Pay,
		/** Open task inventory */
		OpenTask,
		/** Play parcel media */
		PlayMedia,
		/** Open parcel media */
		OpenMedia;

		public static ClickAction setValue(int value) {
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue() {
			return (byte) ordinal();
		}

	}

	// Parameters used to construct a visual representation of a primitive
	public class ConstructionData {
		public ProfileCurve profileCurve;
		public HoleType profileHole;
		public PathCurve pathCurve;
		public float pathBegin;
		public float pathEnd;
		public float pathRadiusOffset;
		public float pathSkew;
		public float pathScaleX;
		public float pathScaleY;
		public float pathShearX;
		public float pathShearY;
		public float pathTaperX;
		public float pathTaperY;
		public float pathTwist;
		public float pathTwistBegin;
		public float pathRevolutions;
		public float profileBegin;
		public float profileEnd;
		public float profileHollow;

		public Material material;
		public byte state;
		public PCode primCode;

		public ConstructionData() {
			profileCurve = Primitive.ProfileCurve.Square;
			profileBegin = 0f;
			profileEnd = 1f;
			profileHollow = 0f;
			pathCurve = Primitive.PathCurve.Line;
			pathBegin = 0f;
			pathEnd = 1f;
			pathRadiusOffset = 0f;
			pathSkew = 0f;
			pathScaleX = 1f;
			pathScaleY = 1f;
			pathShearX = 0f;
			pathShearY = 0f;
			pathTaperX = 0f;
			pathTaperY = 0f;
			pathTwist = 0f;
			pathTwistBegin = 0f;
			pathRevolutions = 1f;
			/*
			 * ProfileHole = primData.ProfileHole;
			 *
			 *
			 * Material = primData.Material; State = primData.State; PCode = primData.PCode;
			 */
		}

		public ConstructionData(OSD osd) {
			fromOSD(osd);
		}

		public ConstructionData(ConstructionData primData) {
			profileCurve = primData.profileCurve;
			profileHole = primData.profileHole;
			pathCurve = primData.pathCurve;
			pathBegin = primData.pathBegin;
			pathEnd = primData.pathEnd;
			pathRadiusOffset = primData.pathRadiusOffset;
			pathSkew = primData.pathSkew;
			pathScaleX = primData.pathScaleX;
			pathScaleY = primData.pathScaleY;
			pathShearX = primData.pathShearX;
			pathShearY = primData.pathShearY;
			pathTaperX = primData.pathTaperX;
			pathTaperY = primData.pathTaperY;
			pathTwistBegin = primData.pathTwistBegin;
			pathTwist = primData.pathTwist;
			pathRevolutions = primData.pathRevolutions;
			profileBegin = primData.profileBegin;
			profileEnd = primData.profileEnd;
			profileHollow = primData.profileHollow;

			material = primData.material;
			state = primData.state;
			primCode = primData.primCode;
		}

		/**
		 * Setup construction data for a basic primitive shape
		 *
		 * @param type
		 *            Primitive shape to construct
		 * @returns Construction data that can be plugged into a
		 *          <seealso cref="Primitive"/>
		 */
		public ConstructionData(PrimType type) {
			primCode = Primitive.PCode.Prim;
			material = Primitive.Material.Wood;

			switch (type) {
			case Box:
				profileCurve = Primitive.ProfileCurve.Square;
				pathCurve = Primitive.PathCurve.Line;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 1f;
				pathRevolutions = 1f;
				break;
			case Cylinder:
				profileCurve = Primitive.ProfileCurve.Circle;
				pathCurve = Primitive.PathCurve.Line;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 1f;
				pathRevolutions = 1f;
				break;
			case Prism:
				profileCurve = Primitive.ProfileCurve.EquilateralTriangle;
				pathCurve = Primitive.PathCurve.Line;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 0f;
				pathScaleY = 0f;
				pathRevolutions = 1f;
				break;
			case Ring:
				profileCurve = Primitive.ProfileCurve.EquilateralTriangle;
				pathCurve = Primitive.PathCurve.Circle;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 0.25f;
				pathRevolutions = 1f;
				break;
			case Sphere:
				profileCurve = Primitive.ProfileCurve.HalfCircle;
				pathCurve = Primitive.PathCurve.Circle;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 1f;
				pathRevolutions = 1f;
				break;
			case Torus:
				profileCurve = Primitive.ProfileCurve.Circle;
				pathCurve = Primitive.PathCurve.Circle;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 0.25f;
				pathRevolutions = 1f;
				break;
			case Tube:
				profileCurve = Primitive.ProfileCurve.Square;
				pathCurve = Primitive.PathCurve.Circle;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 0.25f;
				pathRevolutions = 1f;
				break;
			case Sculpt:
				profileCurve = Primitive.ProfileCurve.Circle;
				pathCurve = Primitive.PathCurve.Circle;
				profileEnd = 1f;
				pathEnd = 1f;
				pathScaleX = 1f;
				pathScaleY = 0.5f;
				pathRevolutions = 1f;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported shape: " + type.toString());
			}
		}

		// Attachment point to an avatar
		public AttachmentPoint getAttachmentPoint() {
			return AttachmentPoint.values()[Helpers.swapNibbles(state)];
		}

		public void setAttachmentPoint(AttachmentPoint value) {
			state = Helpers.swapNibbles((byte) value.ordinal());
		}

		public byte getProfileValue() {
			return (byte) (profileCurve.getValue() | profileHole.getValue());
		}

		public void setProfileValue(byte value) {
			profileCurve = Primitive.ProfileCurve.setValue(value & 0xF);
			profileHole = HoleType.setValue(value >> 4);
		}

		public ProfileCurve getProfileCurve() {
			return profileCurve;
		}

		public void setProfileCurve(ProfileCurve value) {
			profileCurve = value;
		}

		public HoleType getProfileHole() {
			return profileHole;
		}

		public void setProfileHole(HoleType value) {
			profileHole = value;
		}

		public Vector2 getPathBeginScale() {
			Vector2 begin = new Vector2(1f, 1f);
			if (pathScaleX > 1f)
				begin.x = 2f - pathScaleX;
			if (pathScaleY > 1f)
				begin.y = 2f - pathScaleY;
			return begin;
		}

		public Vector2 getPathEndScale() {
			Vector2 end = new Vector2(1f, 1f);
			if (pathScaleX < 1f)
				end.x = pathScaleX;
			if (pathScaleY < 1f)
				end.y = pathScaleY;
			return end;
		}

		public OSD serialize() {
			OSDMap path = new OSDMap(14);
			path.put("begin", OSD.fromReal(pathBegin));
			path.put("curve", OSD.fromInteger(pathCurve.getValue()));
			path.put("end", OSD.fromReal(pathEnd));
			path.put("radius_offset", OSD.fromReal(pathRadiusOffset));
			path.put("revolutions", OSD.fromReal(pathRevolutions));
			path.put("scale_x", OSD.fromReal(pathScaleX));
			path.put("scale_y", OSD.fromReal(pathScaleY));
			path.put("shear_x", OSD.fromReal(pathShearX));
			path.put("shear_y", OSD.fromReal(pathShearY));
			path.put("skew", OSD.fromReal(pathSkew));
			path.put("taper_x", OSD.fromReal(pathTaperX));
			path.put("taper_y", OSD.fromReal(pathTaperY));
			path.put("twist", OSD.fromReal(pathTwist));
			path.put("twist_begin", OSD.fromReal(pathTwistBegin));

			OSDMap profile = new OSDMap(4);
			profile.put("begin", OSD.fromReal(profileBegin));
			profile.put("curve", OSD.fromInteger(profileCurve.getValue()));
			profile.put("hole", OSD.fromInteger(profileHole.getValue()));
			profile.put("end", OSD.fromReal(profileEnd));
			profile.put("hollow", OSD.fromReal(profileHollow));

			OSDMap volume = new OSDMap(2);
			volume.put("path", path);
			volume.put("profile", profile);

			return volume;
		}

		public void fromOSD(OSD osd) {
			if (osd instanceof OSDMap) {
				OSDMap map = (OSDMap) osd;

				OSDMap volume = (OSDMap) map.get("volume");
				OSDMap path = (OSDMap) volume.get("path");
				OSDMap profile = (OSDMap) volume.get("profile");

				state = 0;
				material = Primitive.Material.setValue(map.get("material").asInteger());
				primCode = Primitive.PCode.Prim; // TODO: Put this in SD

				pathBegin = (float) path.get("begin").asReal();
				pathCurve = Primitive.PathCurve.setValue((byte) path.get("curve").asInteger());
				pathEnd = (float) path.get("end").asReal();
				pathRadiusOffset = (float) path.get("radius_offset").asReal();
				pathRevolutions = (float) path.get("revolutions").asReal();
				pathScaleX = (float) path.get("scale_x").asReal();
				pathScaleY = (float) path.get("scale_y").asReal();
				pathShearX = (float) path.get("shear_x").asReal();
				pathShearY = (float) path.get("shear_y").asReal();
				pathSkew = (float) path.get("skew").asReal();
				pathTaperX = (float) path.get("taper_x").asReal();
				pathTaperY = (float) path.get("taper_y").asReal();
				pathTwist = (float) path.get("twist").asReal();
				pathTwistBegin = (float) path.get("twist_begin").asReal();

				profileBegin = (float) profile.get("begin").asReal();
				profileEnd = (float) profile.get("end").asReal();
				profileHollow = (float) profile.get("hollow").asReal();
				profileCurve = Primitive.ProfileCurve.setValue(profile.get("curve").asInteger());
				profileHole = Primitive.HoleType.setValue(profile.get("hole").asInteger());

			}
		}

		@Override
		public int hashCode() {
			return ((Float) pathBegin).hashCode() ^ ((Float) pathEnd).hashCode() ^ ((Float) pathRadiusOffset).hashCode()
					^ ((Float) pathRevolutions).hashCode() ^ ((Float) pathScaleX).hashCode()
					^ ((Float) pathScaleY).hashCode() ^ ((Float) pathShearX).hashCode()
					^ ((Float) pathShearY).hashCode() ^ ((Float) pathSkew).hashCode() ^ ((Float) pathTaperX).hashCode()
					^ ((Float) pathTaperY).hashCode() ^ ((Float) pathTwist).hashCode()
					^ ((Float) pathTwistBegin).hashCode() ^ ((Float) profileBegin).hashCode()
					^ ((Float) profileEnd).hashCode() ^ ((Float) profileHollow).hashCode() ^ state
					^ (material == null ? 0 : material.hashCode()) ^ (primCode == null ? 0 : primCode.hashCode())
					^ (profileCurve == null ? 0 : profileCurve.hashCode())
					^ (pathCurve == null ? 0 : pathCurve.hashCode());
		}

		@Override
		public boolean equals(Object o) {
			return o != null && o instanceof ConstructionData && equals((ConstructionData) o);
		}

		public boolean equals(ConstructionData o) {
			if (o != null) {
				return pathBegin == o.pathBegin && pathEnd == o.pathEnd && pathRadiusOffset == o.pathRadiusOffset
						&& pathRevolutions == o.pathRevolutions && pathScaleX == o.pathScaleX
						&& pathScaleY == o.pathScaleY && pathShearX == o.pathShearX && pathShearY == o.pathShearY
						&& pathSkew == o.pathSkew && pathTaperX == o.pathTaperX && pathTaperY == o.pathTaperY
						&& pathTwist == o.pathTwist && pathTwistBegin == o.pathTwistBegin
						&& profileBegin == o.profileBegin && profileEnd == o.profileEnd
						&& profileHollow == o.profileHollow && state == o.state
						&& (material == null ? material == o.material : material.equals(o.material))
						&& (primCode == null ? primCode == o.primCode : primCode.equals(o.primCode))
						&& (profileCurve == null ? profileCurve == o.profileCurve : profileCurve.equals(o.profileCurve))
						&& (pathCurve == null ? pathCurve == o.pathCurve : pathCurve.equals(o.pathCurve));
			}
			return false;
		}
	}

	// Information on the flexible properties of a primitive
	public class FlexibleData {
		public int softness;
		public float gravity;
		public float drag;
		public float wind;
		public float tension;
		public Vector3 force;

		// Default constructor
		public FlexibleData() {
			softness = 2;
			gravity = 0.3f;
			drag = 2.0f;
			wind = 0.0f;
			tension = 1.0f;
			force = Vector3.ZERO;
		}

		public FlexibleData(OSD osd) {
			this();
			fromOSD(osd);
		}

		public FlexibleData(byte[] data, int pos, int length) {
			this();
			if (length >= 4 && data.length >= pos + 4) {
				softness = ((data[pos] & 0x80) >> 6) | ((data[pos + 1] & 0x80) >> 7);

				tension = (data[pos++] & 0x7F) / 10.0f;
				drag = (data[pos++] & 0x7F) / 10.0f;
				gravity = data[pos++] / 10.0f;
				wind = data[pos++] / 10.0f;
				if (length >= 16 && data.length >= pos + 12) {
					force = new Vector3(data, pos);
				}
			}
		}

		public FlexibleData(FlexibleData data) {
			softness = data.softness;

			tension = data.tension;
			drag = data.drag;
			gravity = data.gravity;
			wind = data.wind;
			force = new Vector3(data.force);
		}

		public byte[] getBytes() {
			byte[] data = new byte[16];
			int i = 0;
			// Softness is packed in the upper bits of tension and drag
			data[i++] = (byte) (((int) (tension * 10.01f) & 0x7F) | ((softness & 2) << 6));
			data[i++] = (byte) (((int) (drag * 10.01f) & 0x7F) | ((softness & 1) << 7));
			data[i++] = (byte) (gravity * 10.01f);
			data[i++] = (byte) (wind * 10.01f);

			force.toBytes(data, i);

			return data;
		}

		public OSD serialize() {
			OSDMap map = new OSDMap();

			map.put("simulate_lod", OSD.fromInteger(softness));
			map.put("gravity", OSD.fromReal(gravity));
			map.put("air_friction", OSD.fromReal(drag));
			map.put("wind_sensitivity", OSD.fromReal(wind));
			map.put("tension", OSD.fromReal(tension));
			map.put("user_force", OSD.fromVector3(force));

			return map;
		}

		public void fromOSD(OSD osd) {
			if (osd.getType() == OSDType.Map) {
				OSDMap map = (OSDMap) osd;

				softness = map.get("simulate_lod").asInteger();
				gravity = (float) map.get("gravity").asReal();
				drag = (float) map.get("air_friction").asReal();
				wind = (float) map.get("wind_sensitivity").asReal();
				tension = (float) map.get("tension").asReal();
				force = map.get("user_force").asVector3();
			}
		}

		@Override
		public int hashCode() {
			return softness ^ ((Float) gravity).hashCode() ^ ((Float) drag).hashCode() ^ ((Float) wind).hashCode()
					^ ((Float) tension).hashCode() ^ (force == null ? 0 : force.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && (obj instanceof FlexibleData) && equals((FlexibleData) obj);
		}

		public boolean equals(FlexibleData obj) {
			return obj != null && softness == obj.softness && gravity == obj.gravity && drag == obj.drag
					&& wind == obj.wind && tension == obj.tension && force == obj.force;
		}
	}

	// Information on the light properties of a primitive
	public class LightData {
		public Color4 color;
		public float intensity;
		public float radius;
		public float cutoff;
		public float falloff;

		// Default constructor
		public LightData() {
			color = Color4.WHITE;
			radius = 10.0f;
			cutoff = 0.0f;
			falloff = 0.75f;
		}

		public LightData(OSD osd) {
			this();
			fromOSD(osd);
		}

		public LightData(byte[] data, int pos, int length) {
			this();
			if (length >= 16 && data.length >= 16 + pos) {
				color = new Color4(data, pos, false);
				radius = Helpers.bytesToFloatL(data, pos + 4);
				cutoff = Helpers.bytesToFloatL(data, pos + 8);
				falloff = Helpers.bytesToFloatL(data, pos + 12);

				// Alpha in color is actually intensity
				intensity = color.a;
				color.a = 1f;
			}
		}

		public LightData(LightData light) {
			color = new Color4(light.color);
			radius = light.radius;
			cutoff = light.cutoff;
			falloff = light.falloff;
			intensity = light.intensity;
		}

		public byte[] getBytes() {
			byte[] data = new byte[16];

			// Alpha channel in color is intensity
			Color4 tmpColor = color;
			tmpColor.a = intensity;
			tmpColor.toBytes(data, 0);
			Helpers.floatToBytesL(radius, data, 4);
			Helpers.floatToBytesL(cutoff, data, 8);
			Helpers.floatToBytesL(falloff, data, 12);

			return data;
		}

		public OSD serialize() {
			OSDMap map = new OSDMap();

			map.put("color", OSD.fromColor4(color));
			map.put("intensity", OSD.fromReal(intensity));
			map.put("radius", OSD.fromReal(radius));
			map.put("cutoff", OSD.fromReal(cutoff));
			map.put("falloff", OSD.fromReal(falloff));

			return map;
		}

		public void fromOSD(OSD osd) {
			if (osd.getType() == OSDType.Map) {
				OSDMap map = (OSDMap) osd;

				color = map.get("color").asColor4();
				intensity = (float) map.get("intensity").asReal();
				radius = (float) map.get("radius").asReal();
				cutoff = (float) map.get("cutoff").asReal();
				falloff = (float) map.get("falloff").asReal();
			}
		}

		@Override
		public int hashCode() {
			return (color != null ? color.hashCode() : 0) ^ ((Float) intensity).hashCode() ^ ((Float) radius).hashCode()
					^ ((Float) cutoff).hashCode() ^ ((Float) falloff).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && (obj instanceof LightData) && equals((LightData) obj);
		}

		public boolean equals(LightData obj) {
			return obj != null && color == null ? (color == obj.color)
					: (color.equals(obj.color)) && intensity == obj.intensity && radius == obj.radius
							&& cutoff == obj.cutoff && falloff == obj.falloff;
		}

		@Override
		public String toString() {
			return String.format("Color: %s Intensity: %f Radius: %f Cutoff: %f Falloff: %f", color, intensity, radius,
					cutoff, falloff);
		}
	}

	// Information on the sculpt properties of a sculpted primitive
	public class SculptData {
		public UUID sculptTexture;
		private byte type;

		// Default constructor
		public SculptData() {
		}

		public SculptData(OSD osd) {
			fromOSD(osd);
		}

		public SculptData(byte[] data, int pos, int length) {
			if (length >= 17 && data.length >= 17 + pos) {
				sculptTexture = new UUID(data, pos);
				type = data[pos + 16];
			} else {
				sculptTexture = UUID.ZERO;
				type = SculptType.None.getValue();
			}
		}

		public SculptData(SculptData value) {
			sculptTexture = value.sculptTexture;
			this.type = value.getType().getValue();
		}

		public SculptType getType() {
			return SculptType.values()[type & 7];
		}

		public void setType(SculptType value) {
			type = value.getValue();
		}

		public void setType(int value) {
			type = (byte) (value & 0x7);
		}

		// Render inside out (inverts the normals).
		public boolean getInvert() {
			return (type & SculptType.Invert.getValue()) != 0;
		}

		// Render an X axis mirror of the sculpty.
		public boolean getMirror() {
			return (type & SculptType.Mirror.getValue()) != 0;
		}

		public byte[] getBytes() {
			byte[] data = new byte[17];

			sculptTexture.toBytes(data, 0);
			data[16] = type;

			return data;
		}

		public OSD serialize() {
			OSDMap map = new OSDMap();

			map.put("texture", OSD.fromUUID(sculptTexture));
			map.put("type", OSD.fromInteger(type));

			return map;
		}

		public void fromOSD(OSD osd) {
			if (osd.getType() == OSDType.Map) {
				OSDMap map = (OSDMap) osd;

				sculptTexture = map.get("texture").asUUID();
				type = (byte) map.get("type").asInteger();
			}
		}

		@Override
		public int hashCode() {
			return (sculptTexture == null ? 0 : sculptTexture.hashCode()) ^ type;
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && (obj instanceof SculptData) && equals((SculptData) obj);
		}

		public boolean equals(SculptData obj) {
			return obj != null && (sculptTexture == null ? (sculptTexture == obj.sculptTexture)
					: sculptTexture.equals(obj.sculptTexture)) && type == obj.type;
		}
	}

	// Information on the sculpt properties of a sculpted primitive
	public class LightImage {
		public UUID lightTexture;
		public Vector3 params;

		// Default constructor
		public LightImage() {
		}

		public LightImage(OSD osd) {
			fromOSD(osd);
		}

		public LightImage(byte[] data, int pos, int length) {
			if (length >= 28 && data.length >= 28 + pos) {
				lightTexture = new UUID(data, pos);
				params = new Vector3(data, pos + 16, true);
			} else {
				lightTexture = UUID.ZERO;
				params = new Vector3(Helpers.PI_OVER_TWO, 0.f, 0.f);
			}
		}

		public LightImage(LightImage value) {
			lightTexture = value.lightTexture;
			params = value.params;
		}

		public byte[] getBytes() {
			byte[] data = new byte[28];

			lightTexture.toBytes(data, 0);
			params.toBytes(data, 16, true);
			return data;
		}

		public OSD serialize() {
			OSDMap map = new OSDMap();

			map.put("texture", OSD.fromUUID(lightTexture));
			map.put("params", OSD.fromVector3(params));

			return map;
		}

		public void fromOSD(OSD osd) {
			if (osd.getType() == OSDType.Map) {
				OSDMap map = (OSDMap) osd;

				lightTexture = map.get("texture").asUUID();
				params = map.get("params").asVector3();
			}
		}

		public boolean isLightSpotlight() {
			return lightTexture != null && !lightTexture.equals(UUID.ZERO);
		}

		@Override
		public String toString() {
			return String.format("LightTexture: %s Params; %s", lightTexture.toString(), params.toString());

		}

		@Override
		public int hashCode() {
			return (lightTexture == null ? 0 : lightTexture.hashCode()) ^ (params == null ? 0 : params.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && (obj instanceof LightImage) && equals((LightImage) obj);
		}

		public boolean equals(LightImage obj) {
			return obj != null && lightTexture == null ? (lightTexture == obj.lightTexture)
					: (lightTexture.equals(obj.lightTexture)) && params == null ? (params == obj.params)
							: params.equals(obj.params);
		}
	}

	// The Object's UUID, asset server
	public UUID id;

	public UUID groupID;

	// Object ID in Region (sim) it is in
	public int localID;

	public int parentID;
	public long regionHandle;
	public int flags;
	// Location of Object (x,y,z probably)
	public Vector3 position;
	// Rotational Position of Object
	public Quaternion rotation = Quaternion.IDENTITY;
	public Vector3 scale;
	public Vector3 velocity;
	public Vector3 angularVelocity;
	public Vector3 acceleration;
	public Vector4 collisionPlane;
	public FlexibleData flexible;
	public LightData light;
	public LightImage lightMap;
	public SculptData sculpt;
	public ClickAction clickAction;
	public UUID soundID;

	// Identifies the owner if audio or a particle system is active
	public UUID ownerID;
	// Foliage type for this primitive. Only applicable if this primitive is
	// foliage
	public Tree treeSpecies;
	public byte[] scratchPad;
	public byte soundFlags;
	public float soundGain;
	public float soundRadius;
	public String text;
	public Color4 textColor;
	public String mediaURL;
	public JointType joint;
	public Vector3 jointPivot;
	public Vector3 jointAxisOrAnchor;
	public NameValue[] nameValues;
	public ConstructionData primData;
	public ObjectProperties properties;
	public PhysicsProperties physicsProps;

	public boolean isAttachment;

	public TextureEntry textures;
	public TextureAnimation textureAnim;

	public ParticleSystem particleSys;

	// Current version of the media data for the prim
	public String mediaVersion = Helpers.EmptyString;

	// Array of media entries indexed by face number
	public MediaEntry[] faceMedia;

	// Default constructor
	public Primitive() {
		// Default a few null property values to String.Empty
		text = Helpers.EmptyString;
		mediaURL = Helpers.EmptyString;
	}

	public Primitive(OSD osd) {
		this();
		fromOSD(osd);
	}

	public Primitive(Primitive prim) {
		id = new UUID(prim.id);
		groupID = new UUID(prim.groupID);
		localID = prim.localID;
		parentID = prim.parentID;
		regionHandle = prim.regionHandle;
		flags = prim.flags;
		treeSpecies = prim.treeSpecies;
		if (prim.scratchPad != null) {
			scratchPad = new byte[prim.scratchPad.length];
			System.arraycopy(prim.scratchPad, 0, scratchPad, 0, scratchPad.length);
		} else
			scratchPad = Helpers.EmptyBytes;
		position = new Vector3(prim.position);
		scale = prim.scale;
		rotation = new Quaternion(prim.rotation);
		velocity = new Vector3(prim.velocity);
		angularVelocity = new Vector3(prim.angularVelocity);
		acceleration = new Vector3(prim.acceleration);
		collisionPlane = new Vector4(prim.collisionPlane);
		flexible = new FlexibleData(prim.flexible);
		light = new LightData(prim.light);
		sculpt = new SculptData(prim.sculpt);
		clickAction = prim.clickAction;
		soundID = new UUID(prim.soundID);
		ownerID = new UUID(prim.ownerID);
		soundFlags = prim.soundFlags;
		soundGain = prim.soundGain;
		soundRadius = prim.soundRadius;
		text = new String(prim.text);
		textColor = new Color4(prim.textColor);
		mediaURL = prim.mediaURL;
		joint = prim.joint;
		jointPivot = prim.jointPivot;
		jointAxisOrAnchor = prim.jointAxisOrAnchor;
		if (prim.nameValues != null) {
			if (nameValues == null || nameValues.length != prim.nameValues.length)
				nameValues = new NameValue[prim.nameValues.length];
			for (int i = 0; i < prim.nameValues.length; i++)
				nameValues[i] = prim.nameValues[i];
		} else
			nameValues = null;
		primData = new ConstructionData(prim.primData);
		properties = new ObjectProperties(prim.properties);
		textures = new TextureEntry(prim.textures);
		textureAnim = textures.new TextureAnimation(prim.textureAnim);
		particleSys = new ParticleSystem(prim.particleSys);
	}

	// Uses basic heuristics to estimate the primitive shape
	public PrimType getType() {
		if (sculpt != null && sculpt.getType() != SculptType.None && !sculpt.sculptTexture.equals(UUID.ZERO)) {
			if (sculpt.getType() == SculptType.Mesh)
				return PrimType.Mesh;
			return PrimType.Sculpt;
		}

		boolean linearPath = primData.pathCurve == PathCurve.Line || primData.pathCurve == PathCurve.Flexible;
		float scaleY = primData.pathScaleY;

		if (linearPath) {
			switch (primData.profileCurve) {
			case Circle:
				return PrimType.Cylinder;
			case Square:
				return PrimType.Box;
			case IsometricTriangle:
			case EquilateralTriangle:
			case RightTriangle:
				return PrimType.Prism;
			case HalfCircle:
			default:
				return PrimType.Unknown;
			}
		}

		switch (primData.pathCurve) {
		case Flexible:
			return PrimType.Unknown;
		case Circle:
			switch (primData.profileCurve) {
			case Circle:
				if (scaleY > 0.75f)
					return PrimType.Sphere;
				return PrimType.Torus;
			case HalfCircle:
				return PrimType.Sphere;
			case EquilateralTriangle:
				return PrimType.Ring;
			case Square:
				if (scaleY <= 0.75f)
					return PrimType.Tube;
			default:
				return PrimType.Unknown;
			}
		case Circle2:
			if (primData.profileCurve == ProfileCurve.Circle)
				return PrimType.Sphere;
		default:
			return PrimType.Unknown;
		}
	}

	public OSD serialize() {

		OSDMap prim = new OSDMap(9);
		if (properties != null) {
			prim.put("name", OSD.fromString(properties.name));
			prim.put("description", OSD.fromString(properties.description));
		} else {
			prim.put("name", OSD.fromString("Object"));
			prim.put("description", OSD.fromString(Helpers.EmptyString));
		}

		prim.put("phantom", OSD.fromBoolean((flags & PrimFlags.Phantom) != 0));
		prim.put("physical", OSD.fromBoolean((flags & PrimFlags.Physics) != 0));
		prim.put("position", OSD.fromVector3(position));
		prim.put("rotation", OSD.fromQuaternion(rotation));
		prim.put("scale", OSD.fromVector3(scale));
		prim.put("material", OSD.fromInteger(primData.material.getValue()));
		prim.put("shadows", OSD.fromBoolean((flags & PrimFlags.CastShadows) != 0));
		prim.put("parentid", OSD.fromInteger(parentID));

		prim.put("volume", primData.serialize());

		if (textures != null)
			prim.put("textures", textures.serialize());

		if ((textureAnim.flags & TextureAnimMode.ANIM_ON) != 0)
			prim.put("texture_anim", textureAnim.serialize());

		if (light != null)
			prim.put("light", light.serialize());

		if (lightMap != null)
			prim.put("light_image", lightMap.serialize());

		if (flexible != null)
			prim.put("flex", flexible.serialize());

		if (sculpt != null)
			prim.put("sculpt", sculpt.serialize());

		return prim;
	}

	public void fromOSD(OSD osd) {
		if (osd instanceof OSDMap) {
			OSDMap map = (OSDMap) osd;

			if (map.get("phantom").asBoolean())
				flags = PrimFlags.Phantom;

			if (map.get("physical").asBoolean())
				flags |= PrimFlags.Physics;

			if (map.get("shadows").asBoolean())
				flags |= PrimFlags.CastShadows;

			parentID = map.get("parentid").asInteger();
			position = map.get("position").asVector3();
			rotation = map.get("rotation").asQuaternion();
			scale = map.get("scale").asVector3();

			primData = new ConstructionData(map.get("volume"));
			flexible = new FlexibleData(map.get("flex"));
			light = new LightData(map.get("light"));
			lightMap = new LightImage(map.get("light_image"));

			if (map.containsKey("sculpt"))
				sculpt = new SculptData(map.get("sculpt"));

			textures = new TextureEntry(map.get("textures"));

			if (map.containsKey("texture_anim"))
				textureAnim = textures.new TextureAnimation(map.get("texture_anim"));

			properties = new ObjectProperties();

			String s;
			s = map.get("name").asString();
			if (s != null && !s.isEmpty()) {
				properties.name = s;
			}
			s = map.get("description").asString();
			if (s != null && !s.isEmpty()) {
				properties.description = s;
			}
		}
	}

	public int setExtraParamsFromBytes(byte[] data, int pos) {
		int i = pos;
		int totalLength = 1;

		if (data.length == 0 || pos >= data.length)
			return 0;

		byte extraParamCount = data[i++];

		for (int k = 0; k < extraParamCount; k++) {
			ExtraParamType type = ExtraParamType.setValue(Helpers.bytesToUInt16L(data, i));
			i += 2;

			int paramLength = (int) Helpers.bytesToUInt32L(data, i);
			i += 4;

			switch (type) {
			case Flexible:
				flexible = new FlexibleData(data, i, paramLength);
				break;
			case Light:
				light = new LightData(data, i, paramLength);
				break;
			case LightImage:
				lightMap = new LightImage(data, i, paramLength);
				break;
			case Sculpt:
			case Mesh:
				sculpt = new SculptData(data, i, paramLength);
				break;
			default:
				break;
			}
			i += paramLength;
			totalLength += paramLength + 6;
		}
		return totalLength;
	}

	public byte[] getExtraParamsBytes() throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		byte buffer[];
		byte count = 0;

		data.write(0);
		if (flexible != null) {
			data.write(Helpers.uint16ToBytesL(ExtraParamType.Flexible.getValue()));
			buffer = flexible.getBytes();
			data.write(Helpers.uint32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}

		if (light != null) {
			data.write(Helpers.uint16ToBytesL(ExtraParamType.Flexible.getValue()));
			buffer = light.getBytes();
			data.write(Helpers.uint32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}

		if (lightMap != null) {
			data.write(Helpers.uint16ToBytesL(ExtraParamType.LightImage.getValue()));
			buffer = lightMap.getBytes();
			data.write(Helpers.uint32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}

		if (sculpt != null) {
			if (sculpt.getType() == SculptType.Mesh) {
				data.write(Helpers.uint16ToBytesL(ExtraParamType.Mesh.getValue()));
			} else {
				data.write(Helpers.uint16ToBytesL(ExtraParamType.Sculpt.getValue()));
			}
			buffer = sculpt.getBytes();
			data.write(Helpers.uint32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}

		buffer = data.toByteArray();
		buffer[0] = count;
		return buffer;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Primitive) ? equals(this, (Primitive) obj) : false;
	}

	public boolean equals(Primitive other) {
		return equals(this, other);
	}

	public static boolean equals(Primitive lhs, Primitive rhs) {
		if (lhs == null || rhs == null) {
			return rhs == lhs;
		}
		return lhs.id == rhs.id;
	}

	@Override
	public String toString() {
		switch (primData.primCode) {
		case Prim:
			return String.format("%s (%s)", getType().toString(), id.toString());
		default:
			return String.format("%s (%s)", primData.primCode.toString(), id.toString());
		}
	}

	@Override
	public int hashCode() {
		return position.hashCode() ^ velocity.hashCode() ^ acceleration.hashCode() ^ rotation.hashCode()
				^ angularVelocity.hashCode() ^ clickAction.hashCode() ^ (flexible != null ? flexible.hashCode() : 0)
				^ (light != null ? light.hashCode() : 0) ^ (sculpt != null ? sculpt.hashCode() : 0) ^ flags
				^ (mediaURL != null ? mediaURL.hashCode() : 0) ^ (ownerID != null ? ownerID.hashCode() : 0) ^ parentID
				^ (primData != null ? primData.hashCode() : 0) ^ (particleSys != null ? particleSys.hashCode() : 0)
				^ (textColor != null ? textColor.hashCode() : 0) ^ (textureAnim != null ? textureAnim.hashCode() : 0)
				^ (textures != null ? textures.hashCode() : 0) ^ (int) soundRadius
				^ (scale != null ? scale.hashCode() : 0) ^ soundID.hashCode() ^ text.hashCode()
				^ treeSpecies.hashCode();
	}

	public static short packBeginCut(float beginCut) {
		return (short) Helpers.roundFromZero(beginCut / CUT_QUANTA);
	}

	public static short packEndCut(float endCut) {
		return (short) (50000 - Helpers.roundFromZero(endCut / CUT_QUANTA));
	}

	public static byte packPathScale(float pathScale) {
		return (byte) (200 - Helpers.roundFromZero(pathScale / SCALE_QUANTA));
	}

	public static byte packPathShear(float pathShear) {
		return (byte) Helpers.roundFromZero(pathShear / SHEAR_QUANTA);
	}

	/**
	 * Packs PathTwist, PathTwistBegin, PathRadiusOffset, and PathSkew parameters in
	 * to signed eight bit values
	 *
	 * @param pathTwist
	 *            Floating point parameter to pack
	 * @return Signed eight bit value containing the packed parameter
	 */
	public static byte packPathTwist(float pathTwist) {
		return (byte) Helpers.roundFromZero(pathTwist / SCALE_QUANTA);
	}

	public static byte packPathTaper(float pathTaper) {
		return (byte) Helpers.roundFromZero(pathTaper / TAPER_QUANTA);
	}

	public static byte packPathRevolutions(float pathRevolutions) {
		return (byte) Helpers.roundFromZero((pathRevolutions - 1f) / REV_QUANTA);
	}

	public static short packProfileHollow(float profileHollow) {
		return (short) Helpers.roundFromZero(profileHollow / HOLLOW_QUANTA);
	}

	public static float unpackBeginCut(short beginCut) {
		return beginCut * CUT_QUANTA;
	}

	public static float unpackEndCut(short endCut) {
		return (50000 - endCut) * CUT_QUANTA;
	}

	public static float unpackPathScale(byte pathScale) {
		return (200 - pathScale) * SCALE_QUANTA;
	}

	public static float unpackPathShear(byte pathShear) {
		return pathShear * SHEAR_QUANTA;
	}

	/**
	 * Unpacks PathTwist, PathTwistBegin, PathRadiusOffset, and PathSkew parameters
	 * from signed eight bit integers to floating point values
	 *
	 * @param pathTwist
	 *            Signed eight bit value to unpack
	 * @return Unpacked floating point value
	 */
	public static float unpackPathTwist(byte pathTwist) {
		return pathTwist * SCALE_QUANTA;
	}

	public static float unpackPathTaper(byte pathTaper) {
		return pathTaper * TAPER_QUANTA;
	}

	public static float unpackPathRevolutions(byte pathRevolutions) {
		return pathRevolutions * REV_QUANTA + 1f;
	}

	public static float unpackProfileHollow(short profileHollow) {
		return profileHollow * HOLLOW_QUANTA;
	}
}
