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
package libomv.primitives;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

// Complete structure for the particle system
public class ParticleSystem
{
	// Particle source pattern
	public static class SourcePattern
	{
		// None
		public static final byte None = 0;
		// Drop particles from source position with no force
		public static final byte Drop = 0x01;
		// "Explode" particles in all directions
		public static final byte Explode = 0x02;
		// Particles shoot across a 2D area
		public static final byte Angle = 0x04;
		// Particles shoot across a 3D Cone
		public static final byte AngleCone = 0x08;
		// Inverse of AngleCone (shoot particles everywhere except the 3D cone
		// defined
		public static final byte AngleConeEmpty = 0x10;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static byte _mask = 0x1F;
	}

	// Particle Data Flags
	// [Flags]
	public static class ParticleDataFlags
	{
		// None
		public static final int None = 0;
		// Interpolate color and alpha from start to end
		public static final int InterpColor = 0x001;
		// Interpolate scale from start to end
		public static final int InterpScale = 0x002;
		// Bounce particles off particle sources Z height
		public static final int Bounce = 0x004;
		// velocity of particles is dampened toward the simulators wind
		public static final int Wind = 0x008;
		// Particles follow the source
		public static final int FollowSrc = 0x010;
		// Particles point towards the direction of source's velocity
		public static final int FollowVelocity = 0x020;
		// Target of the particles
		public static final int TargetPos = 0x040;
		// Particles are sent in a straight line
		public static final int TargetLinear = 0x080;
		// Particles emit a glow
		public static final int Emissive = 0x100;
		// used for point/grab/touch
		public static final int Beam = 0x200;
		// continuous ribbon particle</summary>
		public static final int Ribbon = 0x400;
		// particle data contains glow</summary>
		public static final int DataGlow = 0x10000;
		// particle data contains blend functions</summary>
		public static final int DataBlend = 0x20000;

		public static int setValue(int value)
		{
			return (value & _mask);
		}

		public static int getValue(int value)
		{
			return (value & _mask);
		}

		private static final int _mask = 0x3FF;
	}

	// Particle Flags Enum
	// [Flags]
	public static class ParticleFlags
	{
		// None
		public static final byte None = 0;
		// Acceleration and velocity for particles are relative to the object
		// rotation
		public static final byte ObjectRelative = 0x01;
		// Particles use new 'correct' angle parameters
		public static final byte UseNewAngle = 0x02;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static final byte _mask = 3;
	}

	public enum BlendFunc
	{
	    One,
	    Zero,
	    DestColor,
	    SourceColor,
	    OneMinusDestColor,
	    OneMinusSourceColor,
	    DestAlpha,
	    SourceAlpha,
	    OneMinusDestAlpha,
	    OneMinusSourceAlpha;
	}

	public int CRC;
	// Particle Flags
	// There appears to be more data packed in to this area
	// for many particle systems. It doesn't appear to be flag values
	// and serialization breaks unless there is a flag for every
	// possible bit so it is left as an unsigned integer
	public int PartFlags;
	// {@link T:SourcePattern} pattern of particles
	public byte Pattern;
	// A <see langword="float"/> representing the maximimum age (in seconds)
	// particle will be displayed
	// Maximum value is 30 seconds
	public float MaxAge;
	// A <see langword="float"/> representing the number of seconds,
	// from when the particle source comes into view,
	// or the particle system's creation, that the object will emits particles;
	// after this time period no more particles are emitted
	public float StartAge;
	// A <see langword="float"/> in radians that specifies where particles will
	// not be created
	public float InnerAngle;
	// A <see langword="float"/> in radians that specifies where particles will
	// be created
	public float OuterAngle;
	// A <see langword="float"/> representing the number of seconds between
	// burts.
	public float BurstRate;
	// A <see langword="float"/> representing the number of meters
	// around the center of the source where particles will be created.
	public float BurstRadius;
	// A <see langword="float"/> representing in seconds, the minimum speed
	// between bursts of new
	// particles being emitted
	public float BurstSpeedMin;
	// A <see langword="float"/> representing in seconds the maximum speed of
	// new particles being emitted.
	public float BurstSpeedMax;
	// A <see langword="byte"/> representing the maximum number of particles
	// emitted per burst
	public byte BurstPartCount;
	// A <see cref="T:Vector3"/> which represents the velocity (speed) from the
	// source which particles are emitted
	public Vector3 AngularVelocity;
	// A <see cref="T:Vector3"/> which represents the Acceleration from the
	// source which particles are emitted
	public Vector3 PartAcceleration;
	// The <see cref="T:UUID"/> Key of the texture displayed on the particle
	public UUID Texture;
	// The <see cref="T:UUID"/> Key of the specified target object or avatar
	// particles will follow
	public UUID Target;
	// Flags of particle from {@link T:ParticleDataFlags}
	public int PartDataFlags;
	// Max Age particle system will emit particles for
	public float PartMaxAge;
	// The <see cref="T:Color4"/> the particle has at the beginning of its
	// lifecycle
	public Color4 PartStartColor;
	// The <see cref="T:Color4"/> the particle has at the ending of its
	// lifecycle
	public Color4 PartEndColor;
	// A <see langword="float"/> that represents the starting X size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartStartScaleX;
	// A <see langword="float"/> that represents the starting Y size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartStartScaleY;
	// A <see langword="float"/> that represents the ending X size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartEndScaleX;
	// A <see langword="float"/> that represents the ending Y size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartEndScaleY;
	// A <see langword="float"/> that represents the start glow value
	// Minimum value is 0, maximum value is 1
	public float PartStartGlow;
	// A <see langword="float"/> that represents the end glow value
	// Minimum value is 0, maximum value is 1
	public float PartEndGlow;

