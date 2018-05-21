/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
package libomv.character;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import libomv.types.Vector3;
import libomv.utils.Helpers;

public class KeyFrameMotion {
	public static int KEYFRAME_MOTION_VERSION = 1;
	public static int KEYFRAME_MOTION_SUBVERSION = 0;

	public static float MAX_PELVIS_OFFSET = 5f;

	// Poses set in the animation metadata for the hands
	public enum HandPose {
		Spread, Relaxed, Point_Both, Fist, Relaxed_Left, Point_Left, Fist_Left, Relaxed_Right, Point_Right, Fist_Right, Salute_Right, Typing, Peace_Right
	}

	// A Joint and it's associated meta data and keyframes
	public class Joint {
		// Name of the Joint. Matches the avatar_skeleton.xml in client distros
		public String name;

		// Joint Animation Override? Was the same as the Priority in testing..
		public int priority;

		// Array of Rotation Keyframes in order from earliest to latest
		public JointKey[] rotationkeys;

		// Array of Position Keyframes in order from earliest to latest
		// This seems to only be for the Pelvis?
		public JointKey[] positionkeys;

		// Custom application data that can be attached to a joint
		public Object tag;

		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof Joint && equals((Joint) obj);
		}

		public boolean equals(Joint other) {
			if (other != null) {
				return name == null ? name == other.name
						: name.equals(other.name) && priority == other.priority && tag == null ? tag == other.tag
								: tag.equals(other.tag) && Arrays.equals(rotationkeys, other.rotationkeys)
										&& Arrays.equals(positionkeys, other.positionkeys);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (name == null ? 0 : name.hashCode()) ^ priority ^ (tag == null ? 0 : tag.hashCode())
					^ Arrays.hashCode(rotationkeys) ^ Arrays.hashCode(positionkeys);
		}
	}

	// A Joint Keyframe. This is either a position or a rotation.
	public class JointKey {
		// Time in seconds for this keyframe.
		public float time;

		// Either a Vector3 position or a Vector3 Euler rotation
		public Vector3 keyElement;

		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof JointKey && equals((JointKey) obj);
		}

		public boolean equals(JointKey other) {
			if (other != null) {
				return time == other.time && keyElement == null ? keyElement == other.keyElement
						: keyElement.equals(other.keyElement);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return ((Float) time).hashCode() ^ (keyElement == null ? 0 : keyElement.hashCode());
		}
	}

	enum EConstraintType {
		CONSTRAINT_TYPE_POINT, CONSTRAINT_TYPE_PLANE, NUM_CONSTRAINT_TYPES
	}

	enum EConstraintTargetType {
		CONSTRAINT_TARGET_TYPE_BODY, CONSTRAINT_TARGET_TYPE_GROUND, NUM_CONSTRAINT_TARGET_TYPES
	}

	public class Constraint {
		String sourceJointName;
		String targetJointName;
		int chainLength;
		Vector3 sourceOffset;
		Vector3 targetOffset;
		Vector3 targetDir;
		float easeInStart;
		float easeInStop;
		float easeOutStart;
		float easeOutStop;
		EConstraintType constraintType;

		@Override
		public boolean equals(Object obj) {
			return obj != null && equals((Constraint) obj);
		}

		public boolean equals(Constraint other) {
			if (other != null) {
				return chainLength == other.chainLength && easeInStart == other.easeInStart
						&& easeInStop == other.easeInStop && easeOutStart == other.easeOutStart
						&& easeOutStop == other.easeOutStop && sourceJointName == null
								? sourceJointName == other.sourceJointName
								: sourceJointName.equals(other.sourceJointName) && targetJointName == null
										? targetJointName == other.targetJointName
										: targetJointName.equals(other.targetJointName) && sourceOffset == null
												? sourceOffset == other.sourceOffset
												: sourceOffset.equals(other.sourceOffset) && targetOffset == null
														? targetOffset == other.targetOffset
														: targetOffset.equals(other.targetOffset) && targetDir == null
																? targetDir == other.targetDir
																: targetDir.equals(other.targetDir);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return chainLength ^ ((Float) easeInStart).hashCode() ^ ((Float) easeInStop).hashCode()
					^ ((Float) easeOutStart).hashCode() ^ ((Float) easeOutStop).hashCode()
					^ (sourceJointName == null ? 0 : sourceJointName.hashCode())
					^ (targetJointName == null ? 0 : targetJointName.hashCode())
					^ (targetDir == null ? 0 : targetDir.hashCode())
					^ (sourceOffset == null ? 0 : sourceOffset.hashCode())
					^ (targetOffset != null ? 0 : targetOffset.hashCode());
		}
	}

