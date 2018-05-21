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
public class ParticleSystem {
	// Particle source pattern
	public static class SourcePattern {
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

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static byte _mask = 0x1F;
	}

	// Particle Data Flags
	// [Flags]
	public static class ParticleDataFlags {
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

		public static int setValue(int value) {
			return (value & _mask);
		}

		public static int getValue(int value) {
			return (value & _mask);
		}

		private static final int _mask = 0x3FF;
	}

	// Particle Flags Enum
	// [Flags]
	public static class ParticleFlags {
		// None
		public static final byte None = 0;
		// Acceleration and velocity for particles are relative to the object
		// rotation
		public static final byte ObjectRelative = 0x01;
		// Particles use new 'correct' angle parameters
		public static final byte UseNewAngle = 0x02;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = 3;
	}

	public enum BlendFunc {
		One, Zero, DestColor, SourceColor, OneMinusDestColor, OneMinusSourceColor, DestAlpha, SourceAlpha, OneMinusDestAlpha, OneMinusSourceAlpha;
	}

	public int crc;
	// Particle Flags
	// There appears to be more data packed in to this area
	// for many particle systems. It doesn't appear to be flag values
	// and serialization breaks unless there is a flag for every
	// possible bit so it is left as an unsigned integer
	public int partFlags;
	// {@link T:SourcePattern} pattern of particles
	public byte pattern;
	// A <see langword="float"/> representing the maximimum age (in seconds)
	// particle will be displayed
	// Maximum value is 30 seconds
	public float maxAge;
	// A <see langword="float"/> representing the number of seconds,
	// from when the particle source comes into view,
	// or the particle system's creation, that the object will emits particles;
	// after this time period no more particles are emitted
	public float startAge;
	// A <see langword="float"/> in radians that specifies where particles will
	// not be created
	public float innerAngle;
	// A <see langword="float"/> in radians that specifies where particles will
	// be created
	public float outerAngle;
	// A <see langword="float"/> representing the number of seconds between
	// burts.
	public float burstRate;
	// A <see langword="float"/> representing the number of meters
	// around the center of the source where particles will be created.
	public float burstRadius;
	// A <see langword="float"/> representing in seconds, the minimum speed
	// between bursts of new
	// particles being emitted
	public float burstSpeedMin;
	// A <see langword="float"/> representing in seconds the maximum speed of
	// new particles being emitted.
	public float burstSpeedMax;
	// A <see langword="byte"/> representing the maximum number of particles
	// emitted per burst
	public byte burstPartCount;
	// A <see cref="T:Vector3"/> which represents the velocity (speed) from the
	// source which particles are emitted
	public Vector3 angularVelocity;
	// A <see cref="T:Vector3"/> which represents the Acceleration from the
	// source which particles are emitted
	public Vector3 partAcceleration;
	// The <see cref="T:UUID"/> Key of the texture displayed on the particle
	public UUID texture;
	// The <see cref="T:UUID"/> Key of the specified target object or avatar
	// particles will follow
	public UUID target;
	// Flags of particle from {@link T:ParticleDataFlags}
	public int partDataFlags;
	// Max Age particle system will emit particles for
	public float partMaxAge;
	// The <see cref="T:Color4"/> the particle has at the beginning of its
	// lifecycle
	public Color4 partStartColor;
	// The <see cref="T:Color4"/> the particle has at the ending of its
	// lifecycle
	public Color4 partEndColor;
	// A <see langword="float"/> that represents the starting X size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float partStartScaleX;
	// A <see langword="float"/> that represents the starting Y size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float partStartScaleY;
	// A <see langword="float"/> that represents the ending X size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float partEndScaleX;
	// A <see langword="float"/> that represents the ending Y size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float partEndScaleY;
	// A <see langword="float"/> that represents the start glow value
	// Minimum value is 0, maximum value is 1
	public float partStartGlow;
	// A <see langword="float"/> that represents the end glow value
	// Minimum value is 0, maximum value is 1
	public float partEndGlow;

	// OpenGL blend function to use at particle source
	public byte blendFuncSource;
	// OpenGL blend function to use at particle destination
	public byte blendFuncDest;

