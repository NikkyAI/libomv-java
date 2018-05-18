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

// Describes physics attributes of the prim
public class PhysicsProperties {
	// Type of physics representation used for this prim in the simulator
	public enum PhysicsShapeType {
		// Use prim physics form this object
		Prim,
		// No physics, prim doesn't collide
		None,
		// Use convex hull represantion of this prim
		ConvexHull;

		public static PhysicsShapeType setValue(int value) {
			if (value <= ConvexHull.getValue())
				return values()[value];
			return Prim;
		}

		public static byte getValue(PhysicsShapeType value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// Primitive's local ID
	public int localID;
	// Density (1000 for normal density)
	public float density;
	// Friction
	public float friction;
	// Gravity multiplier (1 for normal gravity)
	public float gravityMultiplier;
	// Type of physics representation of this primitive in the simulator
	public PhysicsShapeType shapeType;
	// Restitution
	public float restitution;

	/**
	 * Creates PhysicsProperties from OSD
	 *
	 * @param name
	 *            OSDMap with incoming data</param>
	 */
	public PhysicsProperties(OSD osd) {
		if (osd instanceof OSDMap) {
			OSDMap map = (OSDMap) osd;
			localID = map.get("LocalID").AsUInteger();
			density = (float) map.get("Density").AsReal();
			friction = (float) map.get("Friction").AsReal();
			gravityMultiplier = (float) map.get("GravityMultiplier").AsReal();
			restitution = (float) map.get("Restitution").AsReal();
			shapeType = PhysicsShapeType.setValue(map.get("PhysicsShapeType").AsInteger());
		}
	}

	/**
	 * Serializes PhysicsProperties to OSD
	 *
	 * @returns OSDMap with serialized PhysicsProperties data
	 */
	public OSDMap getOSD() {
		OSDMap map = new OSDMap(6);
		map.put("LocalID", OSD.FromUInteger(localID));
		map.put("Density", OSD.FromReal(density));
		map.put("Friction", OSD.FromReal(friction));
		map.put("GravityMultiplier", OSD.FromReal(gravityMultiplier));
		map.put("Restitution", OSD.FromReal(restitution));
		map.put("PhysicsShapeType", OSD.FromInteger(shapeType.getValue()));
		return map;
	}
}