	public int version; // Always 1
	public int subVersion; // Always 0

	// Animation Priority
	public int priority;

	// The animation length in seconds.
	public float length;

	// Expression set in the client. Null if [None] is selected
	public String expressionName; // "" (null)

	// The time in seconds to start the animation
	public float inPoint;

	// The time in seconds to end the animation
	public float outPoint;

	// Loop the animation
	public boolean loop;

	// Meta data. Ease in Seconds.
	public float easeInTime;

	// Meta data. Ease out seconds.
	public float easeOutTime;

	// Meta Data for the Hand Pose
	public int handPose;

	// Number of joints defined in the animation
	// public int JointCount;

	// Contains an array of joints
	public Joint[] joints;

	// public int ConstraintCount;
	public Constraint[] constraints;

	// Custom application data that can be attached to a joint
	public Object tag;

	public KeyFrameMotion() {
		version = KEYFRAME_MOTION_VERSION;
		subVersion = KEYFRAME_MOTION_SUBVERSION;
	}

	/**
	 * Serialize an animation asset binary data into it's joints/keyframes/meta data
	 *
	 * @param animationdata
	 *            The asset binary data containing the animation
	 */
	public KeyFrameMotion(byte[] animationdata) {
		int i = 0;
		int jointCount;
		int constraintCount;

		version = Helpers.bytesToUInt16L(animationdata, i);
		i += 2; // Always 1
		subVersion = Helpers.bytesToUInt16L(animationdata, i);
		i += 2; // Always 0
		priority = Helpers.bytesToInt32L(animationdata, i);
		i += 4;
		length = Helpers.bytesToFloatL(animationdata, i);
		i += 4;

		expressionName = readBytesUntilNull(animationdata, i, -1);
		i += expressionName.length() + 1;

		inPoint = Helpers.bytesToFloatL(animationdata, i);
		i += 4;
		outPoint = Helpers.bytesToFloatL(animationdata, i);
		i += 4;
		loop = (Helpers.bytesToInt32L(animationdata, i) != 0);
		i += 4;
		easeInTime = Helpers.bytesToFloatL(animationdata, i);
		i += 4;
		easeOutTime = Helpers.bytesToFloatL(animationdata, i);
		i += 4;
		handPose = (int) Helpers.bytesToUInt32L(animationdata, i);
		i += 4; // Handpose

		jointCount = (int) Helpers.bytesToUInt32L(animationdata, i);
		i += 4; // Get Joint count
		joints = new Joint[jointCount];

		// deserialize the number of joints in the animation.
		// Joints are variable length blocks of binary data consisting of joint data and
		// keyframes
		for (int j = 0; j < jointCount; j++) {
			Joint joint = new Joint();
			i = readJoint(animationdata, i, joint);
			joints[j] = joint;
		}

		// Read possible constraint records if available
		if (i < animationdata.length + 4) {
			constraintCount = (int) Helpers.bytesToUInt32L(animationdata, i);
			i += 4;
			constraints = new Constraint[constraintCount];
			for (int j = 0; j < constraintCount; i++) {
				Constraint constraint = new Constraint();
				i = readConstraint(animationdata, i, constraint);
				constraints[j] = constraint;
			}
		}
	}

	/**
	 * Variable length strings seem to be null terminated in the animation asset..
	 * use with caution, home grown.
	 *
	 * @param data
	 *            The animation asset byte array
	 * @param i
	 *            The offset to start reading
	 * @returns a string
	 */
	public String readBytesUntilNull(byte[] data, int i, int max) {
		int startpos = i;

		if (max < i || max > data.length)
			max = data.length;

		// Find the null character
		for (; i < max; i++) {
			if (data[i] == 0) {
				break;
			}
		}

		// We found the end of the string
		// convert the bytes from the beginning of the string to the end of the string
		try {
			return Helpers.bytesToString(data, startpos, i - startpos);
		} catch (UnsupportedEncodingException e) {
			return Helpers.EmptyString;
		}
	}