	// OpenGL blend function to use at particle source
	public byte BlendFuncSource;
	// OpenGL blend function to use at particle destination
	public byte BlendFuncDest;

	public final byte MaxDataBlockSize = 98;
	public final byte LegacyDataBlockSize = 86;
	public final byte SysDataSize = 68;
	public final byte PartDataSize = 18;

	// Can this particle system be packed in a legacy compatible way
	// True if the particle system doesn't use new particle system features
	public boolean IsLegacyCompatible()
	{
	    return !HasGlow() && !HasBlendFunc();
	}

	public boolean HasGlow()
	{
	    return PartStartGlow > 0f || PartEndGlow > 0f;
	}

	public boolean HasBlendFunc()
	{
	    return BlendFuncSource != BlendFunc.SourceAlpha.ordinal() || BlendFuncDest != BlendFunc.OneMinusSourceAlpha.ordinal();
	}

	public ParticleSystem()
	{
		init();
	}

	public ParticleSystem(OSD osd)
	{
		fromOSD(osd);
	}

	/**
	 * Decodes a byte[] array into a ParticleSystem Object
	 * 
	 * @param data ParticleSystem object
	 * @param pos Start position for BitPacker
	 */
	public ParticleSystem(byte[] bytes, int pos)
	{
		PartStartGlow = 0f;
		PartEndGlow = 0f;
		BlendFuncSource = (byte)BlendFunc.SourceAlpha.ordinal();
		BlendFuncDest = (byte)BlendFunc.OneMinusSourceAlpha.ordinal();

		CRC = PartFlags = 0;
		Pattern = SourcePattern.None;
		MaxAge = StartAge = InnerAngle = OuterAngle = BurstRate = BurstRadius = BurstSpeedMin =

		BurstSpeedMax = 0.0f;
		BurstPartCount = 0;
		AngularVelocity = PartAcceleration = Vector3.Zero;
		Texture = Target = UUID.Zero;
		PartDataFlags = ParticleDataFlags.None;
		PartMaxAge = 0.0f;
		PartStartColor = PartEndColor = Color4.Black;
		PartStartScaleX = PartStartScaleY = PartEndScaleX = PartEndScaleY = 0.0f;

		int size = bytes.length - pos;

		if (size == LegacyDataBlockSize)
        {
			pos += unpackSystem(bytes, pos);
			pos += unpackLegacyData(bytes, pos);
        }
		else if (size > LegacyDataBlockSize && size <= MaxDataBlockSize)
		{
			int sysSize = Helpers.BytesToInt32L(bytes, pos);
			pos += 4;
			if (sysSize != SysDataSize) return; // unkown particle system data size
			pos += unpackSystem(bytes, pos);
			int dataSize =  Helpers.BytesToInt32L(bytes, pos);
			pos += 4;
			if (dataSize != PartDataSize) return; // unkown particle data size
			pos += unpackLegacyData(bytes, pos);

			if ((PartDataFlags & ParticleDataFlags.DataGlow) == ParticleDataFlags.DataGlow)
			{
			    if (bytes.length - pos < 2) return;
			    PartStartGlow = bytes[pos++] / 255f;
			    PartEndGlow = bytes[pos++] / 255f;
			}

		    if ((PartDataFlags & ParticleDataFlags.DataBlend) == ParticleDataFlags.DataBlend)
			{
			    if (bytes.length - pos < 2) return;
			    BlendFuncSource = bytes[pos++];
			    BlendFuncDest = bytes[pos++];
			}
		}
	}
	
	
	private int unpackSystem(byte[] bytes, int pos)
	{
			CRC = (int) Helpers.BytesToUInt32L(bytes, pos);
			pos += 4;
			PartFlags = ParticleFlags.setValue((int) Helpers.BytesToUInt32L(bytes, pos));
			pos += 4;
			Pattern = SourcePattern.setValue(bytes[pos++]);
			MaxAge = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
			StartAge = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
			InnerAngle = Helpers.BytesToFixedL(bytes, pos++, false, 3, 5);
			OuterAngle = Helpers.BytesToFixedL(bytes, pos++, false, 3, 5);
			BurstRate = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			if (BurstRate < 0.01f) BurstRate = 0.01f;
			pos += 2;
			BurstRadius = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
			BurstSpeedMin = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
			BurstSpeedMax = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
			BurstPartCount = bytes[pos++];
			float x = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			float y = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			float z = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			AngularVelocity = new Vector3(x, y, z);
			x = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			y = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			z = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			PartAcceleration = new Vector3(x, y, z);
			Texture = new UUID(bytes, pos);
			pos += 16;
			Target = new UUID(bytes, pos);
			pos += 16;
			return pos;
	}
			