	public final byte maxDataBlockSize = 98;
	public final byte legacyDataBlockSize = 86;
	public final byte sysDataSize = 68;
	public final byte partDataSize = 18;

	// Can this particle system be packed in a legacy compatible way
	// True if the particle system doesn't use new particle system features
	public boolean isLegacyCompatible() {
		return !hasGlow() && !hasBlendFunc();
	}

	public boolean hasGlow() {
		return partStartGlow > 0f || partEndGlow > 0f;
	}

	public boolean hasBlendFunc() {
		return blendFuncSource != BlendFunc.SourceAlpha.ordinal()
				|| blendFuncDest != BlendFunc.OneMinusSourceAlpha.ordinal();
	}

	public ParticleSystem() {
		init();
	}

	public ParticleSystem(OSD osd) {
		fromOSD(osd);
	}

	/**
	 * Decodes a byte[] array into a ParticleSystem Object
	 *
	 * @param data
	 *            ParticleSystem object
	 * @param pos
	 *            Start position for BitPacker
	 */
	public ParticleSystem(byte[] bytes, int pos) {
		partStartGlow = 0f;
		partEndGlow = 0f;
		blendFuncSource = (byte) BlendFunc.SourceAlpha.ordinal();
		blendFuncDest = (byte) BlendFunc.OneMinusSourceAlpha.ordinal();

		crc = partFlags = 0;
		pattern = SourcePattern.None;
		maxAge = startAge = innerAngle = outerAngle = burstRate = burstRadius = burstSpeedMin =

				burstSpeedMax = 0.0f;
		burstPartCount = 0;
		angularVelocity = partAcceleration = Vector3.ZERO;
		texture = target = UUID.ZERO;
		partDataFlags = ParticleDataFlags.None;
		partMaxAge = 0.0f;
		partStartColor = partEndColor = Color4.BLACK;
		partStartScaleX = partStartScaleY = partEndScaleX = partEndScaleY = 0.0f;

		int size = bytes.length - pos;

		if (size == legacyDataBlockSize) {
			pos += unpackSystem(bytes, pos);
			pos += unpackLegacyData(bytes, pos);
		} else if (size > legacyDataBlockSize && size <= maxDataBlockSize) {
			int sysSize = Helpers.bytesToInt32L(bytes, pos);
			pos += 4;
			if (sysSize != sysDataSize)
				return; // unkown particle system data size
			pos += unpackSystem(bytes, pos);
			int dataSize = Helpers.bytesToInt32L(bytes, pos);
			pos += 4;
			if (dataSize != partDataSize)
				return; // unkown particle data size
			pos += unpackLegacyData(bytes, pos);

			if ((partDataFlags & ParticleDataFlags.DataGlow) == ParticleDataFlags.DataGlow) {
				if (bytes.length - pos < 2)
					return;
				partStartGlow = bytes[pos++] / 255f;
				partEndGlow = bytes[pos++] / 255f;
			}

			if ((partDataFlags & ParticleDataFlags.DataBlend) == ParticleDataFlags.DataBlend) {
				if (bytes.length - pos < 2)
					return;
				blendFuncSource = bytes[pos++];
				blendFuncDest = bytes[pos++];
			}
		}
	}