	/**
	 * Read in a Joint from an animation asset byte array
	 *
	 * @param data
	 *            animation asset byte array
	 * @param i
	 *            Byte Offset of the start of the joint
	 * @param pJoint
	 *            The Joint structure to serialized the data into
	 * @return Byte Offset after the end of the joint
	 */
	public int readJoint(byte[] data, int i, Joint pJoint) {
		// Joint name
		pJoint.name = readBytesUntilNull(data, i, -1);
		i += pJoint.name.length() + 1;

		// Priority Revisited
		pJoint.priority = Helpers.bytesToInt32L(data, i);
		i += 4; // Joint Priority override?

		// Read in rotation keyframes
		pJoint.rotationkeys = readKeys(data, i, -1.0f, 1.0f);
		i += 4 + (pJoint.rotationkeys != null ? pJoint.rotationkeys.length * 8 : 0);

		// Read in position keyframes
		pJoint.positionkeys = readKeys(data, i, -0.5f, 1.5f);
		i += 4 + (pJoint.rotationkeys != null ? pJoint.positionkeys.length * 8 : 0);
		;

		return i;
	}

	/**
	 * Read Keyframes of a certain type
	 *
	 * @param data
	 *            Animation Byte array
	 * @param i
	 *            Offset in the Byte Array
	 * @param min
	 *            Scaling Min to pass to the Uint16ToFloat method
	 * @param max
	 *            Scaling Max to pass to the Uint16ToFloat method
	 * @return an array of JointKey records
	 */
	public JointKey[] readKeys(byte[] data, int i, float min, float max) {
		float x;
		float y;
		float z;

		// int32: number of key frames
		int keycount = Helpers.bytesToInt32L(data, i);
		i += 4; // How many rotation keyframes

		// Sanity check how many position keys there are
		if (keycount > 0 && keycount * 8 <= data.length - i) {
			// ... n Keyframe data blocks
			// Read in keyframes
			JointKey[] m_keys = new JointKey[keycount];
			for (int j = 0; j < keycount; j++) {
				JointKey pJKey = new JointKey();

				pJKey.time = Helpers.uint16ToFloatL(data, i, inPoint, outPoint);
				i += 2;
				x = Helpers.uint16ToFloatL(data, i, min, max);
				i += 2;
				y = Helpers.uint16ToFloatL(data, i, min, max);
				i += 2;
				z = Helpers.uint16ToFloatL(data, i, min, max);
				i += 2;
				pJKey.keyElement = new Vector3(x, y, z);
				m_keys[j] = pJKey;
			}
			return m_keys;
		}
		return null;
	}

	public int readConstraint(byte[] data, int i, Constraint constraint) {
		constraint.chainLength = data[i++];
		constraint.constraintType = EConstraintType.values()[data[i++]];
		constraint.sourceJointName = readBytesUntilNull(data, i, i += 16);
		constraint.sourceOffset = new Vector3(data, i);
		i += 12;
		constraint.targetJointName = readBytesUntilNull(data, i, i += 16);
		constraint.targetOffset = new Vector3(data, i);
		i += 12;
		constraint.easeInStart = Helpers.bytesToFloatL(data, i);
		i += 4;
		constraint.easeInStop = Helpers.bytesToFloatL(data, i);
		i += 4;
		constraint.easeOutStart = Helpers.bytesToFloatL(data, i);
		i += 4;
		constraint.easeOutStop = Helpers.bytesToFloatL(data, i);
		i += 4;
		return i;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof KeyFrameMotion && equals((KeyFrameMotion) obj);
	}

	public boolean equals(KeyFrameMotion other) {
		return other != null && version == other.version && subVersion == other.subVersion && loop == other.loop
				&& inPoint == other.inPoint && outPoint == other.outPoint && length == other.length
				&& handPose == other.handPose && easeInTime == other.easeInTime && easeOutTime == other.easeOutTime
				&& priority == other.priority && Arrays.equals(joints, other.joints)
				&& Arrays.equals(constraints, other.constraints);
	}

	@Override
	public int hashCode() {
		return version ^ subVersion ^ (loop ? 1 : 0) ^ ((Float) inPoint).hashCode() ^ ((Float) outPoint).hashCode()
				^ ((Float) easeInTime).hashCode() ^ ((Float) easeOutTime).hashCode() ^ ((Float) length).hashCode()
				^ handPose ^ priority ^ Arrays.hashCode(joints) ^ Arrays.hashCode(constraints);
	}
}