    private int unpackLegacyData(byte[] bytes, int pos)
    {
    	PartDataFlags = ParticleDataFlags.setValue((int) Helpers.BytesToUInt32L(bytes, pos));
		pos += 4;
		PartMaxAge = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		PartStartColor = new Color4(bytes, pos, false);
		pos += 4;
		PartEndColor = new Color4(bytes, pos, false);
		pos += 4;
		PartStartScaleX = Helpers.BytesToFixedL(bytes, pos++, false, 3, 5);
		PartStartScaleY = Helpers.BytesToFixedL(bytes, pos++, false, 3, 5);
		PartEndScaleX = Helpers.BytesToFixedL(bytes, pos++, false, 3, 5);
		PartEndScaleY = Helpers.BytesToFixedL(bytes, pos++, false, 3, 5);
		return pos;
    }

	public ParticleSystem(ParticleSystem particleSys)
	{
		CRC = particleSys.CRC;
		PartFlags = particleSys.PartFlags;
		Pattern = particleSys.Pattern;
		MaxAge = particleSys.MaxAge;
		StartAge = particleSys.StartAge;
		InnerAngle = particleSys.InnerAngle;
		OuterAngle = particleSys.OuterAngle;
		BurstRate = particleSys.BurstRate;
		BurstRadius = particleSys.BurstRadius;
		BurstSpeedMin = particleSys.BurstSpeedMin;
		BurstSpeedMax = particleSys.BurstSpeedMax;
		BurstPartCount = particleSys.BurstPartCount;
		AngularVelocity = new Vector3(particleSys.AngularVelocity);
		PartAcceleration = new Vector3(particleSys.PartAcceleration);
		Texture = particleSys.Texture;
		Target = particleSys.Target;
		PartDataFlags = particleSys.PartDataFlags;
		PartMaxAge = particleSys.PartMaxAge;
		PartStartColor = new Color4(particleSys.PartStartColor);
		PartEndColor = new Color4(particleSys.PartEndColor);
		PartStartScaleX = particleSys.PartStartScaleX;
		PartStartScaleY = particleSys.PartStartScaleY;
		PartEndScaleX = particleSys.PartEndScaleX;
		PartEndScaleY = particleSys.PartEndScaleY;
	}