	private int unpackSystem(byte[] bytes, int pos) {
		crc = (int) Helpers.bytesToUInt32L(bytes, pos);
		pos += 4;
		partFlags = ParticleFlags.setValue((int) Helpers.bytesToUInt32L(bytes, pos));
		pos += 4;
		pattern = SourcePattern.setValue(bytes[pos++]);
		maxAge = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		startAge = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		innerAngle = Helpers.bytesToFixedL(bytes, pos++, false, 3, 5);
		outerAngle = Helpers.bytesToFixedL(bytes, pos++, false, 3, 5);
		burstRate = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		if (burstRate < 0.01f)
			burstRate = 0.01f;
		pos += 2;
		burstRadius = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		burstSpeedMin = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		burstSpeedMax = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		burstPartCount = bytes[pos++];
		float x = Helpers.bytesToFixedL(bytes, pos, true, 8, 7);
		pos += 2;
		float y = Helpers.bytesToFixedL(bytes, pos, true, 8, 7);
		pos += 2;
		float z = Helpers.bytesToFixedL(bytes, pos, true, 8, 7);
		pos += 2;
		angularVelocity = new Vector3(x, y, z);
		x = Helpers.bytesToFixedL(bytes, pos, true, 8, 7);
		pos += 2;
		y = Helpers.bytesToFixedL(bytes, pos, true, 8, 7);
		pos += 2;
		z = Helpers.bytesToFixedL(bytes, pos, true, 8, 7);
		pos += 2;
		partAcceleration = new Vector3(x, y, z);
		texture = new UUID(bytes, pos);
		pos += 16;
		target = new UUID(bytes, pos);
		pos += 16;
		return pos;
	}

	private int unpackLegacyData(byte[] bytes, int pos) {
		partDataFlags = ParticleDataFlags.setValue((int) Helpers.bytesToUInt32L(bytes, pos));
		pos += 4;
		partMaxAge = Helpers.bytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		partStartColor = new Color4(bytes, pos, false);
		pos += 4;
		partEndColor = new Color4(bytes, pos, false);
		pos += 4;
		partStartScaleX = Helpers.bytesToFixedL(bytes, pos++, false, 3, 5);
		partStartScaleY = Helpers.bytesToFixedL(bytes, pos++, false, 3, 5);
		partEndScaleX = Helpers.bytesToFixedL(bytes, pos++, false, 3, 5);
		partEndScaleY = Helpers.bytesToFixedL(bytes, pos++, false, 3, 5);
		return pos;
	}

	public ParticleSystem(ParticleSystem particleSys) {
		crc = particleSys.crc;
		partFlags = particleSys.partFlags;
		pattern = particleSys.pattern;
		maxAge = particleSys.maxAge;
		startAge = particleSys.startAge;
		innerAngle = particleSys.innerAngle;
		outerAngle = particleSys.outerAngle;
		burstRate = particleSys.burstRate;
		burstRadius = particleSys.burstRadius;
		burstSpeedMin = particleSys.burstSpeedMin;
		burstSpeedMax = particleSys.burstSpeedMax;
		burstPartCount = particleSys.burstPartCount;
		angularVelocity = new Vector3(particleSys.angularVelocity);
		partAcceleration = new Vector3(particleSys.partAcceleration);
		texture = particleSys.texture;
		target = particleSys.target;
		partDataFlags = particleSys.partDataFlags;
		partMaxAge = particleSys.partMaxAge;
		partStartColor = new Color4(particleSys.partStartColor);
		partEndColor = new Color4(particleSys.partEndColor);
		partStartScaleX = particleSys.partStartScaleX;
		partStartScaleY = particleSys.partStartScaleY;
		partEndScaleX = particleSys.partEndScaleX;
		partEndScaleY = particleSys.partEndScaleY;
	}

	private void init() {
		crc = 0;
		partFlags = ParticleFlags.None;
		pattern = SourcePattern.None;
		maxAge = startAge = innerAngle = outerAngle = burstRate = burstRadius = burstSpeedMin = burstSpeedMax = 0.0f;
		burstPartCount = 0;
		angularVelocity = partAcceleration = Vector3.ZERO;
		texture = target = UUID.ZERO;
		partDataFlags = ParticleDataFlags.None;
		partMaxAge = 0.0f;
		partStartColor = partEndColor = Color4.BLACK;
		partStartScaleX = partStartScaleY = partEndScaleX = partEndScaleY = 0.0f;
	}

