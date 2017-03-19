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

public class KeyFrameMotion
{
	public static int KEYFRAME_MOTION_VERSION = 1;
	public static int KEYFRAME_MOTION_SUBVERSION = 0;
	
	public static float MAX_PELVIS_OFFSET = 5f;

	// Poses set in the animation metadata for the hands
    public enum HandPose
    {
        Spread,
        Relaxed,
        Point_Both,
        Fist,
        Relaxed_Left,
        Point_Left,
        Fist_Left,
        Relaxed_Right,
        Point_Right,
        Fist_Right,
        Salute_Right,
        Typing,
        Peace_Right
    }

    // A Joint and it's associated meta data and keyframes
    public class Joint
    {
        // Name of the Joint. Matches the avatar_skeleton.xml in client distros
        public String Name;

        // Joint Animation Override?   Was the same as the Priority in testing.. 
        public int Priority;

        // Array of Rotation Keyframes in order from earliest to latest
        public JointKey[] rotationkeys;

        // Array of Position Keyframes in order from earliest to latest
        // This seems to only be for the Pelvis?
        public JointKey[] positionkeys;

        // Custom application data that can be attached to a joint
        public Object Tag;

        @Override
		public boolean equals(Object obj)
		{
			return obj != null && obj instanceof Joint && equals((Joint)obj);
		}
		
		public boolean equals(Joint other)
		{
			if (other != null)
			{
				return Name == null ? Name == other.Name : Name.equals(other.Name) && Priority == other.Priority && 
					Tag == null ? Tag == other.Tag : Tag.equals(other.Tag) &&
					Arrays.equals(rotationkeys, other.rotationkeys) && Arrays.equals(positionkeys, other.positionkeys);
			}
			return false;	
		}

		@Override
		public int hashCode()
		{
			return  (Name == null ? 0 : Name.hashCode()) ^ Priority ^ (Tag == null ? 0 : Tag.hashCode()) ^
					Arrays.hashCode(rotationkeys) ^ Arrays.hashCode(positionkeys);
		}
    }

    // A Joint Keyframe.  This is either a position or a rotation.
    public class JointKey
    {
        // Time in seconds for this keyframe.
        public float time;

        // Either a Vector3 position or a Vector3 Euler rotation
        public Vector3 keyElement;

        @Override
		public boolean equals(Object obj)
		{
			return obj != null && obj instanceof JointKey && equals((JointKey)obj);
		}
		
		public boolean equals(JointKey other)
		{
			if (other != null)
			{
				return time == other.time && keyElement == null ? keyElement == other.keyElement : keyElement.equals(other.keyElement);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return ((Float)time).hashCode() ^ (keyElement == null ? 0 : keyElement.hashCode());
		}
    }

	enum EConstraintType
	{
		CONSTRAINT_TYPE_POINT,
		CONSTRAINT_TYPE_PLANE,
		NUM_CONSTRAINT_TYPES
	}

	enum EConstraintTargetType
	{
		CONSTRAINT_TARGET_TYPE_BODY,
		CONSTRAINT_TARGET_TYPE_GROUND,
		NUM_CONSTRAINT_TARGET_TYPES
	}
	
	public class Constraint
	{
		String		SourceJointName;
		String		TargetJointName;
		int			ChainLength;
		Vector3		SourceOffset;
		Vector3		TargetOffset;
		Vector3		TargetDir;
		float		EaseInStart;
		float		EaseInStop;
		float		EaseOutStart;
		float		EaseOutStop;
		EConstraintType ConstraintType;
		
		@Override
		public boolean equals(Object obj)
		{
			return obj != null && equals((Constraint)obj);
		}
		
		public boolean equals(Constraint other)
		{
			if (other != null)
			{
				return ChainLength == other.ChainLength && EaseInStart == other.EaseInStart && EaseInStop == other.EaseInStop && 
					EaseOutStart == other.EaseOutStart && EaseOutStop == other.EaseOutStop &&
					SourceJointName == null ? SourceJointName == other.SourceJointName : SourceJointName.equals(other.SourceJointName) &&
					TargetJointName == null ? TargetJointName == other.TargetJointName : TargetJointName.equals(other.TargetJointName) &&
					SourceOffset == null ? SourceOffset == other.SourceOffset : SourceOffset.equals(other.SourceOffset) &&
					TargetOffset == null ? TargetOffset == other.TargetOffset : TargetOffset.equals(other.TargetOffset) &&
					TargetDir == null ? TargetDir == other.TargetDir : TargetDir.equals(other.TargetDir);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return ChainLength ^ ((Float)EaseInStart).hashCode() ^ ((Float)EaseInStop).hashCode() ^ 
			    ((Float)EaseOutStart).hashCode() ^ ((Float)EaseOutStop).hashCode() ^
				(SourceJointName == null ? 0 : SourceJointName.hashCode()) ^
				(TargetJointName == null ? 0 : TargetJointName.hashCode()) ^
				(TargetDir == null ? 0 : TargetDir.hashCode()) ^
				(SourceOffset == null ? 0 : SourceOffset.hashCode()) ^
				(TargetOffset != null ? 0 : TargetOffset.hashCode());
		}
	}