	private void init()
	{
		CRC = 0;
		PartFlags = ParticleFlags.None;
		Pattern = SourcePattern.None;
		MaxAge = StartAge = InnerAngle = OuterAngle = BurstRate = BurstRadius = BurstSpeedMin = BurstSpeedMax = 0.0f;
		BurstPartCount = 0;
		AngularVelocity = PartAcceleration = Vector3.Zero;
		Texture = Target = UUID.Zero;
		PartDataFlags = ParticleDataFlags.None;
		PartMaxAge = 0.0f;
		PartStartColor = PartEndColor = Color4.Black;
		PartStartScaleX = PartStartScaleY = PartEndScaleX = PartEndScaleY = 0.0f;
	}

	/**
	 * Generate byte[] array from particle data
	 * 
	 * @return Byte array
	 */
	public byte[] getBytes()
	{
		int pos = 0;
		int size = LegacyDataBlockSize;

		if (!IsLegacyCompatible())
			size += 8; // two new ints for size
		if (HasGlow())
			size += 2; // two bytes for start and end glow
		if (HasBlendFunc())
			size += 2; // two bytes for start and end blend function

        byte[] bytes = new byte[size];
        if (IsLegacyCompatible())
       	{
       	    pos += packSystemBytes(bytes, pos);
       	    pos += packLegacyData(bytes, pos);
        }
        else
        {
        	pos += Helpers.UInt32ToBytesL(SysDataSize, bytes, pos);
        	pos += packSystemBytes(bytes, pos);
        	int partSize = PartDataSize;
        	if (HasGlow())
        	{
        		partSize += 2; // two bytes for start and end glow
        		PartDataFlags |= ParticleDataFlags.DataGlow;
        	}
        	if (HasBlendFunc())
        	{
        		partSize += 2; // two bytes for start end end blend function
        		PartDataFlags |= ParticleDataFlags.DataBlend;
        	}
        	pos += Helpers.UInt32ToBytesL(partSize, bytes, pos);
        	pos += packLegacyData(bytes, pos);

        	if (HasGlow())
        	{
        		bytes[pos++] = Helpers.FloatToByte(PartStartGlow, 0.0f, 1.0f);
        		bytes[pos++] = Helpers.FloatToByte(PartEndGlow, 0.0f, 1.0f);
        	}

        	if (HasBlendFunc())
        	{
        		bytes[pos++] = Helpers.FloatToByte(BlendFuncSource, 0.0f, 1.0f);
        		bytes[pos++] = Helpers.FloatToByte(BlendFuncDest, 0.0f, 1.0f);
        	}
        }
        return bytes;
    }

	private int packSystemBytes(byte[] bytes, int pos)
    {
		pos += Helpers.UInt32ToBytesL(CRC, bytes, pos);
		pos += Helpers.UInt32ToBytesL(PartFlags, bytes, pos);
		bytes[pos++] = Pattern;
		pos += Helpers.FixedToBytesL(bytes, pos, MaxAge, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, StartAge, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, InnerAngle, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, OuterAngle, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, BurstRate, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, BurstRadius, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, BurstSpeedMin, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, BurstSpeedMax, false, 8, 8);
		bytes[pos++] = BurstPartCount;
		pos += Helpers.FixedToBytesL(bytes, pos, AngularVelocity.X, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, AngularVelocity.Y, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, AngularVelocity.Z, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, PartAcceleration.X, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, PartAcceleration.Y, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, PartAcceleration.Z, true, 8, 7);
		pos += Texture.toBytes(bytes, pos);
		pos += Target.toBytes(bytes, pos);
		return pos;
    }
	
	private int packLegacyData(byte[] bytes, int pos)
    {
		pos += Helpers.UInt32ToBytesL(PartDataFlags, bytes, pos);
		pos += Helpers.FixedToBytesL(bytes, pos, PartMaxAge, false, 8, 8);
		pos += PartStartColor.toBytes(bytes, pos);
		pos += PartEndColor.toBytes(bytes, pos);
		pos += Helpers.FixedToBytesL(bytes, pos, PartStartScaleX, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, PartStartScaleY, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, PartEndScaleX, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, PartEndScaleY, false, 3, 5);
		return pos;
	}