	/**
	 * Generate byte[] array from particle data
	 *
	 * @return Byte array
	 */
	public byte[] getBytes() {
		int pos = 0;
		int size = legacyDataBlockSize;

		if (!isLegacyCompatible())
			size += 8; // two new ints for size
		if (hasGlow())
			size += 2; // two bytes for start and end glow
		if (hasBlendFunc())
			size += 2; // two bytes for start and end blend function

		byte[] bytes = new byte[size];
		if (isLegacyCompatible()) {
			pos += packSystemBytes(bytes, pos);
			pos += packLegacyData(bytes, pos);
		} else {
			pos += Helpers.uint32ToBytesL(sysDataSize, bytes, pos);
			pos += packSystemBytes(bytes, pos);
			int partSize = partDataSize;
			if (hasGlow()) {
				partSize += 2; // two bytes for start and end glow
				partDataFlags |= ParticleDataFlags.DataGlow;
			}
			if (hasBlendFunc()) {
				partSize += 2; // two bytes for start end end blend function
				partDataFlags |= ParticleDataFlags.DataBlend;
			}
			pos += Helpers.uint32ToBytesL(partSize, bytes, pos);
			pos += packLegacyData(bytes, pos);

			if (hasGlow()) {
				bytes[pos++] = Helpers.floatToByte(partStartGlow, 0.0f, 1.0f);
				bytes[pos++] = Helpers.floatToByte(partEndGlow, 0.0f, 1.0f);
			}

			if (hasBlendFunc()) {
				bytes[pos++] = Helpers.floatToByte(blendFuncSource, 0.0f, 1.0f);
				bytes[pos++] = Helpers.floatToByte(blendFuncDest, 0.0f, 1.0f);
			}
		}
		return bytes;
	}

	private int packSystemBytes(byte[] bytes, int pos) {
		pos += Helpers.uint32ToBytesL(crc, bytes, pos);
		pos += Helpers.uint32ToBytesL(partFlags, bytes, pos);
		bytes[pos++] = pattern;
		pos += Helpers.fixedToBytesL(bytes, pos, maxAge, false, 8, 8);
		pos += Helpers.fixedToBytesL(bytes, pos, startAge, false, 8, 8);
		pos += Helpers.fixedToBytesL(bytes, pos, innerAngle, false, 3, 5);
		pos += Helpers.fixedToBytesL(bytes, pos, outerAngle, false, 3, 5);
		pos += Helpers.fixedToBytesL(bytes, pos, burstRate, false, 8, 8);
		pos += Helpers.fixedToBytesL(bytes, pos, burstRadius, false, 8, 8);
		pos += Helpers.fixedToBytesL(bytes, pos, burstSpeedMin, false, 8, 8);
		pos += Helpers.fixedToBytesL(bytes, pos, burstSpeedMax, false, 8, 8);
		bytes[pos++] = burstPartCount;
		pos += Helpers.fixedToBytesL(bytes, pos, angularVelocity.x, true, 8, 7);
		pos += Helpers.fixedToBytesL(bytes, pos, angularVelocity.y, true, 8, 7);
		pos += Helpers.fixedToBytesL(bytes, pos, angularVelocity.z, true, 8, 7);
		pos += Helpers.fixedToBytesL(bytes, pos, partAcceleration.x, true, 8, 7);
		pos += Helpers.fixedToBytesL(bytes, pos, partAcceleration.y, true, 8, 7);
		pos += Helpers.fixedToBytesL(bytes, pos, partAcceleration.z, true, 8, 7);
		pos += texture.toBytes(bytes, pos);
		pos += target.toBytes(bytes, pos);
		return pos;
	}

	private int packLegacyData(byte[] bytes, int pos) {
		pos += Helpers.uint32ToBytesL(partDataFlags, bytes, pos);
		pos += Helpers.fixedToBytesL(bytes, pos, partMaxAge, false, 8, 8);
		pos += partStartColor.toBytes(bytes, pos);
		pos += partEndColor.toBytes(bytes, pos);
		pos += Helpers.fixedToBytesL(bytes, pos, partStartScaleX, false, 3, 5);
		pos += Helpers.fixedToBytesL(bytes, pos, partStartScaleY, false, 3, 5);
		pos += Helpers.fixedToBytesL(bytes, pos, partEndScaleX, false, 3, 5);
		pos += Helpers.fixedToBytesL(bytes, pos, partEndScaleY, false, 3, 5);
		return pos;
	}