	public int version; // Always 1
    public int sub_version; // Always 0

    // Animation Priority
    public int Priority;

    // The animation length in seconds.
    public float Length;

    // Expression set in the client.  Null if [None] is selected
    public String ExpressionName; // "" (null)

    // The time in seconds to start the animation
    public float InPoint;

    // The time in seconds to end the animation
    public float OutPoint;

    // Loop the animation
    public boolean Loop;

    // Meta data. Ease in Seconds.
    public float EaseInTime;

    // Meta data. Ease out seconds.
    public float EaseOutTime;

    // Meta Data for the Hand Pose
    public int HandPose;

    // Number of joints defined in the animation
//    public int JointCount;

    // Contains an array of joints
    public Joint[] Joints;

//    public int ConstraintCount;
	public Constraint[] Constraints;

	// Custom application data that can be attached to a joint
    public Object Tag;

    public KeyFrameMotion()
    {
		version = KEYFRAME_MOTION_VERSION;
		sub_version = KEYFRAME_MOTION_SUBVERSION;   	
    }
    
    /**
     * Serialize an animation asset binary data into it's joints/keyframes/meta data
     * 
     * @param animationdata The asset binary data containing the animation
     */
    public KeyFrameMotion(byte[] animationdata)
    {
    	int i = 0, jointCount, constraintCount;
    	
        version = Helpers.BytesToUInt16L(animationdata, i); i += 2; // Always 1
        sub_version = Helpers.BytesToUInt16L(animationdata, i); i += 2; // Always 0
        Priority = Helpers.BytesToInt32L(animationdata, i); i += 4;
        Length = Helpers.BytesToFloatL(animationdata, i); i += 4;

        ExpressionName = ReadBytesUntilNull(animationdata, i, -1);
        i += ExpressionName.length() + 1;

        InPoint = Helpers.BytesToFloatL(animationdata, i); i += 4;
        OutPoint = Helpers.BytesToFloatL(animationdata, i); i += 4;
        Loop = (Helpers.BytesToInt32L(animationdata, i) != 0); i += 4;
        EaseInTime = Helpers.BytesToFloatL(animationdata, i); i += 4;
        EaseOutTime = Helpers.BytesToFloatL(animationdata, i); i += 4;
        HandPose = (int)Helpers.BytesToUInt32L(animationdata, i); i += 4; // Handpose

        jointCount = (int)Helpers.BytesToUInt32L(animationdata, i); i += 4; // Get Joint count
        Joints = new Joint[jointCount];

        // deserialize the number of joints in the animation.
        // Joints are variable length blocks of binary data consisting of joint data and keyframes
        for (int j = 0; j < jointCount; j++)
        {
            Joint joint = new Joint();
            i = readJoint(animationdata, i, joint);
            Joints[j] = joint;
        }

        // Read possible constraint records if available
        if (i < animationdata.length + 4)
        {
        	constraintCount = (int)Helpers.BytesToUInt32L(animationdata, i); i += 4;
        	Constraints = new Constraint[constraintCount];
        	for (int j = 0; j < constraintCount; i++)
        	{
        		Constraint constraint = new Constraint();
        		i = readConstraint(animationdata, i, constraint);
        		Constraints[j] = constraint;
        	}
        }
    }

    /**
     * Variable length strings seem to be null terminated in the animation asset.. 
	 * use with caution, home grown.
	 * 
     * @param data The animation asset byte array
     * @param i The offset to start reading
     * @returns a string
     */
    public String ReadBytesUntilNull(byte[] data, int i, int max)
    {
        int startpos = i;
        
        if (max < i || max > data.length)
        	max = data.length;
        
        // Find the null character
        for (; i < max; i++)
        {
            if (data[i] == 0)
            {
                break;
            }
        }

		// We found the end of the string
		// convert the bytes from the beginning of the string to the end of the string
		try
		{
			return Helpers.BytesToString(data, startpos, i - startpos);
		}
		catch (UnsupportedEncodingException e)
		{
			return Helpers.EmptyString;
		}
    }