	public OSD serialize()
	{
		OSDMap map = new OSDMap();

		map.put("crc", OSD.FromInteger(CRC));
		map.put("part_flags", OSD.FromInteger(PartFlags));
		map.put("pattern", OSD.FromInteger(Pattern));
		map.put("max_age", OSD.FromReal(MaxAge));
		map.put("start_age", OSD.FromReal(StartAge));
		map.put("inner_angle", OSD.FromReal(InnerAngle));
		map.put("outer_angle", OSD.FromReal(OuterAngle));
		map.put("burst_rate", OSD.FromReal(BurstRate));
		map.put("burst_radius", OSD.FromReal(BurstRadius));
		map.put("burst_speed_min", OSD.FromReal(BurstSpeedMin));
		map.put("burst_speed_max", OSD.FromReal(BurstSpeedMax));
		map.put("burst_part_count", OSD.FromInteger(BurstPartCount));
		map.put("ang_velocity", OSD.FromVector3(AngularVelocity));
		map.put("part_acceleration", OSD.FromVector3(PartAcceleration));
		map.put("texture", OSD.FromUUID(Texture));
		map.put("target", OSD.FromUUID(Target));

		map.put("part_data_flags", OSD.FromInteger(PartDataFlags));
		map.put("part_max_age", OSD.FromReal(PartMaxAge));
		map.put("part_start_color", OSD.FromColor4(PartStartColor));
		map.put("part_end_color", OSD.FromColor4(PartEndColor));
		map.put("part_start_scale", OSD.FromVector3(new Vector3(PartStartScaleX, PartStartScaleY, 0f)));
		map.put("part_end_scale", OSD.FromVector3(new Vector3(PartEndScaleX, PartEndScaleY, 0f)));

		if (HasGlow())
		{
		    map.put("part_start_glow", OSD.FromReal(PartStartGlow));
		    map.put("part_end_glow", OSD.FromReal(PartEndGlow));
		}

		if (HasBlendFunc())
		{
		    map.put("blendfunc_source", OSD.FromInteger(BlendFuncSource));
		    map.put("blendfunc_dest", OSD.FromInteger(BlendFuncDest));
		}
		return map;
	}

	public void fromOSD(OSD osd)
	{
		if (osd instanceof OSDMap)
		{
			OSDMap map = (OSDMap) osd;

			CRC = map.get("crc").AsUInteger();
			PartFlags = map.get("part_flags").AsUInteger();
			Pattern = SourcePattern.setValue(map.get("pattern").AsInteger());
			MaxAge = (float) map.get("max_age").AsReal();
			StartAge = (float) map.get("start_age").AsReal();
			InnerAngle = (float) map.get("inner_angle").AsReal();
			OuterAngle = (float) map.get("outer_angle").AsReal();
			BurstRate = (float) map.get("burst_rate").AsReal();
			BurstRadius = (float) map.get("burst_radius").AsReal();
			BurstSpeedMin = (float) map.get("burst_speed_min").AsReal();
			BurstSpeedMax = (float) map.get("burst_speed_max").AsReal();
			BurstPartCount = (byte) map.get("burst_part_count").AsInteger();
			AngularVelocity = map.get("ang_velocity").AsVector3();
			PartAcceleration = map.get("part_acceleration").AsVector3();
			Texture = map.get("texture").AsUUID();
			Target = map.get("target").AsUUID();

			PartDataFlags = ParticleDataFlags.setValue(map.get("part_data_flags").AsUInteger());
			PartMaxAge = (float) map.get("part_max_age").AsReal();
			PartStartColor = map.get("part_start_color").AsColor4();
			PartEndColor = map.get("part_end_color").AsColor4();

			Vector3 ss = map.get("part_start_scale").AsVector3();
			PartStartScaleX = ss.X;
			PartStartScaleY = ss.Y;

			Vector3 es = map.get("part_end_scale").AsVector3();
			PartEndScaleX = es.X;
			PartEndScaleY = es.Y;

			if (map.containsKey("part_start_glow"))
			{
				PartStartGlow = (float)map.get("part_start_glow").AsReal();
				PartEndGlow = (float)map.get("part_end_glow").AsReal();
			}

			if (map.containsKey("blendfunc_source"))
			{
			    BlendFuncSource = (byte)map.get("blendfunc_source").AsUInteger();
			    BlendFuncDest = (byte)map.get("blendfunc_dest").AsUInteger();
			}
		}
		else
		{
			init();
		}
	}
}