	public OSD serialize() {
		OSDMap map = new OSDMap();

		map.put("crc", OSD.fromInteger(crc));
		map.put("part_flags", OSD.fromInteger(partFlags));
		map.put("pattern", OSD.fromInteger(pattern));
		map.put("max_age", OSD.fromReal(maxAge));
		map.put("start_age", OSD.fromReal(startAge));
		map.put("inner_angle", OSD.fromReal(innerAngle));
		map.put("outer_angle", OSD.fromReal(outerAngle));
		map.put("burst_rate", OSD.fromReal(burstRate));
		map.put("burst_radius", OSD.fromReal(burstRadius));
		map.put("burst_speed_min", OSD.fromReal(burstSpeedMin));
		map.put("burst_speed_max", OSD.fromReal(burstSpeedMax));
		map.put("burst_part_count", OSD.fromInteger(burstPartCount));
		map.put("ang_velocity", OSD.fromVector3(angularVelocity));
		map.put("part_acceleration", OSD.fromVector3(partAcceleration));
		map.put("texture", OSD.fromUUID(texture));
		map.put("target", OSD.fromUUID(target));

		map.put("part_data_flags", OSD.fromInteger(partDataFlags));
		map.put("part_max_age", OSD.fromReal(partMaxAge));
		map.put("part_start_color", OSD.fromColor4(partStartColor));
		map.put("part_end_color", OSD.fromColor4(partEndColor));
		map.put("part_start_scale", OSD.fromVector3(new Vector3(partStartScaleX, partStartScaleY, 0f)));
		map.put("part_end_scale", OSD.fromVector3(new Vector3(partEndScaleX, partEndScaleY, 0f)));

		if (hasGlow()) {
			map.put("part_start_glow", OSD.fromReal(partStartGlow));
			map.put("part_end_glow", OSD.fromReal(partEndGlow));
		}

		if (hasBlendFunc()) {
			map.put("blendfunc_source", OSD.fromInteger(blendFuncSource));
			map.put("blendfunc_dest", OSD.fromInteger(blendFuncDest));
		}
		return map;
	}

	public void fromOSD(OSD osd) {
		if (osd instanceof OSDMap) {
			OSDMap map = (OSDMap) osd;

			crc = map.get("crc").asUInteger();
			partFlags = map.get("part_flags").asUInteger();
			pattern = SourcePattern.setValue(map.get("pattern").asInteger());
			maxAge = (float) map.get("max_age").asReal();
			startAge = (float) map.get("start_age").asReal();
			innerAngle = (float) map.get("inner_angle").asReal();
			outerAngle = (float) map.get("outer_angle").asReal();
			burstRate = (float) map.get("burst_rate").asReal();
			burstRadius = (float) map.get("burst_radius").asReal();
			burstSpeedMin = (float) map.get("burst_speed_min").asReal();
			burstSpeedMax = (float) map.get("burst_speed_max").asReal();
			burstPartCount = (byte) map.get("burst_part_count").asInteger();
			angularVelocity = map.get("ang_velocity").asVector3();
			partAcceleration = map.get("part_acceleration").asVector3();
			texture = map.get("texture").asUUID();
			target = map.get("target").asUUID();

			partDataFlags = ParticleDataFlags.setValue(map.get("part_data_flags").asUInteger());
			partMaxAge = (float) map.get("part_max_age").asReal();
			partStartColor = map.get("part_start_color").asColor4();
			partEndColor = map.get("part_end_color").asColor4();

			Vector3 ss = map.get("part_start_scale").asVector3();
			partStartScaleX = ss.x;
			partStartScaleY = ss.y;

			Vector3 es = map.get("part_end_scale").asVector3();
			partEndScaleX = es.x;
			partEndScaleY = es.y;

			if (map.containsKey("part_start_glow")) {
				partStartGlow = (float) map.get("part_start_glow").asReal();
				partEndGlow = (float) map.get("part_end_glow").asReal();
			}

			if (map.containsKey("blendfunc_source")) {
				blendFuncSource = (byte) map.get("blendfunc_source").asUInteger();
				blendFuncDest = (byte) map.get("blendfunc_dest").asUInteger();
			}
		} else {
			init();
		}
	}
}