    /**
     * Read in a Joint from an animation asset byte array
     *
     * @param data animation asset byte array
     * @param i Byte Offset of the start of the joint
     * @param pJoint The Joint structure to serialized the data into
     * @return Byte Offset after the end of the joint
     */
    public int readJoint(byte[] data, int i, Joint pJoint)
    {
        // Joint name
        pJoint.Name = ReadBytesUntilNull(data, i, -1);
        i += pJoint.Name.length() + 1;

        // Priority Revisited
        pJoint.Priority = Helpers.BytesToInt32L(data, i); i += 4; // Joint Priority override?

        // Read in rotation keyframes
        pJoint.rotationkeys = readKeys(data, i, -1.0f, 1.0f);
        i += 4 + (pJoint.rotationkeys != null ? pJoint.rotationkeys.length * 8 : 0);

        // Read in position keyframes
        pJoint.positionkeys = readKeys(data, i, -0.5f, 1.5f);
        i += 4 + (pJoint.rotationkeys != null ? pJoint.positionkeys.length * 8 : 0);;

        return i;
    }

    /**
     * Read Keyframes of a certain type
     *
     * @param data Animation Byte array
     * @param i Offset in the Byte Array
     * @param min Scaling Min to pass to the Uint16ToFloat method
     * @param max Scaling Max to pass to the Uint16ToFloat method
     * @return an array of JointKey records
     */
    public JointKey[] readKeys(byte[] data, int i, float min, float max)
    {
        float x, y, z;

        // int32: number of key frames 
        int keycount = Helpers.BytesToInt32L(data, i); i += 4; // How many rotation keyframes
        
        // Sanity check how many position keys there are
        if (keycount > 0 && keycount * 8 <= data.length - i)
        {
        	// ... n Keyframe data blocks
        	// Read in keyframes
        	JointKey[] m_keys = new JointKey[keycount];
        	for (int j = 0; j < keycount; j++)
        	{
        		JointKey pJKey = new JointKey();
            
        		pJKey.time = Helpers.UInt16ToFloatL(data, i, InPoint, OutPoint); i += 2;
        		x = Helpers.UInt16ToFloatL(data, i, min, max); i += 2;
        		y = Helpers.UInt16ToFloatL(data, i, min, max); i += 2;
        		z = Helpers.UInt16ToFloatL(data, i, min, max); i += 2;
        		pJKey.keyElement = new Vector3(x, y, z);
        		m_keys[j] = pJKey;
        	}
            return m_keys;
        }
        return null;
    }

    public int readConstraint(byte[] data, int i, Constraint constraint)
    {
    	constraint.ChainLength = data[i++];
    	constraint.ConstraintType = EConstraintType.values()[data[i++]];
    	constraint.SourceJointName = ReadBytesUntilNull(data, i, i += 16);
    	constraint.SourceOffset = new Vector3(data, i); i += 12;
    	constraint.TargetJointName = ReadBytesUntilNull(data, i, i += 16);
    	constraint.TargetOffset = new Vector3(data, i); i += 12;
    	constraint.EaseInStart = Helpers.BytesToFloatL(data, i); i += 4;
    	constraint.EaseInStop = Helpers.BytesToFloatL(data, i); i += 4;
    	constraint.EaseOutStart = Helpers.BytesToFloatL(data, i); i += 4;
    	constraint.EaseOutStop = Helpers.BytesToFloatL(data, i); i += 4;
    	return i;
    }
    
    @Override
	public boolean equals(Object obj)
    {
    	return obj != null && obj instanceof KeyFrameMotion && equals((KeyFrameMotion)obj);
    }
    
    public boolean equals(KeyFrameMotion other)
    {
    	return other != null && version == other.version && sub_version == other.sub_version && Loop == other.Loop &&
    		InPoint == other.InPoint && OutPoint == other.OutPoint && Length == other.Length && HandPose == other.HandPose && 
    		EaseInTime == other.EaseInTime && EaseOutTime == other.EaseOutTime && Priority == other.Priority && 
    		Arrays.equals(Joints, other.Joints) && Arrays.equals(Constraints, other.Constraints);
    }

	@Override
	public int hashCode()
	{
		return version ^ sub_version ^ (Loop ? 1 : 0) ^ ((Float)InPoint).hashCode() ^ ((Float)OutPoint).hashCode() ^
				((Float)EaseInTime).hashCode() ^ ((Float)EaseOutTime).hashCode() ^ ((Float)Length).hashCode() ^ 
				HandPose ^ Priority ^ Arrays.hashCode(Joints) ^ Arrays.hashCode(Constraints);
	}
}
